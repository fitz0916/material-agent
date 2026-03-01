package com.material.agent.service;

import com.material.agent.tool.MaterialQueryTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MaterialQueryTool 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MaterialQueryToolTest {

    @Mock
    private MaterialRepository materialRepository;

    private MaterialQueryTool materialQueryTool;

    @BeforeEach
    void setUp() {
        materialQueryTool = new MaterialQueryTool(materialRepository);
    }

    @Test
    void testGetName() {
        assertEquals("material_query", materialQueryTool.getName());
    }

    @Test
    void testGetDescription() {
        String desc = materialQueryTool.getDescription();
        assertNotNull(desc);
        assertTrue(desc.contains("materialCode"));
    }

    @Test
    void testExecute_WithNullParams() {
        String result = (String) materialQueryTool.execute(null);
        assertNotNull(result);
    }

    @Test
    void testExecute_WithEmptyParams() {
        String result = (String) materialQueryTool.execute(java.util.Map.of());
        assertNotNull(result);
    }

    @Test
    void testIsValidCode() {
        // 通过反射测试私有方法
        assertDoesNotThrow(() -> {
            var method = MaterialQueryTool.class.getDeclaredMethod("isValidCode", String.class);
            method.setAccessible(true);
            
            // 测试有效编码
            assertEquals(true, method.invoke(materialQueryTool, "AB-1234-XY"));
            assertEquals(true, method.invoke(materialQueryTool, "SP-2024-A1"));
            
            // 测试无效编码
            assertEquals(false, method.invoke(materialQueryTool, ""));
            assertEquals(false, method.invoke(materialQueryTool, "invalid"));
            assertEquals(false, method.invoke(materialQueryTool, null));
        });
    }
}
