package com.material.agent.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IntentRouterService {
    
    private final ChatClient chatClient;
    
    public IntentRouterService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    public Intent route(String userMessage) {
        String prompt = """
            判断用户意图，只返回以下类别之一：
            - MATERIAL_QUERY: 物资查询（如查规格、查价格）
            - STOCK_ANALYSIS: 库存分析（如统计消耗，分析趋势）
            - MATERIAL_SELECTION: 物资选型（如推荐替代品）
            - DOCUMENT_SEARCH: 文档检索（如查技术手册）
            - PROCUREMENT: 采购审批
            - CHAT: 闲聊
            
            用户消息：%s
            """.formatted(userMessage);
        
        try {
            String result = chatClient.prompt(prompt).call().content();
            return Intent.valueOf(result.trim().toUpperCase());
        } catch (Exception e) {
            log.warn("意图识别失败，使用默认意图: {}", e.getMessage());
            return Intent.CHAT;
        }
    }
}
