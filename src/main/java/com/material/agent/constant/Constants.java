package com.material.agent.constant;

/**
 * 系统常量
 */
public final class Constants {
    
    private Constants() {}
    
    // 订单状态
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_APPROVED = "approved";
    public static final String ORDER_STATUS_REJECTED = "rejected";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    public static final String ORDER_STATUS_COMPLETED = "completed";
    
    // 通知状态
    public static final String NOTIFICATION_UNREAD = "unread";
    public static final String NOTIFICATION_READ = "read";
    
    // 通知类型
    public static final String NOTIFICATION_TYPE_INFO = "info";
    public static final String NOTIFICATION_TYPE_WARNING = "warning";
    public static final String NOTIFICATION_TYPE_ERROR = "error";
    public static final String NOTIFICATION_TYPE_SUCCESS = "success";
    
    // 文档状态
    public static final String DOCUMENT_STATUS_PENDING = "pending";
    public static final String DOCUMENT_STATUS_ACTIVE = "active";
    public static final String DOCUMENT_STATUS_DELETED = "deleted";
    
    // 向量状态
    public static final String VECTOR_STATUS_PENDING = "pending";
    public static final String VECTOR_STATUS_INDEXED = "indexed";
    public static final String VECTOR_STATUS_FAILED = "failed";
    public static final String VECTOR_STATUS_REMOVED = "removed";
    
    // Redis Key 前缀
    public static final String REDIS_CHAT_SESSION = "chat:session:";
    public static final String REDIS_USER_SESSION = "user:session:";
    public static final String REDIS_MODEL_STATUS = "model:status:";
    
    // 默认配置
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_HISTORY_SIZE = 10;
    public static final int MAX_HISTORY_SIZE = 50;
    
    // 超时配置
    public static final int CHAT_TIMEOUT_SECONDS = 30;
    public static final int TOOL_TIMEOUT_SECONDS = 10;
}
