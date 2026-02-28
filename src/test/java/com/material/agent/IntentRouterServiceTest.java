package com.material.agent;

import com.material.agent.router.Intent;
import com.material.agent.router.IntentRouterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IntentRouterServiceTest {

    @Autowired
    private IntentRouterService intentRouterService;

    @Test
    void testMaterialQueryIntent() {
        Intent intent = intentRouterService.route("SP-2024-X9 这个物资的规格是什么");
        assertEquals(Intent.MATERIAL_QUERY, intent);
    }

    @Test
    void testStockAnalysisIntent() {
        Intent intent = intentRouterService.route("去年哪个物资不合格率最高");
        assertEquals(Intent.STOCK_ANALYSIS, intent);
    }

    @Test
    void testMaterialSelectionIntent() {
        Intent intent = intentRouterService.route("耐高温200度的物资有哪些");
        assertEquals(Intent.MATERIAL_SELECTION, intent);
    }
}
