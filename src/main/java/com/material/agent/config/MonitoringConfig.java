package com.material.agent.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控配置
 * 配置 Actuator 端点和自定义指标
 */
@Configuration
public class MonitoringConfig {

    /**
     * AI 请求计数器
     */
    @Bean
    public Counter aiRequestCounter(MeterRegistry registry) {
        return Counter.builder("ai.requests.total")
                .description("AI 请求总数")
                .register(registry);
    }

    /**
     * AI 请求耗时计时器
     */
    @Bean
    public Timer aiRequestTimer(MeterRegistry registry) {
        return Timer.builder("ai.requests.duration")
                .description("AI 请求耗时")
                .register(registry);
    }

    /**
     * 意图识别计数器
     */
    @Bean
    public Counter intentRecognitionCounter(MeterRegistry registry) {
        return Counter.builder("intent.recognition.total")
                .description("意图识别次数")
                .register(registry);
    }

    /**
     * 工具调用计数器
     */
    @Bean
    public Counter toolInvocationCounter(MeterRegistry registry) {
        return Counter.builder("tool.invocations.total")
                .description("工具调用次数")
                .register(registry);
    }

    /**
     * RAG 查询计数器
     */
    @Bean
    public Counter ragQueryCounter(MeterRegistry registry) {
        return Counter.builder("rag.queries.total")
                .description("RAG 查询次数")
                .register(registry);
    }

    /**
     * 应用健康检查
     */
    @Bean
    public HealthIndicator applicationHealth() {
        return () -> {
            // 检查关键组件状态
            // 实际生产中应该检查数据库、Redis、向量库等
            return Health.up()
                    .withDetail("status", "running")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        };
    }
}
