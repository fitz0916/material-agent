package com.material.agent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工具执行记录模型
 */
@Data
@Entity
@Table(name = "tool_executions")
public class ToolExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_id", length = 100)
    private String userId;
    
    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;
    
    @Column(name = "tool_input", columnDefinition = "TEXT")
    private String toolInput;
    
    @Column(name = "tool_output", columnDefinition = "TEXT")
    private String toolOutput;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(length = 20)
    private String status = "success";
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
