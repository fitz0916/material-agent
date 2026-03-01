package com.material.agent.mcp;

import com.material.agent.tool.Tool;
import com.material.agent.tool.MaterialQueryTool;
import com.material.agent.tool.DocumentSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP 工具注册中心
 * 将所有 Tool 实现注册为 MCP 工具
 */
@Slf4j
@Component
public class McpToolRegistry {

    private final Map<String, Tool> tools;

    public McpToolRegistry(List<Tool> toolList) {
        this.tools = toolList.stream()
            .collect(Collectors.toMap(Tool::getName, t -> t));
        log.info("注册了 {} 个 MCP 工具: {}", tools.size(), tools.keySet());
    }

    /**
     * 根据名称获取工具
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * 获取所有工具
     */
    public Map<String, Tool> getAllTools() {
        return tools;
    }

    /**
     * 执行工具
     */
    public String execute(String toolName, Object params) {
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return "错误：未找到工具 " + toolName;
        }
        
        try {
            return String.valueOf(tool.execute(params));
        } catch (Exception e) {
            log.error("工具执行失败: {}", toolName, e);
            return "工具执行失败: " + e.getMessage();
        }
    }
}
