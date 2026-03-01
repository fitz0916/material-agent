package com.material.agent.service;

import com.material.agent.model.AuditLog;
import com.material.agent.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务
 * 记录所有关键操作
 */
@Slf4j
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 记录用户请求
     */
    @Async
    public void logUserRequest(String userId, String sessionId, String request, String response, String intent) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setSessionId(sessionId);
            auditLog.setAction("USER_REQUEST");
            auditLog.setResource("agent");
            auditLog.setRequest(request);
            auditLog.setResponse(response);
            auditLog.setIntent(intent);
            auditLog.setResult("SUCCESS");
            auditLog.setTimestamp(Instant.now());
            
            auditLogRepository.save(auditLog);
            log.debug("审计日志记录: USER_REQUEST - {}", userId);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 记录工具调用
     */
    @Async
    public void logToolInvocation(String userId, String toolName, Map<String, Object> params, Object result) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction("TOOL_INVOCATION");
            auditLog.setResource(toolName);
            auditLog.setRequest(params != null ? params.toString() : "");
            auditLog.setResponse(result != null ? result.toString() : "");
            auditLog.setResult("SUCCESS");
            auditLog.setTimestamp(Instant.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 记录系统操作
     */
    @Async
    public void logSystemAction(String userId, String action, String resource, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setResource(resource);
            auditLog.setRequest(details);
            auditLog.setResult("SUCCESS");
            auditLog.setTimestamp(Instant.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 记录错误
     */
    @Async
    public void logError(String userId, String action, String resource, String error) {
        try {
            AuditLog auditLog = new AuditLog();
            audit(userId);
           Log.setUserId auditLog.setAction(action);
            auditLog.setResource(resource);
            auditLog.setRequest(error);
            auditLog.setResult("ERROR");
            auditLog.setTimestamp(Instant.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 查询用户操作历史
     */
    public List<AuditLog> getUserAuditLogs(String userId, int limit) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * 查询会话操作历史
     */
    public List<AuditLog> getSessionAuditLogs(String sessionId, int limit) {
        return auditLogRepository.findBySessionIdOrderByTimestampDesc(sessionId)
                .stream()
                .limit(limit)
                .toList();
    }
}
