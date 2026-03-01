package com.material.agent.tool;

import com.material.agent.model.Material;
import com.material.agent.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MaterialQueryTool implements Tool {
    
    private final MaterialRepository materialRepository;
    
    public MaterialQueryTool(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }
    
    @Override
    public String getName() {
        return "material_query";
    }
    
    @Override
    public String getDescription() {
        return "根据物资编码精确查询物资详情，或根据关键词搜索物资，也可查询库存低于安全库存的物资。输入：{materialCode: 物资编码, keyword: 搜索关键词, lowStock: 是否查询低库存}";
    }
    
    @Override
    public Object execute(Object params) {
        if (params == null) {
            return "请提供查询参数";
        }
        
        Map<String, Object> p = (Map<String, Object>) params;
        
        // 优先查询低库存
        if (Boolean.TRUE.equals(p.get("lowStock"))) {
            return getLowStockMaterials();
        }
        
        // 根据编码查询
        String materialCode = (String) p.get("materialCode");
        if (materialCode != null && !materialCode.isEmpty()) {
            return queryByCode(materialCode);
        }
        
        // 关键词搜索
        String keyword = (String) p.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            int limit = p.get("limit") != null ? (Integer) p.get("limit") : 10;
            return searchByKeyword(keyword, limit);
        }
        
        return "请提供 materialCode、keyword 或 lowStock 参数";
    }
    
    /**
     * 根据物资编码精确查询物资详情
     */
    public Material queryByCode(String materialCode) {
        if (!isValidCode(materialCode)) {
            throw new IllegalArgumentException("物资编码格式错误");
        }
        
        return materialRepository.findByMaterialCode(materialCode)
            .orElseThrow(() -> new RuntimeException("物资不存在: " + materialCode));
    }
    
    /**
     * 根据关键词搜索物资
     */
    public List<Material> searchByKeyword(String keyword, int limit) {
        return materialRepository.findByKeyword(keyword, 
            org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    /**
     * 查询库存低于安全库存的物资
     */
    public List<Material> getLowStockMaterials() {
        // 获取所有物资，然后过滤出低库存的
        // 注意：实际生产环境应该用数据库查询优化
        return materialRepository.findAll().stream()
            .filter(m -> m.getCurrentStock() != null && m.getSafetyStock() != null
                && m.getCurrentStock() <= m.getSafetyStock())
            .toList();
    }
    
    private boolean isValidCode(String code) {
        return code != null && code.matches("^[A-Z]{2}-\\d{4}-[A-Z0-9]{2,5}$");
    }
}
