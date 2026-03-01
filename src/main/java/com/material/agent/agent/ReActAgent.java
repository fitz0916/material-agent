package com.material.agent.agent;

import com.material.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.prompt.UserPromptTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReAct (Reasoning + Acting) 自主规划智能体
 * 真正的 ReAct 循环实现：Thought → Action → Observation → ...
 */
@Slf4j
@Component
public class ReActAgent {

    private final ChatClient chatClient;
    private final Map<String, Tool> toolRegistry;
    private static final int MAX_ITERATIONS = 10;

    public ReActAgent(ChatClient.Builder chatClientBuilder, 
                      List<Tool> tools) {
        this.chatClient = chatClientBuilder.build();
        // 将 Tool 列表转换为 Map
        this.toolRegistry = tools.stream()
            .collect(Collectors.toMap(Tool::getName, t -> t));
    }

    /**
     * 执行 ReAct 循环
     * @param userInput 用户输入
     * @param context 额外上下文信息
     * @return 最终答案
     */
    public String think(String userInput, String context) {
        StringBuilder history = new StringBuilder();
        String currentInput = userInput;
        
        log.info("开始 ReAct 推理，输入: {}", userInput);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            log.debug("ReAct 迭代 #{}", i + 1);
            
            // 1. Thought - 推理
            String thought = reason(currentInput, context, history.toString());
            log.debug("Thought: {}", thought);
            
            // 2. 检查是否是最终答案
            if (isFinalAnswer(thought)) {
                String answer = extractAnswer(thought);
                log.info("ReAct 循环结束，得到最终答案");
                return answer;
            }
            
            // 3. Action - 选择动作
            String action = chooseAction(thought);
            log.debug("Action: {}", action);
            
            // 4. 执行 Action
            String observation = execute(action, thought);
            log.debug("Observation: {}", observation);
            
            // 5. 更新上下文
            history.append("\nThought: ").append(thought);
            history.append("\nAction: ").append(action);
            history.append("\nObservation: ").append(observation);
            
            // 6. 继续下一轮
            currentInput = userInput + "\n\n" + history;
        }
        
        log.warn("达到最大迭代次数 {}，返回当前结果", MAX_ITERATIONS);
        return "已达到最大迭代次数。请尝试更具体的问题。";
    }

    /**
     * 推理阶段 - 调用 LLM 生成 Thought
     */
    private String reason(String input, String context, String history) {
        String systemPrompt = new SystemPromptTemplate(REACT_SYSTEM_PROMPT)
            .render(Map.of(
                "tool_names", getToolNames(),
                "tool_descriptions", getToolDescriptions()
            ));
        
        String userPrompt = new UserPromptTemplate(REACT_USER_PROMPT)
            .render(Map.of(
                "input", input,
                "context", context != null ? context : "",
                "history", history
            ));
        
        Prompt prompt = new Prompt(List.of(
            new org.springframework.ai.chat.messages.SystemMessage(systemPrompt),
            new UserMessage(userPrompt)
        ));
        
        return chatClient.prompt(prompt).call().content();
    }

    /**
     * 从 Thought 中提取最终答案
     */
    private boolean isFinalAnswer(String thought) {
        return thought.contains("FINAL_ANSWER") || 
               thought.contains("最终答案") ||
               thought.contains("完成");
    }

    /**
     * 提取最终答案内容
     */
    private String extractAnswer(String thought) {
        // 尝试多种可能的答案格式
        if (thought.contains("FINAL_ANSWER:")) {
            return thought.replaceAll(".*FINAL_ANSWER:\\s*", "").trim();
        }
        if (thought.contains("最终答案：")) {
            return thought.replaceAll(".*最终答案：\\s*", "").trim();
        }
        // 如果没有明确标记，返回整个 thought
        return thought.replaceAll(".*Thought:\\s*", "").trim();
    }

    /**
     * 从 Thought 中解析要执行的 Action
     */
    private String chooseAction(String thought) {
        // 解析动作名称
        for (String toolName : toolRegistry.keySet()) {
            if (thought.toLowerCase().contains(toolName.toLowerCase())) {
                return toolName;
            }
        }
        
        // 默认返回 finish
        return "finish";
    }

    /**
     * 执行 Action
     */
    private String execute(String action, String thought) {
        // finish 动作直接返回
        if ("finish".equals(action)) {
            return extractAnswer(thought);
        }
        
        // 获取工具并执行
        Tool tool = toolRegistry.get(action);
        if (tool == null) {
            return "错误：未找到工具 " + action;
        }
        
        try {
            // 从 thought 中解析参数
            Object params = parseParams(thought, tool);
            Object result = tool.execute(params);
            return formatResult(result);
        } catch (Exception e) {
            log.error("工具执行失败: {}", action, e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    /**
     * 从 thought 中解析工具参数
     */
    private Object parseParams(String thought, Tool tool) {
        // 简化实现：提取 thought 中的关键信息作为参数
        // 实际应该用更复杂的 LLM 来解析参数
        String desc = tool.getDescription();
        
        if (desc.contains("materialCode")) {
            // 尝试提取物资编码
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Z]{2}-\\d{4}-[A-Z0-9]{2,5}");
            java.util.regex.Matcher matcher = pattern.matcher(thought);
            if (matcher.find()) {
                return Map.of("materialCode", matcher.group());
            }
        }
        
        if (desc.contains("query") || desc.contains("keyword")) {
            // 提取问句中的关键词
            String query = thought.replaceAll(".*?(查询|搜索|找|问).*?", "").trim();
            return Map.of("query", query.isEmpty() ? "物资" : query);
        }
        
        return Map.of();
    }

    /**
     * 格式化工具执行结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "无结果";
        }
        if (result instanceof String) {
            return (String) result;
        }
        return result.toString();
    }

    private String getToolNames() {
        return String.join(", ", toolRegistry.keySet());
    }

    private String getToolDescriptions() {
        return toolRegistry.values().stream()
            .map(t -> "- " + t.getName() + ": " + t.getDescription())
            .collect(Collectors.joining("\n"));
    }

    private static final String REACT_SYSTEM_PROMPT = """
        你是一个智能助手，使用 ReAct (Reasoning + Acting) 模式来解决问题。
        
        可用工具：
        {tool_names}
        
        工具描述：
        {tool_descriptions}
        
        请按以下格式思考：
        Thought: 分析用户问题，确定需要使用的工具
        Action: 工具名称（如果可以回答问题，使用 finish）
        Action Input: 给工具的参数
        
        重要规则：
        1. 每轮只能选择一个 Action
        2. 如果需要查询信息，使用工具
        3. 如果已经有足够信息回答问题，使用 finish 并给出 FINAL_ANSWER
        4. 严格按照格式输出，不要输出其他内容
        """;

    private static final String REACT_USER_PROMPT = """
        用户输入：{input}
        
        上下文信息：{context}
        
        对话历史：
        {history}
        
        请开始推理并选择下一步 Action。
        """;
}
