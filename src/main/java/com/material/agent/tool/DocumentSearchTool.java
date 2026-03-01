package com.material.agent.tool;

import com.material.agent.service.HybridSearchService;
import com.material.agent.service.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DocumentSearchTool implements Tool {
    
    private final RagService ragService;
    private final HybridSearchService hybridSearchService;
    
    public DocumentSearchTool(RagService ragService, HybridSearchService hybridSearchService) {
        this.ragService = ragService;
        this.hybridSearchService = hybridSearchService;
    }
    
    @Override
    public String getName() {
        return "document_search";
    }
    
    @Override
    public String getDescription() {
        return "搜索物资相关的技术文档、规格说明书、操作手册等。输入：{query: 查询内容, useHybrid: 是否使用混合检索}";
    }
    
    @Override
    public Object execute(Object params) {
        if (params == null) {
            return "请提供查询参数";
        }
        
        Map<String, Object> p = (Map<String, Object>) params;
        String query = (String) p.get("query");
        
        if (query == null || query.isEmpty()) {
            return "请提供 query 参数";
        }
        
        boolean useHybrid = Boolean.TRUE.equals(p.get("useHybrid"));
        int topK = p.get("topK") != null ? (Integer) p.get("topK") : 5;
        
        try {
            List<Document> results;
            if (useHybrid) {
                results = hybridSearchService.hybridSearch(query, topK);
            } else {
                results = ragService.similaritySearch(query, topK);
            }
            
            if (results.isEmpty()) {
                return "未找到相关文档";
            }
            
            // 构建返回结果
            StringBuilder sb = new StringBuilder();
            sb.append("找到 ").append(results.size()).append(" 篇相关文档：\n\n");
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                sb.append("### ").append(i + 1).append(". ");
                sb.append(doc.getMetadata().getOrDefault("title", "无标题")).append("\n");
                sb.append(doc.getContent().substring(0, Math.min(200, doc.getContent().length())));
                if (doc.getContent().length() > 200) {
                    sb.append("...");
                }
                sb.append("\n\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            log.error("文档搜索失败", e);
            return "文档搜索失败: " + e.getMessage();
        }
    }
}
