package com.material.agent.controller;

import com.material.agent.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    private final AgentService agentService;

    public StreamingController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * SSE 流式对话
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        String result = agentService.process(message, sessionId, userId);
        
        // 模拟流式输出
        return Flux.fromArray(result.split(""))
                .delayElements(Duration.ofMillis(30))
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    /**
     * SSE 健康检查
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> health() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "data: alive\n\n");
    }
}
