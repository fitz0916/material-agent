package com.material.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.material.agent.model.ChatSession;
import com.material.agent.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ChatHistoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private ChatHistoryService chatHistoryService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectHistoryService();
        when(objectMapper).objectMapper = new ObjectMapper();
        
        chatHistoryService = new ChatHistoryService(
                chatSessionRepository,
                redisTemplate,
                objectMapper
        );
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSaveUserMessage() {
        // Given
        String sessionId = "session-123";
        String userId = "user-456";
        String content = "查询物资";

        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(i -> i.getArgument(0));

        // When
        chatHistoryService.saveUserMessage(sessionId, userId, content);

        // Then
        verify(chatSessionRepository).save(argThat(session -> 
                session.getContent().equals(content) && 
                session.getRole().equals("user")
        ));
    }

    @Test
    void testSaveAssistantMessage() {
        // Given
        String sessionId = "session-123";
        String content = "这是查询结果";

        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(i -> i.getArgument(0));

        // When
        chatHistoryService.saveAssistantMessage(sessionId, content);

        // Then
        verify(chatSessionRepository).save(argThat(session -> 
                session.getContent().equals(content) && 
                session.getRole().equals("assistant")
        ));
    }

    @Test
    void testGetHistory_FromDatabase() {
        // Given
        String sessionId = "session-123";
        
        ChatSession session1 = new ChatSession();
        session1.setContent("用户消息");
        session1.setRole("user");
        session1.setCreatedAt(Instant.now().minusSeconds(60));
        
        ChatSession session2 = new ChatSession();
        session2.setContent("助手回复");
        session2.setRole("assistant");
        session2.setCreatedAt(Instant.now());

        when(redisTemplate.get(anyString())).thenReturn(null);
        when(chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId))
                .thenReturn(List.of(session1, session2));

        // When
        List<ChatHistoryService.ChatMessage> history = chatHistoryService.getHistory(sessionId, 10);

        // Then
        assertEquals(2, history.size());
    }

    @Test
    void testBuildContextPrompt() {
        // Given
        String sessionId = "session-123";
        
        ChatSession session1 = new ChatSession();
        session1.setContent("用户消息");
        session1.setRole("user");
        session1.setCreatedAt(Instant.now().minusSeconds(60));
        
        when(redisTemplate.get(anyString())).thenReturn(null);
        when(chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId))
                .thenReturn(List.of(session1));

        // When
        String context = chatHistoryService.buildContextPrompt(sessionId, 5);

        // Then
        assertTrue(context.contains("用户消息"));
    }

    @Test
    void testGetSessionSummary() {
        // Given
        String sessionId = "session-123";
        
        ChatSession session1 = new ChatSession();
        session1.setCreatedAt(Instant.now().minusSeconds(60));
        
        ChatSession session2 = new ChatSession();
        session2.setCreatedAt(Instant.now());

        when(redisTemplate.get(anyString())).thenReturn(null);
        when(chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId))
                .thenReturn(List.of(session1, session2));

        // When
        ChatHistoryService.SessionSummary summary = chatHistoryService.getSessionSummary(sessionId);

        // Then
        assertNotNull(summary);
        assertEquals(sessionId, summary.sessionId());
    }

    @Test
    void testClearSession() {
        // Given
        String sessionId = "session-123";
        
        ChatSession session = new ChatSession();
        session.setCreatedAt(Instant.now());
        
        when(chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId))
                .thenReturn(List.of(session));
        doNothing().when(chatSessionRepository).delete(any(ChatSession.class));
        doNothing().when(redisTemplate).delete(anyString());

        // When
        chatHistoryService.clearSession(sessionId);

        // Then
        verify(redisTemplate).delete("chat:session:" + sessionId);
        verify(chatSessionRepository).delete(any(ChatSession.class));
    }
}
