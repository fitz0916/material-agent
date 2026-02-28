package com.material.agent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agent_audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_id", length = 100)
    private String userId;
    
    @Column(name = "action_type", length = 50)
    private String actionType;
    
    @Column(name = "input_summary", columnDefinition = "JSONB")
    private String inputSummary;
    
    @Column(name = "output_summary", columnDefinition = "JSONB")
    private String outputSummary;
    
    @Column(length = 50)
    private String model;
    
    @Column(name = "duration_ms")
    private Integer durationMs;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
