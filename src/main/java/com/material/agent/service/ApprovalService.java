package com.material.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 审批服务
 * 支持多种审批类型：采购、领用、报废等
 */
@Slf4j
@Service
public class ApprovalService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    private static final String APPROVAL_KEY = "approval:task:";
    private static final String APPROVAL_HISTORY = "approval:history:";
    private static final Duration EXPIRE_TIME = Duration.ofDays(7);
    
    public ApprovalService(RedisTemplate<String, Object> redisTemplate,
                          NotificationService notificationService,
                          ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 创建审批任务
     */
    public ApprovalTask createApprovalTask(ApprovalTask task) {
        String taskId = generateTaskId();
        task.setId(taskId);
        task.setStatus(ApprovalStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        
        // 保存任务
        saveTask(task);
        
        // 通知审批人
        notifyApprovers(task);
        
        log.info("创建审批任务: {} - {}", taskId, task.getType());
        return task;
    }
    
    /**
     * 审批通过
     */
    public ApprovalTask approve(String taskId, String approverId, String approverName, String comment) {
        ApprovalTask task = getTask(taskId);
        if (task == null) {
            throw new RuntimeException("审批任务不存在或已过期");
        }
        
        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("任务状态不允许审批: " + task.getStatus());
        }
        
        // 更新任务状态
        task.setStatus(ApprovalStatus.APPROVED);
        task.setApprovedBy(approverId);
        task.setApprovedByName(approverName);
        task.setApprovalComment(comment);
        task.setApprovedAt(LocalDateTime.now());
        
        saveTask(task);
        
        // 记录审批历史
        recordHistory(task);
        
        // 通知申请人
        notificationService.sendNotification(
                task.getCreatedBy(),
                "审批通过",
                "您的" + task.getType() + "申请已通过审批",
                "approval",
                taskId
        );
        
        log.info("审批通过: {} by {}", taskId, approverName);
        return task;
    }
    
    /**
     * 审批拒绝
     */
    public ApprovalTask reject(String taskId, String approverId, String approverName, String reason) {
        ApprovalTask task = getTask(taskId);
        if (task == null) {
            throw new RuntimeException("审批任务不存在或已过期");
        }
        
        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("任务状态不允许审批: " + task.getStatus());
        }
        
        task.setStatus(ApprovalStatus.REJECTED);
        task.setApprovedBy(approverId);
        task.setApprovedByName(approverName);
        task.setApprovalComment(reason);
        task.setApprovedAt(LocalDateTime.now());
        
        saveTask(task);
        recordHistory(task);
        
        notificationService.sendNotification(
                task.getCreatedBy(),
                "审批拒绝",
                "您的" + task.getType() + "申请未通过审批: " + reason,
                "approval",
                taskId
        );
        
        log.info("审批拒绝: {} by {}, 原因: {}", taskId, approverName, reason);
        return task;
    }
    
    /**
     * 撤回审批
     */
    public void withdraw(String taskId, String userId) {
        ApprovalTask task = getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!task.getCreatedBy().equals(userId)) {
            throw new RuntimeException("只能撤回自己创建的申请");
        }
        
        if (task.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("只能撤回待审批的申请");
        }
        
        task.setStatus(ApprovalStatus.WITHDRAWN);
        task.setUpdatedAt(LocalDateTime.now());
        saveTask(task);
        
        log.info("撤回审批: {}", taskId);
    }
    
    /**
     * 获取任务详情
     */
    public ApprovalTask getTask(String taskId) {
        try {
            String key = APPROVAL_KEY + taskId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return null;
            return objectMapper.convertValue(value, ApprovalTask.class);
        } catch (Exception e) {
            log.error("获取任务失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取待办任务
     */
    public List<ApprovalTask> getPendingTasks(String approverId) {
        // 简化实现，实际应该用 Redis 集合或数据库查询
        Set<String> keys = redisTemplate.keys(APPROVAL_KEY + "*");
        List<ApprovalTask> result = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                try {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        ApprovalTask task = objectMapper.convertValue(value, ApprovalTask.class);
                        if (task.getStatus() == ApprovalStatus.PENDING) {
                            result.add(task);
                        }
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取用户申请历史
     */
    public List<ApprovalTask> getUserTasks(String userId) {
        Set<String> keys = redisTemplate.keys(APPROVAL_KEY + "*");
        List<ApprovalTask> result = new ArrayList<>();
        
        if (keys != null) {
            for (String key : keys) {
                try {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        ApprovalTask task = objectMapper.convertValue(value, ApprovalTask.class);
                        if (task.getCreatedBy().equals(userId)) {
                            result.add(task);
                        }
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
        
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }
    
    private void saveTask(ApprovalTask task) {
        try {
            String key = APPROVAL_KEY + task.getId();
            redisTemplate.opsForValue().set(key, task, EXPIRE_TIME);
        } catch (Exception e) {
            log.error("保存任务失败: {}", e.getMessage());
            throw new RuntimeException("保存任务失败");
        }
    }
    
    private void recordHistory(ApprovalTask task) {
        try {
            String key = APPROVAL_HISTORY + task.getId();
            redisTemplate.opsForValue().set(key, task, EXPIRE_TIME);
        } catch (Exception e) {
            log.warn("记录历史失败: {}", e.getMessage());
        }
    }
    
    private void notifyApprovers(ApprovalTask task) {
        // 通知管理员
        notificationService.sendNotification(
                "admin",
                "新审批任务",
                "有新的" + task.getType() + "申请需要审批",
                "approval",
                Long.parseLong(task.getId())
        );
    }
    
    private String generateTaskId() {
        return "AT" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }
    
    /**
     * 审批任务模型
     */
    public static class ApprovalTask {
        private String id;
        private String type;
        private String title;
        private String content;
        private String createdBy;
        private String createdByName;
        private ApprovalStatus status;
        private String approvedBy;
        private String approvedByName;
        private String approvalComment;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime approvedAt;
        private Map<String, Object> metadata;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        public ApprovalStatus getStatus() { return status; }
        public void setStatus(ApprovalStatus status) { this.status = status; }
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        public String getApprovedByName() { return approvedByName; }
        public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
        public String getApprovalComment() { return approvalComment; }
        public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public LocalDateTime getApprovedAt() { return approvedAt; }
        public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, WITHDRAWN, CANCELLED
    }
}
