package com.material.agent.controller;

import com.material.agent.model.Material;
import com.material.agent.repository.MaterialRepository;
import com.material.agent.service.MaterialQueryTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物资管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/materials")
public class MaterialController {
    
    private final MaterialRepository materialRepository;
    private final MaterialQueryTool materialQueryTool;
    
    public MaterialController(MaterialRepository materialRepository, 
                             MaterialQueryTool materialQueryTool) {
        this.materialRepository = materialRepository;
        this.materialQueryTool = materialQueryTool;
    }
    
    /**
     * 分页获取物资列表
     */
    @GetMapping
    public ResponseEntity<Page<Material>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Material> result;
        
        if (category != null && !category.isEmpty()) {
            result = materialRepository.findByCategory(category, pageable);
        } else if (status != null && !status.isEmpty()) {
            result = materialRepository.findByStatus(status, pageable);
        } else {
            result = materialRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取所有物资（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<List<Material>> getAll() {
        return ResponseEntity.ok(materialRepository.findAll());
    }
    
    /**
     * 根据编码获取物资
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Material> getByCode(@PathVariable String code) {
        return materialRepository.findByMaterialCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据 ID 获取物资
     */
    @GetMapping("/{id}")
    public ResponseEntity<Material> getById(@PathVariable Long id) {
        return materialRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 搜索物资
     */
    @GetMapping("/search")
    public ResponseEntity<List<Material>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Material> result = materialQueryTool.searchByKeyword(keyword, limit);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取低库存物资
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Material>> getLowStock() {
        List<Material> result = materialQueryTool.getLowStockMaterials();
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据分类获取物资
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Material>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(materialRepository.findByCategory(category));
    }
    
    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = materialRepository.findAll()
                .stream()
                .map(Material::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .toList();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 创建物资
     */
    @PostMapping
    public ResponseEntity<Material> create(@RequestBody Material material) {
        Material saved = materialRepository.save(material);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * 更新物资
     */
    @PutMapping("/{id}")
    public ResponseEntity<Material> update(@PathVariable Long id, @RequestBody Material material) {
        if (!materialRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        material.setId(id);
        Material saved = materialRepository.save(material);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * 删除物资
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!materialRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        materialRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 批量导入物资
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchImport(@RequestBody List<Material> materials) {
        List<Material> saved = materialRepository.saveAll(materials);
        return ResponseEntity.ok(Map.of(
                "total", materials.size(),
                "success", saved.size(),
                "failed", materials.size() - saved.size()
        ));
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Material> all = materialRepository.findAll();
        
        long total = all.size();
        long lowStock = all.stream()
                .filter(m -> m.getCurrentStock() != null && m.getSafetyStock() != null
                        && m.getCurrentStock() <= m.getSafetyStock())
                .count();
        
        return ResponseEntity.ok(Map.of(
                "total", total,
                "lowStock", lowStock,
                "normal", total - lowStock
        ));
    }
}
