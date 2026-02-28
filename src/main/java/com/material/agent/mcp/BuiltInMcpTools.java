package com.material.agent.mcp;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置 MCP 工具
 */
@Component
public class BuiltInMcpTools {

    public static class MaterialQueryMcpTool implements McpTool {
        @Override
        public String getName() { return "material_query"; }
        
        @Override
        public String getDescription() { return "根据编码查询物资信息"; }
        
        @Override
        public String execute(Map<String, Object> params) {
            String code = (String) params.get("material_code");
            return "查询物资: " + code;
        }
    }

    public static class DocumentSearchMcpTool implements McpTool {
        @Override
        public String getName() { return "document_search"; }
        
        @Override
        public String getDescription() { return "搜索技术文档"; }
        
        @Override
        public String execute(Map<String, Object> params) {
            String keyword = (String) params.get("keyword");
            return "搜索文档: " + keyword;
        }
    }

    public static class StockAnalysisMcpTool implements McpTool {
        @Override
        public String getName() { return "stock_analysis"; }
        
        @Override
        public String getDescription() { return "分析库存数据"; }
        
        @Override
        public String execute(Map<String, Object> params) {
            return "库存分析完成";
        }
    }
}
