package com.material.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DocumentSearchTool {
    
    /**
     * 搜索相关文档
     */
    public String searchDocument(String keyword) {
        // TODO: 实现向量检索
        log.info("搜索文档: {}", keyword);
        return "文档检索功能开发中...";
    }
}
