package com.material.agent.dto;

import lombok.Data;

/**
 * 聊天请求 DTO
 */
@Data
public class ChatRequest {
    private String message;
    private String sessionId;
    private String userId;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}
