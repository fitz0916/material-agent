package com.material.agent.controller;

import com.material.agent.service.AgentService;
import com.material.agent.service.ChatModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

/**
 * SSE 流式对话控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    private final AgentService agentService;
    private final ChatModelManager modelManager;

    public StreamingController(AgentService agentService, ChatModelManager modelManager) {
        this.agentService = agentService;
        this.modelManager = modelManager;
    }

    /**
     * SSE 流式对话（真·流式）
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        log.info("收到流式请求: {}, session: {}, user: {}", message, sessionId, userId);
        
        try {
            // 使用真正的流式响应
            return modelManager.chatStream(
                    "你是一个智能助手，请用简洁的语言回答用户问题。",
                    message
            )
            .map(chunk -> "data: " + chunk.replace("\n", "\\n") + "\n\n")
            .concatWith(Flux.just("data: [DONE]\n\n"))
            .doOnError(e -> log.error("流式响应错误: {}", e.getMessage()));
            
        } catch (Exception e) {
            log.error("流式对话失败: {}", e.getMessage());
            return Flux.just("data: {\"error\":\"" + e.getMessage() + "\"}\n\n");
        }
    }

    /**
     * SSE 流式对话（兼容模式）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatPost(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", "default");
        String userId = request.getOrDefault("userId", "anonymous");
        
        return streamChat(message, sessionId, userId);
    }

    /**
     * SSE 健康检查
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> health() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "data: {\"status\":\"alive\",\"timestamp\":" + System.currentTimeMillis() + "}\n\n");
    }

    /**
     * 事件流心跳
     */
    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ping() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(i -> ": ping\n\n");
    }
}
