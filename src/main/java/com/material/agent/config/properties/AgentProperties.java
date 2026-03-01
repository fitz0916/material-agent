package com.material.agent.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 配置属性
 * 注意：Spring Boot 3.x 推荐使用 @EnableConfigurationProperties 而不是 @Component + @ConfigurationProperties
 */
@EnableConfigurationProperties(AgentProperties.class)
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {
    private int maxIterations = 5;
    private int topK = 5;
    private double temperature = 0.7;
    private String defaultModel = "kimi";
    private int maxHistorySize = 10;

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }
    public int getMaxHistorySize() { return maxHistorySize; }
    public void setMaxHistorySize(int maxHistorySize) { this.maxHistorySize = maxHistorySize; }
}
