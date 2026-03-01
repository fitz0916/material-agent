package com.material.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.prompt.UserPromptTemplate;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 多模型聊天客户端配置
 * 支持 Kimi、OpenAI、Claude、Ollama
 */
@Slf4j
@Configuration
public class ChatClientConfig {

    /**
     * 默认 ChatClient（使用配置的默认模型）
     */
    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * Kimi 专用 ChatClient
     */
    @Bean("kimiChatClient")
    @ConditionalOnProperty(name = "spring.ai.moonshot.chat.api-key")
    public ChatClient kimiChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * OpenAI 专用 ChatClient
     */
    @Bean("openAiChatClient")
    @ConditionalOnProperty(name = "spring.ai.openai.chat.api-key")
    public ChatClient openAiChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * Claude 专用 ChatClient
     */
    @Bean("claudeChatClient")
    @ConditionalOnProperty(name = "spring.ai.anthropic.chat.api-key")
    public ChatClient claudeChatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    /**
     * Ollama 专用 ChatClient
     */
    @Bean("ollamaChatClient")
    @ConditionalOnProperty(name = "spring.ai.ollama.chat.enabled")
    public ChatClient ollamaChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
