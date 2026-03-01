package com.material.agent.dto;

import lombok.Data;

/**
 * 聊天响应 DTO
 */
@Data
public class ChatResponse {
    private String content;
    private String sessionId;
    private String intent;
    private String model;
    private Long durationMs;
    private Usage usage;

    @Data
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
