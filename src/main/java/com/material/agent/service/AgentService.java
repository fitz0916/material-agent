package com.material.agent.service;

import com.material.agent.agent.EnhancedReActAgent;
import com.material.agent.agent.ReActAgent;
import com.material.agent.mcp.McpToolRegistry;
import com.material.agent.router.Intent;
import com.material.agent.router.IntentRouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Agent 服务核心类 - 串联意图路由、Agent 推理、工具执行
 * 生产级实现：支持多模型、对话历史、监控指标
 */
@Slf4j
@Service
public class AgentService {
    
    private final IntentRouterService intentRouterService;
    private final McpToolRegistry toolRegistry;
    private final EnhancedReActAgent reActAgent;
    private final EnhancedRagService ragService;
    private final ChatHistoryService chatHistoryService;
    private final ChatModelManager modelManager;
    
    public AgentService(IntentRouterService intentRouterService, 
                       McpToolRegistry toolRegistry,
                       EnhancedReActAgent reActAgent,
                       EnhancedRagService ragService,
                       ChatHistoryService chatHistoryService,
                       ChatModelManager modelManager) {
        this.intentRouterService = intentRouterService;
        this.toolRegistry = toolRegistry;
        this.reActAgent = reActAgent;
        this.ragService = ragService;
        this.chatHistoryService = chatHistoryService;
        this.modelManager = modelManager;
    }
    
    /**
     * 处理用户消息
     */
    public String process(String message, String sessionId, String userId) {
        log.info("收到消息: {}, session: {}, user: {}", message, sessionId, userId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 保存用户消息
            chatHistoryService.saveUserMessage(sessionId, userId, message);
            
            // 2. 获取对话历史
            String historyContext = chatHistoryService.buildContextPrompt(sessionId, 10);
            
            // 3. 意图路由
            Intent intent = intentRouterService.route(message);
            log.info("意图识别结果: {}", intent);
            
            // 4. 根据意图分发处理
            String response = switch (intent) {
                case MATERIAL_QUERY -> handleMaterialQuery(message, historyContext);
                case STOCK_ANALYSIS -> handleStockAnalysis(message);
                case MATERIAL_SELECTION -> handleMaterialSelection(message, historyContext);
                case DOCUMENT_SEARCH -> handleDocumentSearch(message, historyContext);
                case PROCUREMENT -> handleProcurement(message);
                case CHAT -> handleChat(message, historyContext);
            };
            
            // 5. 保存助手消息
            chatHistoryService.saveAssistantMessage(sessionId, response);
            
            // 6. 记录指标
            long duration = System.currentTimeMillis() - startTime;
            log.info("请求处理完成 - 意图: {}, 耗时: {}ms", intent, duration);
            
            return response;
            
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
            return "处理您的请求时出错，请稍后重试。";
        }
    }
    
    /**
     * 物资查询处理
     */
    private String handleMaterialQuery(String message, String context) {
        return reActAgent.think(message, "用户想要查询物资信息。" + context);
    }
    
    /**
     * 库存分析处理
     */
    private String handleStockAnalysis(String message) {
        if (message.contains("预警") || message.contains("低库存")) {
            return analyzeLowStock();
        }
        if (message.contains("趋势") || message.contains("消耗")) {
            return "消耗趋势分析功能开发中...";
        }
        return "请选择分析维度：库存量/消耗趋势/安全库存预警";
    }
    
    /**
     * 低库存分析
     */
    private String analyzeLowStock() {
        try {
            // 通过 ReAct Agent 查询
            return reActAgent.think("查询所有低库存物资", "执行库存预警分析");
        } catch (Exception e) {
            log.error("库存分析失败", e);
            return "库存分析失败: " + e.getMessage();
        }
    }
    
    /**
     * 物资选型处理
     */
    private String handleMaterialSelection(String message, String context) {
        return ragService.query(message);
    }
    
    /**
     * 文档搜索处理
     */
    private String handleDocumentSearch(String message, String context) {
        return ragService.query(message);
    }
    
    /**
     * 采购处理
     */
    private String handleProcurement(String message) {
        return "采购申请功能需要审批流程，请通过采购申请页面提交。\n" +
               "我可以帮助您：\n" +
               "- 查询物资库存\n" +
               "- 了解采购流程\n" +
               "- 推荐替代物资";
    }
    
    /**
     * 通用对话处理
     */
    private String handleChat(String message, String context) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("你好") || lowerMessage.contains("hello")) {
            return "您好！我是物资管理智能助手，请问有什么可以帮您？";
        }
        
        if (lowerMessage.contains("帮助") || lowerMessage.contains("能做什么")) {
            return """
                我可以帮您：
                
                📦 物资查询 - 根据编码或关键词查询物资信息
                📊 库存分析 - 分析库存状态、安全库存预警
                📚 文档搜索 - 搜索技术文档和操作手册
                🔍 物资选型 - 根据参数推荐合适物资
                🛒 采购咨询 - 了解采购流程和状态
                
                请直接告诉我您需要什么帮助～
                """;
        }
        
        // 默认走增强版 ReAct
        return reActAgent.think(message, "用户在进行日常对话。" + context);
    }
    
    /**
     * 切换模型
     */
    public void switchModel(String model) {
        modelManager.switchModel(model);
    }
    
    /**
     * 获取当前模型
     */
    public String getCurrentModel() {
        return modelManager.getCurrentModel();
    }
}
