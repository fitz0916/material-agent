package com.material.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 物资选型推荐工具
 */
@Slf4j
@Component
public class MaterialSelectionTool implements Tool {
    
    @Override
    public String getName() {
        return "material_selection";
    }
    
    @Override
    public String getDescription() {
        return "根据需求推荐合适的物资，支持参数匹配、规格筛选、替代推荐。输入：{requirements: 需求描述, conditions: 条件Map}";
    }
    
    @Override
    public Object execute(Object params) {
        if (params == null) {
            return "请提供选型参数";
        }
        
        Map<String, Object> p = (Map<String, Object>) params;
        String requirements = (String) p.get("requirements");
        
        // 简单关键词匹配推荐
        return recommendMaterials(requirements);
    }
    
    private String recommendMaterials(String requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "请提供选型需求";
        }
        
        String lower = requirements.toLowerCase();
        
        if (lower.contains("耐高温") || lower.contains("高温")) {
            return """
                🔥 高温物资推荐
                
                | 编码 | 名称 | 耐温 | 库存 |
                |------|------|------|------|
                | GL-2024-01 | 高温密封圈 | 300℃ | 50 |
                | GL-2024-02 | 耐热垫片 | 250℃ | 100 |
                | GL-2024-03 | 隔热套管 | 200℃ | 80 |
                """;
        }
        
        if (lower.contains("耐腐蚀") || lower.contains("防腐")) {
            return """
                🛡️ 防腐物资推荐
                
                | 编码 | 名称 | 耐腐蚀等级 | 库存 |
                |------|------|-----------|------|
                | FF-2024-01 | 防腐涂料 | IP65 | 200kg |
                | FF-2024-02 | 不锈钢螺栓 | 304 | 1000 |
                | FF-2024-03 | PVC管材 | 耐酸碱 | 500m |
                """;
        }
        
        if (lower.contains("润滑") || lower.contains("油")) {
            return """
                🛢️ 润滑物资推荐
                
                | 编码 | 名称 | 粘度 | 库存 |
                |------|------|------|------|
                | WL-2024-K3 | 昆仑润滑油 | ISO 68 | 100L |
                | WL-2024-K5 | 长城润滑脂 | NLGI 2 | 50kg |
                | WL-2024-01 | 食品级润滑油 | NSF H1 | 30L |
                """;
        }
        
        return """
            📋 推荐物资列表
            
            根据您的需求，建议联系专业人员获取详细选型支持。
            
            您可以提供更多参数：
            - 使用环境（温度、压力、介质）
            - 材质要求
            - 尺寸规格
            - 数量需求
            """;
    }
}
