package com.material.agent.service;

import com.material.agent.agent.ReActAgent;
import com.material.agent.mcp.McpToolRegistry;
import com.material.agent.model.ChatSession;
import com.material.agent.router.Intent;
import com.material.agent.router.IntentRouterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent 服务核心类 - 串联意图路由、Agent 推理、工具执行
 */
@Slf4j
@Service
public class AgentService {
    
    private final IntentRouterService intentRouterService;
    private final McpToolRegistry toolRegistry;
    private final ReActAgent reActAgent;
    private final RagService ragService;
    private final MaterialQueryTool materialQueryTool;
    private final DocumentSearchTool documentSearchTool;
    private final ChatSessionRepository chatSessionRepository;
    
    public AgentService(IntentRouterService intentRouterService, 
                       McpToolRegistry toolRegistry,
                       ReActAgent reActAgent,
                       RagService ragService,
                       MaterialQueryTool materialQueryTool,
                       DocumentSearchTool documentSearchTool,
                       ChatSessionRepository chatSessionRepository) {
        this.intentRouterService = intentRouterService;
        this.toolRegistry = toolRegistry;
        this.reActAgent = reActAgent;
        this.ragService = ragService;
        this.materialQueryTool = materialQueryTool;
        this.documentSearchTool = documentSearchTool;
        this.chatSessionRepository = chatSessionRepository;
    }
    
    /**
     * 处理用户消息
     */
    public String process(String message, String sessionId, String userId) {
        log.info("收到消息: {}, session: {}, user: {}", message, sessionId, userId);
        
        // 1. 保存用户消息到会话
        saveMessage(sessionId, userId, message, "user");
        
        // 2. 意图路由
        Intent intent = intentRouterService.route(message);
        log.info("意图识别结果: {}", intent);
        
        // 3. 根据意图分发处理
        String response = switch (intent) {
            case MATERIAL_QUERY -> handleMaterialQuery(message);
            case STOCK_ANALYSIS -> handleStockAnalysis(message);
            case MATERIAL_SELECTION -> handleMaterialSelection(message);
            case DOCUMENT_SEARCH -> handleDocumentSearch(message);
            case PROCUREMENT -> handleProcurement(message);
            case CHAT -> handleChat(message);
        };
        
        // 4. 保存助手消息
        saveMessage(sessionId, "assistant", response, "assistant");
        
        // 5. 记录审计日志
        // auditLogService.log(userId, sessionId, message, response, intent);
        
        return response;
    }
    
    /**
     * 物资查询处理
     */
    private String handleMaterialQuery(String message) {
        // 使用 ReAct Agent 进行智能查询
        return reActAgent.think(message, "用户想要查询物资信息");
    }
    
    /**
     * 库存分析处理
     */
    private String handleStockAnalysis(String message) {
        // 分析用户想要的库存分析类型
        if (message.contains("预警") || message.contains("低库存")) {
            return analyzeLowStock();
        }
        if (message.contains("趋势") || message.contains("消耗")) {
            return "消耗趋势分析功能开发中...";
        }
        if (message.contains("库存量") || message.contains("盘点")) {
            return analyzeInventory();
        }
        
        return "请选择分析维度：库存量/消耗趋势/安全库存预警";
    }
    
    /**
     * 低库存分析
     */
    private String analyzeLowStock() {
        try {
            var materials = materialQueryTool.getLowStockMaterials();
            if (materials.isEmpty()) {
                return "当前没有库存低于安全库存的物资 ✅";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("⚠️ 发现 ").append(materials.size()).append(" 种物资库存低于安全库存：\n\n");
            
            for (var m : materials) {
                sb.append("- ").append(m.getMaterialCode())
                  .append(" ").append(m.getMaterialName())
                  .append("：当前 ").append(m.getCurrentStock())
                  .append("，安全库存 ").append(m.getSafetyStock())
                  .append("\n");
            }
            
            return sb.toString();
        } catch (Exception e) {
            log.error("库存分析失败", e);
            return "库存分析失败: " + e.getMessage();
        }
    }
    
    /**
     * 库存盘点
     */
    private String analyzeInventory() {
        return "库存盘点功能开发中...\n可以查询：物资编码、名称、分类、当前库存、安全库存、位置等信息。";
    }
    
    /**
     * 物资选型处理
     */
    private String handleMaterialSelection(String message) {
        // 使用 RAG 进行智能选型
        return ragService.query(message);
    }
    
    /**
     * 文档搜索处理
     */
    private String handleDocumentSearch(String message) {
        return reActAgent.think(message, "用户想要搜索文档");
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
    private String handleChat(String message) {
        // 简单对话 - 可以接入 RAG 或其他 LLM
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("你好") || lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
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
        
        if (lowerMessage.contains("谢谢")) {
            return "不客气！有问题随时问我 😊";
        }
        
        // 默认走 ReAct 智能处理
        return reActAgent.think(message, "用户在进行日常对话");
    }
    
    /**
     * 保存消息到会话历史
     */
    private void saveMessage(String sessionId, String userId, String content, String role) {
        try {
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setUserId(userId);
            chatSession.setRole(role);
            chatSession.setContent(content);
            chatSession.setCreatedAt(Instant.now());
            
            chatSessionRepository.save(chatSession);
        } catch (Exception e) {
            log.warn("保存消息失败: {}", e.getMessage());
        }
    }
}
