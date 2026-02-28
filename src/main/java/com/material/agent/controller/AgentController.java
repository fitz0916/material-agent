package com.material.agent.controller;

import com.material.agent.service.AgentService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    
    private final AgentService agentService;
    
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String result = agentService.process(
            request.getMessage(),
            request.getSessionId(),
            request.getUserId()
        );
        
        return ResponseEntity.ok(new ChatResponse(
            result,
            request.getSessionId(),
            null,
            0
        ));
    }
    
    @Data
    public static class ChatRequest {
        private String message;
        private String sessionId;
        private String userId;
    }
    
    @Data
    public static class ChatResponse {
        private String message;
        private String sessionId;
        private String intent;
        private int tokens;
        
        public ChatResponse(String message, String sessionId, String intent, int tokens) {
            this.message = message;
            this.sessionId = sessionId;
            this.intent = intent;
            this.tokens = tokens;
        }
    }
}
