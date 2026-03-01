package com.material.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.material.agent.model.ChatSession;
import com.material.agent.repository.ChatSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 对话历史管理服务
 * 支持会话存储、加载、压缩
 */
@Slf4j
@Service
public class ChatHistoryService {

    private final ChatSessionRepository chatSessionRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 内存缓存近期会话（避免频繁查库）
    private final Map<String, List<ChatMessage>> memoryCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;
    private static final Duration SESSION_EXPIRE = Duration.ofHours(24);

    public ChatHistoryService(ChatSessionRepository chatSessionRepository,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.chatSessionRepository = chatSessionRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存用户消息
     */
    public void saveUserMessage(String sessionId, String userId, String content) {
        saveMessage(sessionId, userId, content, "user");
    }

    /**
     * 保存助手消息
     */
    public void saveAssistantMessage(String sessionId, String content) {
        saveMessage(sessionId, "assistant", content, "assistant");
    }

    /**
     * 保存消息
     */
    private void saveMessage(String sessionId, String senderId, String content, String role) {
        try {
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(senderId);
            session.setContent(content);
            session.setRole(role);
            session.setCreatedAt(Instant.now());
            
            chatSessionRepository.save(session);
            
            // 更新缓存
            memoryCache.computeIfAbsent(sessionId, k -> new ArrayList<>())
                    .add(new ChatMessage(content, role, Instant.now()));
            
            // 缓存超限清理
            if (memoryCache.get(sessionId).size() > MAX_CACHE_SIZE) {
                List<ChatMessage> msgs = memoryCache.get(sessionId);
                List<ChatMessage> compacted = compressMessages(msgs);
                memoryCache.put(sessionId, compacted);
            }
            
            // Redis 缓存（用于分布式）
            redisTemplate.opsForValue().set(
                    "chat:session:" + sessionId,
                    objectMapper.writeValueAsString(getRecentMessages(sessionId, 10)),
                    SESSION_EXPIRE
            );
            
        } catch (Exception e) {
            log.error("保存消息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取会话历史
     */
    public List<ChatMessage> getHistory(String sessionId, int limit) {
        // 1. 先查内存缓存
        List<ChatMessage> cached = memoryCache.get(sessionId);
        if (cached != null && cached.size() >= limit) {
            return cached.subList(Math.max(0, cached.size() - limit), cached.size());
        }
        
        // 2. 查 Redis
        try {
            String redisData = redisTemplate.opsForValue().get("chat:session:" + sessionId);
            if (redisData != null) {
                List<ChatMessage> redisMsgs = objectMapper.readValue(redisData, 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ChatMessage.class));
                return redisMsgs.subList(Math.max(0, redisMsgs.size() - limit), redisMsgs.size());
            }
        } catch (Exception e) {
            log.warn("Redis 获取失败: {}", e.getMessage());
        }
        
        // 3. 查数据库
        return chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream()
                .limit(limit)
                .map(s -> new ChatMessage(s.getContent(), s.getRole(), s.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * 获取最近 N 条消息
     */
    public List<ChatMessage> getRecentMessages(String sessionId, int count) {
        return getHistory(sessionId, count);
    }

    /**
     * 压缩历史消息（保留关键信息）
     */
    private List<ChatMessage> compressMessages(List<ChatMessage> messages) {
        if (messages.size() <= 20) {
            return messages;
        }
        
        // 保留最近 10 条 + 压缩早期消息
        List<ChatMessage> recent = messages.subList(messages.size() - 10, messages.size());
        
        // 早期消息摘要
        int earlyCount = messages.size() - 10;
        String summary = String.format("[早期 %d 条消息已压缩]", earlyCount);
        
        List<ChatMessage> result = new ArrayList<>();
        result.add(new ChatMessage(summary, "system", Instant.now()));
        result.addAll(recent);
        
        return result;
    }

    /**
     * 构建上下文提示
     */
    public String buildContextPrompt(String sessionId, int historySize) {
        List<ChatMessage> history = getHistory(sessionId, historySize);
        
        if (history.isEmpty()) {
            return "";
        }
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("对话历史：\n");
        
        for (ChatMessage msg : history) {
            String role = "user".equals(msg.role()) ? "用户" : "助手";
            prompt.append(role).append(": ").append(msg.content()).append("\n");
        }
        
        return prompt.toString();
    }

    /**
     * 清理会话
     */
    public void clearSession(String sessionId) {
        memoryCache.remove(sessionId);
        redisTemplate.delete("chat:session:" + sessionId);
        chatSessionRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .forEach(chatSessionRepository::delete);
    }

    /**
     * 获取会话摘要
     */
    public SessionSummary getSessionSummary(String sessionId) {
        List<ChatMessage> history = getHistory(sessionId, 100);
        
        return new SessionSummary(
                sessionId,
                history.size(),
                history.isEmpty() ? null : history.get(0).timestamp(),
                history.isEmpty() ? null : history.get(history.size() - 1).timestamp()
        );
    }

    /**
     * 消息记录
     */
    public record ChatMessage(String content, String role, Instant timestamp) {}

    /**
     * 会话摘要
     */
    public record SessionSummary(String sessionId, int messageCount, 
                                 Instant firstMessage, Instant lastMessage) {}
}
