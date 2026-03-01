package com.material.agent.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING("待审批"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    CANCELLED("已取消"),
    COMPLETED("已完成");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
