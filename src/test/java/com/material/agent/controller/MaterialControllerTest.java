package com.material.agent.controller;

import com.material.agent.model.Material;
import com.material.agent.repository.MaterialRepository;
import com.material.agent.service.MaterialQueryTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MaterialController 单元测试
 */
@WebMvcTest(MaterialController.class)
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialRepository materialRepository;

    @MockBean
    private MaterialQueryTool materialQueryTool;

    @Test
    void testGetAll() throws Exception {
        Material material = new Material();
        material.setId(1L);
        material.setMaterialCode("TEST-001");
        material.setName("测试物资");
        
        when(materialRepository.findAll()).thenReturn(List.of(material));
        
        mockMvc.perform(get("/api/materials/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].materialCode").value("TEST-001"));
    }

    @Test
    void testGetByCode() throws Exception {
        Material material = new Material();
        material.setId(1L);
        material.setMaterialCode("TEST-001");
        
        when(materialRepository.findByMaterialCode("TEST-001")).thenReturn(Optional.of(material));
        
        mockMvc.perform(get("/api/materials/code/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialCode").value("TEST-001"));
    }

    @Test
    void testGetByCodeNotFound() throws Exception {
        when(materialRepository.findByMaterialCode("NOTFOUND")).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/api/materials/code/NOTFOUND"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLowStock() throws Exception {
        Material material = new Material();
        material.setId(1L);
        material.setMaterialCode("LOW-001");
        material.setCurrentStock(5);
        material.setSafetyStock(10);
        
        when(materialQueryTool.getLowStockMaterials()).thenReturn(List.of(material));
        
        mockMvc.perform(get("/api/materials/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].materialCode").value("LOW-001"));
    }

    @Test
    void testGetStats() throws Exception {
        when(materialRepository.findAll()).thenReturn(List.of(
                createMaterial(1L, "A", 5, 10),
                createMaterial(2L, "B", 20, 10)
        ));
        
        mockMvc.perform(get("/api/materials/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.lowStock").value(1));
    }

    private Material createMaterial(Long id, String code, int currentStock, int safetyStock) {
        Material m = new Material();
        m.setId(id);
        m.setMaterialCode(code);
        m.setCurrentStock(currentStock);
        m.setSafetyStock(safetyStock);
        return m;
    }
}
