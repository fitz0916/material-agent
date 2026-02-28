package com.material.agent.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * ReAct (Reasoning + Acting) 自主规划智能体
 */
@Slf4j
@Component
public class ReActAgent {

    private final Map<String, Object> tools;

    public ReActAgent(List<com.material.agent.tool.Tool> tools) {
        this.tools = Map.of(
            "material_query", "MaterialQueryTool",
            "document_search", "DocumentSearchTool"
        );
    }

    /**
     * ReAct 循环执行
     */
    public String think(String input, String context) {
        String current = input;
        int maxIterations = 5;
        
        for (int i = 0; i < maxIterations; i++) {
            String thought = reason(current, context);
            log.debug("Thought #{}: {}", i + 1, thought);
            
            if (isFinalAnswer(thought)) {
                return extractAnswer(thought);
            }
            
            String action = chooseAction(thought);
            String observation = execute(action);
            log.debug("Action: {}, Observation: {}", action, observation);
            
            current = current + "\nThought: " + thought + "\nAction: " + action + "\nObservation: " + observation;
        }
        
        return "已达到最大迭代次数";
    }

    private String reason(String input, String context) {
        return "Reasoning: 需要查询物资信息";
    }

    private boolean isFinalAnswer(String thought) {
        return thought.contains("FINAL_ANSWER");
    }

    private String extractAnswer(String thought) {
        return thought.replaceAll(".*FINAL_ANSWER[:：]?", "").trim();
    }

    private String chooseAction(String thought) {
        if (thought.contains("查询物资")) return "material_query";
        if (thought.contains("搜索文档")) return "document_search";
        return "finish";
    }

    private String execute(String action) {
        if (action.equals("finish")) return "任务完成";
        return "Executed: " + action;
    }
}
