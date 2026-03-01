package com.material.agent.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多模型管理器
 * 支持模型切换、熔断、指标收集
 */
@Slf4j
@Service
public class ChatModelManager {

    private final Map<String, ChatClient> clients;
    private final Map<String, CircuitBreaker> circuitBreakers;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private String currentModel;

    public ChatModelManager(
            ChatClient defaultClient,
            Map<String, ChatClient> chatClients,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        
        this.clients = new ConcurrentHashMap<>();
        this.circuitBreakers = new ConcurrentHashMap<>();
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        
        // 注册所有客户端
        clients.put("default", defaultClient);
        chatClients.forEach((name, client) -> {
            clients.put(name, client);
            initCircuitBreaker(name);
        });
        
        this.currentModel = "default";
        log.info("初始化模型管理器，支持模型: {}", clients.keySet());
    }

    private void initCircuitBreaker(String name) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% 失败率触发熔断
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();
        
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        circuitBreaker.updateConfig(config);
        circuitBreakers.put(name, circuitBreaker);
    }

    /**
     * 切换当前模型
     */
    public void switchModel(String model) {
        if (!clients.containsKey(model)) {
            throw new IllegalArgumentException("不支持的模型: " + model);
        }
        this.currentModel = model;
        log.info("切换到模型: {}", model);
    }

    /**
     * 获取当前模型名称
     */
    public String getCurrentModel() {
        return currentModel;
    }

    /**
     * 获取可用模型列表
     */
    public Map<String, CircuitBreaker.State> getAvailableModels() {
        Map<String, CircuitBreaker.State> result = new ConcurrentHashMap<>();
        clients.keySet().forEach(name -> {
            CircuitBreaker cb = circuitBreakers.get(name);
            result.put(name, cb != null ? cb.getState() : CircuitBreaker.State.CLOSED);
        });
        return result;
    }

    /**
     * 发送聊天请求（带熔断）
     */
    public ChatResponse chat(String systemPrompt, String userMessage) {
        return chat(currentModel, systemPrompt, userMessage);
    }

    /**
     * 指定模型发送聊天请求
     */
    public ChatResponse chat(String model, String systemPrompt, String userMessage) {
        ChatClient client = clients.get(model);
        if (client == null) {
            throw new IllegalArgumentException("模型不存在: " + model);
        }

        CircuitBreaker circuitBreaker = circuitBreakers.get(model);
        if (circuitBreaker != null && circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            // 熔断开启，尝试降级
            log.warn("模型 {} 熔断开启，尝试降级", model);
            return fallbackChat(systemPrompt, userMessage);
        }

        try {
            ChatResponse response = client.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .chatResponse();
            
            // 记录成功
            if (circuitBreaker != null) {
                circuitBreaker.onSuccess();
            }
            recordUsage(response);
            
            return response;
        } catch (Exception e) {
            // 记录失败
            if (circuitBreaker != null) {
                circuitBreaker.onFailure(e);
            }
            log.error("模型 {} 调用失败: {}", model, e.getMessage());
            throw e;
        }
    }

    /**
     * 流式聊天
     */
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return clients.get(currentModel).prompt()
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * 降级处理
     */
    private ChatResponse fallbackChat(String systemPrompt, String userMessage) {
        // 尝试找一个可用的模型
        for (Map.Entry<String, CircuitBreaker> entry : circuitBreakers.entrySet()) {
            if (entry.getValue().getState() == CircuitBreaker.State.CLOSED) {
                log.info("降级到模型: {}", entry.getKey());
                return chat(entry.getKey(), systemPrompt, userMessage);
            }
        }
        
        throw new RuntimeException("所有模型都不可用");
    }

    /**
     * 记录使用统计
     */
    private void recordUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return;
        }
        
        Usage usage = response.getMetadata().getUsage();
        if (usage != null) {
            log.debug("模型 {} 使用统计 - Prompt: {}, Completion: {}, Total: {}", 
                    currentModel,
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens());
        }
    }
}
