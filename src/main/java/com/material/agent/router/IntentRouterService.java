package com.material.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 意图路由服务
 * 使用 LLM 智能识别用户意图
 */
@Slf4j
@Component
public class IntentRouterService {
    
    private final ChatClient chatClient;
    private static final Pattern INTENT_PATTERN = Pattern.compile(
        "(MATERIAL_QUERY|STOCK_ANALYSIS|MATERIAL_SELECTION|DOCUMENT_SEARCH|PROCUREMENT|CHAT)",
        Pattern.CASE_INSENSITIVE
    );
    
    public IntentRouterService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    public Intent route(String userMessage) {
        String prompt = """
            判断用户意图，只返回以下类别之一：
            - MATERIAL_QUERY: 物资查询（如查规格、查价格、查库存）
            - STOCK_ANALYSIS: 库存分析（如统计消耗，分析趋势、安全库存预警）
            - MATERIAL_SELECTION: 物资选型（如推荐替代品、根据参数选型）
            - DOCUMENT_SEARCH: 文档检索（如查技术手册、操作规程）
            - PROCUREMENT: 采购审批（如提交采购、审批状态）
            - CHAT: 闲聊或其他
            
            只返回类别名称，不要其他解释。
            
            用户消息：%s
            """.formatted(userMessage);
        
        try {
            String result = chatClient.prompt(List.of(
                new SystemMessage("你是一个意图分类器，只返回分类名称。"),
                new UserMessage(prompt)
            )).call().content();
            
            // 解析意图
            return parseIntent(result);
            
        } catch (Exception e) {
            log.warn("意图识别失败，使用默认意图: {}", e.getMessage());
            return Intent.CHAT;
        }
    }
    
    private Intent parseIntent(String result) {
        // 尝试精确匹配
        String trimmed = result.trim().toUpperCase();
        for (Intent intent : Intent.values()) {
            if (trimmed.contains(intent.name())) {
                return intent;
            }
        }
        
        // 尝试正则匹配
        Matcher matcher = INTENT_PATTERN.matcher(result);
        if (matcher.find()) {
            try {
                return Intent.valueOf(matcher.group(1).toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略
            }
        }
        
        // 默认返回闲聊
        return Intent.CHAT;
    }
}
