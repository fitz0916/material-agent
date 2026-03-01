package com.material.agent.agent;

import com.material.agent.service.ChatHistoryService;
import com.material.agent.service.ChatModelManager;
import com.material.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 增强版 ReAct Agent
 * 支持：反思、多模型切换、超时控制
 */
@Slf4j
@Component
public class EnhancedReActAgent {

    private final ChatClient defaultClient;
    private final ChatModelManager modelManager;
    private final ChatHistoryService historyService;
    private final Map<String, Tool> toolRegistry;
    private static final int MAX_ITERATIONS = 10;
    private static final long TIMEOUT_SECONDS = 30;

    // 正则匹配
    private static final Pattern THOUGHT_PATTERN = Pattern.compile("Thought:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(\\w+)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile("Action Input:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBSERVATION_PATTERN = Pattern.compile("Observation:\\s*(.+?)(?:\\n|$)", Pattern.CASE_INSENSITIVE);

    public EnhancedReActAgent(
            ChatClient defaultClient,
            ChatModelManager modelManager,
            ChatHistoryService historyService,
            List<Tool> tools) {
        this.defaultClient = defaultClient;
        this.modelManager = modelManager;
        this.historyService = historyService;
        this.toolRegistry = tools.stream().collect(Collectors.toMap(Tool::getName, t -> t));
    }

    /**
     * 执行增强 ReAct 循环
     */
    public String think(String userInput, String context) {
        return think(userInput, context, MAX_ITERATIONS);
    }

    /**
     * 执行增强 ReAct 循环（可自定义迭代次数）
     */
    public String think(String userInput, String context, int maxIterations) {
        StringBuilder conversation = new StringBuilder();
        String currentInput = userInput;
        
        log.info("开始增强 ReAct 推理，输入: {}", userInput);

        for (int i = 0; i < maxIterations; i++) {
            log.debug("ReAct 迭代 #{}", i + 1);
            
            try {
                // 1. 思考阶段
                String thought = reason(currentInput, context, conversation.toString());
                log.debug("Thought: {}", thought);
                
                // 2. 检查是否完成
                if (isFinalAnswer(thought)) {
                    String answer = extractAnswer(thought);
                    log.info("ReAct 循环结束，得到最终答案");
                    return answer;
                }
                
                // 3. 解析动作
                String action = extractAction(thought);
                String actionInput = extractActionInput(thought);
                log.debug("Action: {}, Input: {}", action, actionInput);
                
                // 4. 执行动作
                String observation = execute(action, actionInput);
                log.debug("Observation: {}", observation);
                
                // 5. 更新对话历史
                conversation.append("\nThought: ").append(thought);
                conversation.append("\nAction: ").append(action);
                conversation.append("\nAction Input: ").append(actionInput);
                conversation.append("\nObservation: ").append(observation);
                
                // 6. 继续下一轮
                currentInput = userInput + "\n\n" + conversation;

            } catch (Exception e) {
                log.error("ReAct 迭代 #{} 失败: {}", i + 1, e.getMessage());
                
                // 7. 尝试切换模型
                if (attemptModelFallback()) {
                    log.info("尝试使用备用模型重试");
                    conversation.setLength(0);
                    currentInput = userInput;
                    i--; // 重试当前迭代
                    continue;
                }
                
                return "处理您的请求时遇到问题，请稍后重试。错误: " + e.getMessage();
            }
        }
        
        log.warn("达到最大迭代次数 {}，返回当前结果", maxIterations);
        return "已达到最大迭代次数，请尝试更具体的问题。";
    }

    /**
     * 思考阶段
     */
    private String reason(String input, String context, String history) {
        String systemPrompt = buildSystemPrompt();
        
        String userPrompt = String.format("""
            %s
            
            用户输入：%s
            
            对话历史：
            %s
            
            请按以下格式输出：
            Thought: 你的思考
            Action: 工具名称 (或 finish)
            Action Input: 给工具的参数 (如果是 finish 则为空)
            """, 
            context != null ? "额外上下文：" + context : "",
            input,
            history.isEmpty() ? "无" : history
        );
        
        ChatResponse response = modelManager.chat(
                systemPrompt,
                userPrompt
        );
        
        return response.getResult().getOutput().getText();
    }

    /**
     * 构建系统提示
     */
    private String buildSystemPrompt() {
        String toolsDesc = toolRegistry.values().stream()
                .map(t -> String.format("- %s: %s", t.getName(), t.getDescription()))
                .collect(Collectors.joining("\n"));
        
        return """
            你是一个智能助手，使用 ReAct (Reasoning + Acting) 模式来解决问题。
            
            可用工具：
            """ + toolsDesc + """
            
            输出格式要求：
            1. Thought: 分析问题，确定下一步
            2. Action: 选择工具（使用 tools 中定义的名称）
            3. Action Input: 传递给工具的参数（JSON 格式）
            4. Observation: 工具执行结果会自动补充
            5. 如果可以回答问题，使用 Action: finish，Action Input: 最终答案
            
            重要规则：
            - 每轮只能选择一个 Action
            - 如果需要查询信息，使用工具
            - Action Input 必须是可以解析的
            - 如果已经有足够信息回答问题，使用 finish
            """;
    }

    /**
     * 提取思考内容
     */
    private String extractAction(String thought) {
        Matcher matcher = ACTION_PATTERN.matcher(thought);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // fallback: 检查是否包含工具名
        for (String toolName : toolRegistry.keySet()) {
            if (thought.toLowerCase().contains(toolName.toLowerCase())) {
                return toolName;
            }
        }
        
        return "finish";
    }

    /**
     * 提取动作输入
     */
    private String extractActionInput(String thought) {
        Matcher matcher = ACTION_INPUT_PATTERN.matcher(thought);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    /**
     * 检查是否是最终答案
     */
    private boolean isFinalAnswer(String thought) {
        return thought.toLowerCase().contains("action: finish") ||
               thought.toLowerCase().contains("action:finish") ||
               thought.contains("FINAL_ANSWER");
    }

    /**
     * 提取答案
     */
    private String extractAnswer(String thought) {
        // 尝试多种格式
        String[] patterns = {
            "Action Input:\\s*(.+?)(?:\\n|$)",
            "final answer[:：]\\s*(.+?)(?:\\n|$)",
            "FINAL_ANSWER[:：]?\\s*(.+)"
        };
        
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(thought);
            if (m.find()) {
                return m.group(1).trim();
            }
        }
        
        // fallback: 返回整个 thought
        return thought.replaceAll(".*(finish|FINAL_ANSWER)[:：]?\\s*", "").trim();
    }

    /**
     * 执行动作
     */
    private String execute(String action, String actionInput) {
        if ("finish".equalsIgnoreCase(action)) {
            return actionInput;
        }
        
        Tool tool = toolRegistry.get(action);
        if (tool == null) {
            return "错误：未找到工具 " + action + "，可用工具: " + toolRegistry.keySet();
        }
        
        try {
            // 解析参数
            Object params = parseActionInput(actionInput);
            Object result = tool.execute(params);
            return formatResult(result);
        } catch (Exception e) {
            log.error("工具执行失败: {} - {}", action, e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }

    /**
     * 解析动作输入为参数
     */
    private Object parseActionInput(String input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }
        
        input = input.trim();
        
        // 尝试解析为 JSON
        if (input.startsWith("{") && input.endsWith("}")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> json = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(input, Map.class);
                return json;
            } catch (Exception e) {
                // JSON 解析失败，当作文本处理
            }
        }
        
        // 简单参数
        return Map.of("query", input);
    }

    /**
     * 格式化结果
     */
    private String formatResult(Object result) {
        if (result == null) return "无结果";
        if (result instanceof String) return (String) result;
        return result.toString();
    }

    /**
     * 尝试模型降级
     */
    private boolean attemptModelFallback() {
        try {
            Map<String, io.github.resilience4j.circuitbreaker.CircuitBreaker.State> models = 
                    modelManager.getAvailableModels();
            
            for (Map.Entry<String, io.github.resilience4j.circuitbreaker.CircuitBreaker.State> entry : 
                    models.entrySet()) {
                if (entry.getValue() == io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED
                        && !entry.getKey().equals(modelManager.getCurrentModel())) {
                    modelManager.switchModel(entry.getKey());
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("模型降级失败: {}", e.getMessage());
        }
        return false;
    }
}
