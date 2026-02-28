package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ApprovalService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String APPROVAL_KEY = "approval:";
    private static final Duration EXPIRE_TIME = Duration.ofHours(24);
    
    public ApprovalService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 创建审批任务
     */
    public String createApprovalTask(String type, String content, String requester) {
        String taskId = UUID.randomUUID().toString();
        
        Map<String, Object> taskData = Map.of(
            "id", taskId,
            "type", type,
            "content", content,
            "requester", requester,
            "status", "PENDING"
        );
        
        redisTemplate.opsForValue().set(APPROVAL_KEY + taskId, taskData, EXPIRE_TIME);
        log.info("创建审批任务: {}", taskId);
        
        return taskId;
    }
    
    /**
     * 审批通过
     */
    public void approve(String taskId, String approver) {
        Map<Object, Object> taskData = redisTemplate.opsForValue().get(APPROVAL_KEY + taskId);
        if (taskData == null) {
            throw new RuntimeException("审批任务不存在或已过期");
        }
        
        log.info("审批通过: {} by {}", taskId, approver);
    }
    
    /**
     * 审批拒绝
     */
    public void reject(String taskId, String approver, String reason) {
        log.info("审批拒绝: {} by {}, 原因: {}", taskId, approver, reason);
    }
}
