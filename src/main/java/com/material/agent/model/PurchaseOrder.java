package com.material.agent.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单模型
 */
@Data
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;
    
    @Column(name = "material_id")
    private Long materialId;
    
    @Column(name = "material_code", length = 50)
    private String materialCode;
    
    @Column(name = "material_name", length = 200)
    private String materialName;
    
    private Integer quantity;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(length = 50)
    private String supplier;
    
    @Column(length = 20)
    private String status = "pending";
    
    @Column(name = "requester_id")
    private String requesterId;
    
    @Column(name = "requester_name")
    private String requesterName;
    
    @Column(name = "approver_id")
    private String approverId;
    
    @Column(name = "approver_name")
    private String approverName;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(columnDefinition = "TEXT")
    private String remark;
    
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;
    
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
