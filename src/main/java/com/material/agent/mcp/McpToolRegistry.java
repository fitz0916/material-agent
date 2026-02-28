package com.material.agent.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP (Model Context Protocol) 工具注册中心
 */
@Slf4j
@Component
public class McpToolRegistry {

    private final Map<String, McpTool> tools = new ConcurrentHashMap<>();

    /**
     * 注册工具
     */
    public void register(McpTool tool) {
        tools.put(tool.getName(), tool);
        log.info("MCP 工具注册: {} - {}", tool.getName(), tool.getDescription());
    }

    /**
     * 获取工具
     */
    public McpTool get(String name) {
        return tools.get(name);
    }

    /**
     * 列出所有工具
     */
    public List<McpTool> listAll() {
        return new ArrayList<>(tools.values());
    }

    /**
     * 执行工具
     */
    public String execute(String name, Map<String, Object> params) {
        McpTool tool = tools.get(name);
        if (tool == null) {
            return "工具不存在: " + name;
        }
        
        try {
            return tool.execute(params);
        } catch (Exception e) {
            log.error("工具执行失败: {}", name, e);
            return "执行失败: " + e.getMessage();
        }
    }
}
