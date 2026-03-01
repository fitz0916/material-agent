package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 增强版 RAG 服务
 * 支持：混合检索、RRF 融合、重排序、索引管理
 */
@Slf4j
@Service
public class EnhancedRagService {

    private final VectorStore vectorStore;
    private final ChatClient defaultChatClient;
    private final HybridSearchService hybridSearchService;
    
    // 索引状态缓存
    private final Map<String, IndexStatus> indexStatus = new ConcurrentHashMap<>();

    public EnhancedRagService(
            VectorStore vectorStore,
            @Qualifier("chatClient") ChatClient defaultChatClient,
            HybridSearchService hybridSearchService) {
        this.vectorStore = vectorStore;
        this.defaultChatClient = defaultChatClient;
        this.hybridSearchService = hybridSearchService;
    }

    /**
     * 语义检索
     */
    public List<Document> semanticSearch(String query, int topK) {
        SearchRequest request = SearchRequest.query(query)
                .topK(topK)
                .similarityThreshold(0.7);
        
        return vectorStore.similaritySearch(request);
    }

    /**
     * 混合检索（向量 + 关键词）
     */
    public List<Document> hybridSearch(String query, int topK) {
        return hybridSearchService.hybridSearch(query, topK);
    }

    /**
     * RRF 融合检索（Reciprocal Rank Fusion）
     */
    public List<Document> rrfSearch(String query, int topK) {
        // 1. 向量检索
        List<Document> vectorResults = semanticSearch(query, topK * 2);
        
        // 2. 关键词检索
        List<Document> keywordResults = hybridSearchService.keywordSearch(query, topK * 2);
        
        // 3. RRF 融合
        return rrfFusion(vectorResults, keywordResults, topK, 60);
    }

    /**
     * RRF 融合算法
     */
    private List<Document> rrfFusion(List<Document> list1, List<Document> list2, int topK, int rrfK) {
        Map<String, Double> scores = new ConcurrentHashMap<>();
        
        // 计算 list1 的 RRF 分数
        for (int i = 0; i < list1.size(); i++) {
            String id = list1.get(i).getId();
            double score = 1.0 / (rrfK + i + 1);
            scores.merge(id, score, Double::sum);
        }
        
        // 计算 list2 的 RRF 分数
        for (int i = 0; i < list2.size(); i++) {
            String id = list2.get(i).getId();
            double score = 1.0 / (rrfK + i + 1);
            scores.merge(id, score, Double::sum);
        }
        
        // 按分数排序
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> {
                    // 合并文档信息
                    Optional<Document> doc1 = list1.stream()
                            .filter(d -> d.getId().equals(entry.getKey())).findFirst();
                    Optional<Document> doc2 = list2.stream()
                            .filter(d -> d.getId().equals(entry.getKey())).findFirst();
                    
                    return doc1.orElseGet(() -> doc2.orElse(null));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 带重排序的检索
     */
    public List<Document> searchWithRerank(String query, int topK, boolean useRrf) {
        List<Document> results = useRrf ? rrfSearch(query, topK * 2) 
                                         : hybridSearch(query, topK * 2);
        
        // 简单重排序：优先返回标题匹配的
        results.sort((d1, d2) -> {
            String q = query.toLowerCase();
            boolean h1 = d1.getContent().toLowerCase().contains(q);
            boolean h2 = d2.getContent().toLowerCase().contains(q);
            if (h1 && !h2) return -1;
            if (!h1 && h2) return 1;
            return 0;
        });
        
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * RAG 问答
     */
    public String query(String question) {
        return query(question, 5, true);
    }

    /**
     * RAG 问答（可配置）
     */
    public String query(String question, int topK, boolean useRrf) {
        // 1. 检索相关文档
        List<Document> docs = searchWithRerank(question, topK, useRrf);
        
        if (docs.isEmpty()) {
            return "未找到相关文档。";
        }
        
        // 2. 构建上下文
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            String title = doc.getMetadata().getOrDefault("title", 
                    doc.getMetadata().getOrDefault("source", "文档" + (i + 1)));
            context.append(String.format("【%s】\n%s\n\n", title, doc.getContent()));
        }
        
        // 3. 调用 LLM 生成答案
        String prompt = String.format("""
            你是一个专业的技术文档助手。请根据以下参考资料回答用户问题。
            
            要求：
            1. 只根据提供的资料回答，不要编造
            2. 如果资料中没有相关信息，请如实告知
            3. 回答要准确、完整
            
            参考资料：
            %s
            
            用户问题：%s
            
            请给出回答：
            """, context, question);
        
        try {
            ChatResponse response = defaultChatClient.prompt()
                    .system("你是一个专业的技术文档助手。")
                    .user(prompt)
                    .call()
                    .chatResponse();
            
            String answer = response.getResult().getOutput().getText();
            
            // 4. 记录检索统计
            log.info("RAG 查询完成 - 问题: {}, 检索文档数: {}, 答案长度: {}", 
                    question, docs.size(), answer.length());
            
            return answer;
            
        } catch (Exception e) {
            log.error("RAG 查询失败: {}", e.getMessage());
            return "查询失败: " + e.getMessage();
        }
    }

    /**
     * 添加文档到向量库
     */
    public void addDocument(String content, Map<String, Object> metadata) {
        Document doc = new Document(content, metadata);
        vectorStore.add(List.of(doc));
        
        String docId = doc.getId();
        indexStatus.put(docId, new IndexStatus(docId, "indexed", new Date()));
        log.info("文档添加成功: {}", docId);
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (documents.isEmpty()) return;
        
        vectorStore.add(documents);
        
        documents.forEach(doc -> 
                indexStatus.put(doc.getId(), new IndexStatus(doc.getId(), "indexed", new Date()))
        );
        
        log.info("批量添加文档成功: {} 条", documents.size());
    }

    /**
     * 删除文档
     */
    public void deleteDocument(String docId) {
        vectorStore.delete(List.of(docId));
        indexStatus.put(docId, new IndexStatus(docId, "deleted", new Date()));
        log.info("文档删除成功: {}", docId);
    }

    /**
     * 获取索引状态
     */
    public Map<String, IndexStatus> getIndexStatus() {
        return Collections.unmodifiableMap(indexStatus);
    }

    /**
     * 索引状态记录
     */
    public record IndexStatus(String docId, String status, Date indexedAt) {}
}
