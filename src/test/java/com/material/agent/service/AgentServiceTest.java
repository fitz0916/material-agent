package com.material.agent.service;

import com.material.agent.agent.EnhancedReActAgent;
import com.material.agent.mcp.McpToolRegistry;
import com.material.agent.router.Intent;
import com.material.agent.router.IntentRouterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AgentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private IntentRouterService intentRouterService;

    @Mock
    private McpToolRegistry toolRegistry;

    @Mock
    private EnhancedReActAgent reActAgent;

    @Mock
    private EnhancedRagService ragService;

    @Mock
    private ChatHistoryService chatHistoryService;

    @Mock
    private ChatModelManager modelManager;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(
                intentRouterService,
                toolRegistry,
                reActAgent,
                ragService,
                chatHistoryService,
                modelManager
        );
    }

    @Test
    void testProcess_MaterialQuery() {
        // Given
        String message = "查询物资 SP-2024-X9";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.MATERIAL_QUERY);
        when(reActAgent.think(anyString(), anyString())).thenReturn("物资信息...");
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
        verify(intentRouterService).route(message);
        verify(reActAgent).think(eq(message), contains("物资信息"));
    }

    @Test
    void testProcess_StockAnalysis() {
        // Given
        String message = "查询低库存物资";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.STOCK_ANALYSIS);
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
        verify(intentRouterService).route(message);
    }

    @Test
    void testProcess_DocumentSearch() {
        // Given
        String message = "搜索技术文档";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.DOCUMENT_SEARCH);
        when(ragService.query(anyString())).thenReturn("搜索结果...");
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
        verify(ragService).query(message);
    }

    @Test
    void testProcess_Procurement() {
        // Given
        String message = "我要采购物资";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.PROCUREMENT);
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("采购申请"));
    }

    @Test
    void testProcess_Chat() {
        // Given
        String message = "你好";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.CHAT);
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("您好"));
    }

    @Test
    void testProcess_Greeting() {
        // Given
        String message = "hello";
        String sessionId = "session-123";
        String userId = "user-456";

        when(intentRouterService.route(message)).thenReturn(Intent.CHAT);
        when(chatHistoryService.buildContextPrompt(anyString(), anyInt())).thenReturn("");

        // When
        String result = agentService.process(message, sessionId, userId);

        // Then
        assertNotNull(result);
    }

    @Test
    void testSwitchModel() {
        // Given
        String model = "openai";

        // When
        agentService.switchModel(model);

        // Then
        verify(modelManager).switchModel(model);
    }

    @Test
    void testGetCurrentModel() {
        // Given
        when(modelManager.getCurrentModel()).thenReturn("kimi");

        // When
        String model = agentService.getCurrentModel();

        // Then
        assertEquals("kimi", model);
    }
}
