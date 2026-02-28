package com.material.agent.controller;

import com.material.agent.model.Material;
import com.material.agent.repository.MaterialRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {
    
    private final MaterialRepository materialRepository;
    
    public MaterialController(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Material>> list() {
        return ResponseEntity.ok(materialRepository.findAll());
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<Material> getByCode(@PathVariable String code) {
        return materialRepository.findByMaterialCode(code)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<Material>> getLowStock() {
        List<Material> lowStock = materialRepository.findAll().stream()
            .filter(m -> m.getCurrentStock() != null && m.getSafetyStock() != null
                && m.getCurrentStock() <= m.getSafetyStock())
            .toList();
        return ResponseEntity.ok(lowStock);
    }
}
