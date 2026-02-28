package com.material.agent.mcp;

import java.util.Map;

/**
 * MCP 工具接口
 */
public interface McpTool {
    String getName();
    String getDescription();
    String execute(Map<String, Object> params);
}
