package com.material.agent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档模型
 */
@Data
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 50)
    private String fileType;
    
    @Column(length = 500)
    private String filePath;
    
    @Column(length = 100)
    private String uploadedBy;
    
    @Column(name = "material_id")
    private Long materialId;
    
    @Column(length = 20)
    private String status = "pending";
    
    @Column(name = "vector_status")
    private String vectorStatus = "pending";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
