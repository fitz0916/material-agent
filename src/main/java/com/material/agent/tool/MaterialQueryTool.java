package com.material.agent.tool;

import com.material.agent.model.Material;
import com.material.agent.repository.MaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MaterialQueryTool {
    
    private final MaterialRepository materialRepository;
    
    public MaterialQueryTool(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }
    
    /**
     * 根据物资编码精确查询物资详情
     */
    public Material queryByCode(String materialCode) {
        // Schema 校验
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
        return materialRepository.findAll().stream()
            .filter(m -> m.getCurrentStock() != null && m.getSafetyStock() != null
                && m.getCurrentStock() <= m.getSafetyStock())
            .toList();
    }
    
    private boolean isValidCode(String code) {
        return code != null && code.matches("^[A-Z]{2}-\\d{4}-[A-Z0-9]{2,5}$");
    }
}
