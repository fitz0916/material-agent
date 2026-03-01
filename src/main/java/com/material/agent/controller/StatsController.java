package com.material.agent.controller;

import com.material.agent.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计 API
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@Tag(name = "数据统计", description = "系统数据统计接口")
public class StatsController {

    private final MaterialQueryTool materialQueryTool;
    private final DocumentService documentService;
    private final PurchaseOrderService purchaseOrderService;
    private final ChatHistoryService chatHistoryService;
    private final ChatModelManager chatModelManager;

    public StatsController(MaterialQueryTool materialQueryTool,
                          DocumentService documentService,
                          PurchaseOrderService purchaseOrderService,
                          ChatHistoryService chatHistoryService,
                          ChatModelManager chatModelManager) {
        this.materialQueryTool = materialQueryTool;
        this.documentService = documentService;
        this.purchaseOrderService = purchaseOrderService;
        this.chatHistoryService = chatHistoryService;
        this.chatModelManager = chatModelManager;
    }

    @GetMapping("/overview")
    @Operation(summary = "获取总览统计")
    public ResponseEntity<Map<String, Object>> getOverview() {
        try {
            var lowStock = materialQueryTool.getLowStockMaterials();
            
            return ResponseEntity.ok(Map.of(
                    "lowStockCount", lowStock.size(),
                    "currentModel", chatModelManager.getCurrentModel(),
                    "availableModels", chatModelManager.getAvailableModels().keySet().size()
            ));
        } catch (Exception e) {
            log.error("获取统计失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/materials")
    @Operation(summary = "物资统计")
    public ResponseEntity<Map<String, Object>> getMaterialStats() {
        try {
            var lowStock = materialQueryTool.getLowStockMaterials();
            
            return ResponseEntity.ok(Map.of(
                    "lowStockCount", lowStock.size(),
                    "lowStockItems", lowStock.stream().limit(10).toList()
            ));
        } catch (Exception e) {
            log.error("获取物资统计失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/documents")
    @Operation(summary = "文档统计")
    public ResponseEntity<Map<String, Object>> getDocumentStats() {
        var stats = documentService.getStats();
        return ResponseEntity.ok(Map.of(
                "total", stats.total(),
                "indexed", stats.indexed(),
                "pending", stats.pending(),
                "failed", stats.failed()
        ));
    }

    @GetMapping("/orders")
    @Operation(summary = "订单统计")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        var stats = purchaseOrderService.getStats();
        return ResponseEntity.ok(Map.of(
                "total", stats.total(),
                "pending", stats.pending(),
                "approved", stats.approved(),
                "rejected", stats.rejected()
        ));
    }

    @GetMapping("/models")
    @Operation(summary = "模型状态")
    public ResponseEntity<Map<String, Object>> getModelStats() {
        return ResponseEntity.ok(Map.of(
                "current", chatModelManager.getCurrentModel(),
                "available", chatModelManager.getAvailableModels()
        ));
    }
}
