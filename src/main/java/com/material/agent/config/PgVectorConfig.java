package com.material.agent.config;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.chat.client.ChatModel;

@Configuration
public class PgVectorConfig {
    
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, 
                                   jakarta.persistence.EntityManager entityManager) {
        return PgVectorStore.builder()
            .embeddingModel(embeddingModel)
            .entityManager(entityManager)
            .initializeSchema(true)
            .build();
    }
}
