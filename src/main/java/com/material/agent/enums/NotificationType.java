package com.material.agent.enums;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    INFO("通知"),
    WARNING("警告"),
    ERROR("错误"),
    SUCCESS("成功");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
