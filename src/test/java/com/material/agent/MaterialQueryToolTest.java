package com.material.agent;

import com.material.agent.model.Material;
import com.material.agent.tool.MaterialQueryTool;
import com.material.agent.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialQueryToolTest {

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private MaterialQueryTool materialQueryTool;

    @Test
    void testQueryByCode_Success() {
        Material material = new Material();
        material.setMaterialCode("SP-2024-X9");
        material.setName("测试物资");
        
        when(materialRepository.findByMaterialCode("SP-2024-X9"))
            .thenReturn(Optional.of(material));

        Material result = materialQueryTool.queryByCode("SP-2024-X9");
        
        assertNotNull(result);
        assertEquals("SP-2024-X9", result.getMaterialCode());
    }

    @Test
    void testQueryByCode_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            materialQueryTool.queryByCode("invalid-code");
        });
    }

    @Test
    void testQueryByCode_NotFound() {
        when(materialRepository.findByMaterialCode("SP-9999-X9"))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            materialQueryTool.queryByCode("SP-9999-X9");
        });
    }
}
