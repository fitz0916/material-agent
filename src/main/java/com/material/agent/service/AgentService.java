package com.material.agent.service;

import com.material.agent.mcp.McpToolRegistry;
import com.material.agent.router.Intent;
import com.material.agent.router.IntentRouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService {
    
    private final IntentRouterService intentRouterService;
    private final McpToolRegistry toolRegistry;
    
    public AgentService(IntentRouterService intentRouterService, 
                       McpToolRegistry toolRegistry) {
        this.intentRouterService = intentRouterService;
        this.toolRegistry = toolRegistry;
    }
    
    public String process(String message, String sessionId, String userId) {
        // 1. 意图路由
        Intent intent = intentRouterService.route(message);
        log.info("意图识别结果: {}", intent);
        
        // 2. 根据意图分发处理
        return switch (intent) {
            case MATERIAL_QUERY -> handleMaterialQuery(message);
            case STOCK_ANALYSIS -> handleStockAnalysis(message);
            case MATERIAL_SELECTION -> handleMaterialSelection(message);
            case DOCUMENT_SEARCH -> handleDocumentSearch(message);
            case PROCUREMENT -> handleProcurement(message);
            case CHAT -> handleChat(message);
        };
    }
    
    private String handleMaterialQuery(String message) {
        return "请提供物资编码，如：SP-2024-X9";
    }
    
    private String handleStockAnalysis(String message) {
        return "请选择分析维度：库存量/消耗趋势/安全库存预警";
    }
    
    private String handleMaterialSelection(String message) {
        return "请提供选型参数，如：耐高温200度";
    }
    
    private String handleDocumentSearch(String message) {
        return "请输入要搜索的文档关键词";
    }
    
    private String handleProcurement(String message) {
        return "采购申请功能需要审批流程，请稍后";
    }
    
    private String handleChat(String message) {
        return "您好！我是物资管理智能助手，请问有什么可以帮您？";
    }
}
