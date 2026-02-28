package com.material.agent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "materials")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "material_code", unique = true, nullable = false, length = 50)
    private String materialCode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 50)
    private String category;
    
    @Column(columnDefinition = "JSONB")
    private String specification;
    
    @Column(length = 20)
    private String unit;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "safety_stock")
    private Integer safetyStock = 0;
    
    @Column(name = "current_stock")
    private Integer currentStock = 0;
    
    @Column(length = 200)
    private String supplier;
    
    @Column(length = 20)
    private String status = "active";
    
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
