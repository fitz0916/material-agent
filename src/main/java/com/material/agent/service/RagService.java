package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import java.util.List;

@Slf4j
@Service
public class RagService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public List<Document> similaritySearch(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.query(query).topK(topK);
        return vectorStore.similaritySearch(searchRequest);
    }

    public String query(String question) {
        List<Document> docs = similaritySearch(question, 5);
        StringBuilder ctx = new StringBuilder();
        docs.forEach(d -> ctx.append(d.getContent()).append("\n\n"));
        
        String prompt = String.format("""
            基于以下参考资料回答。如果资料中没有，请如实告知。
            参考资料：%s
            用户问题：%s
            """, ctx, question);
        
        return chatClient.prompt(prompt).call().content();
    }

    public void addDocument(String content, java.util.Map<String, Object> metadata) {
        Document doc = new Document(content, metadata);
        vectorStore.add(List.of(doc));
    }
}
