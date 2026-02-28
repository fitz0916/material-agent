package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 混合检索服务：向量检索 + 关键词检索
 */
@Slf4j
@Service
public class HybridSearchService {

    private final VectorStore vectorStore;

    public HybridSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 混合检索
     */
    public List<Document> hybridSearch(String query, int topK) {
        // 1. 向量检索
        List<Document> vectorResults = vectorStore.similaritySearch(
                SearchRequest.query(query).topK(topK)
        );
        
        // 2. 简单关键词匹配（实际应结合 Elasticsearch）
        List<Document> keywordResults = keywordSearch(query, topK);
        
        // 3. 合并结果
        return mergeAndRerank(vectorResults, keywordResults, query);
    }

    private List<Document> keywordSearch(String query, int topK) {
        // 简化实现：返回空结果
        // 实际应集成 Elasticsearch 或使用数据库 LIKE 查询
        return new ArrayList<>();
    }

    private List<Document> mergeAndRerank(List<Document> vector, List<Document> keyword, String query) {
        List<Document> merged = new ArrayList<>();
        merged.addAll(vector);
        
        // 去重并合并
        for (Document doc : keyword) {
            boolean exists = merged.stream()
                    .anyMatch(d -> d.getId().equals(doc.getId()));
            if (!exists) {
                merged.add(doc);
            }
        }
        
        // 简单重排序：包含查询词的排前面
        merged.sort(Comparator.comparingInt((Document d) -> {
            int score = 0;
            String content = d.getContent().toLowerCase();
            for (String word : query.toLowerCase().split("\\s+")) {
                if (content.contains(word)) score += 10;
            }
            return -score;
        }));
        
        return merged.stream().limit(topK).toList();
    }
}
