package com.material.agent.controller;

import com.material.agent.model.PurchaseOrder;
import com.material.agent.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 采购订单 API
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@Tag(name = "采购订单", description = "采购订单管理接口")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService);
    }

    @PostMapping
    @Operation(summary = "创建采购订单")
    public ResponseEntity<PurchaseOrder> createOrder(@RequestBody PurchaseOrder order) {
        PurchaseOrder created = purchaseOrderService.createOrder(order);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public ResponseEntity<PurchaseOrder> getOrder(@PathVariable Long id) {
        return purchaseOrderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "获取所有订单")
    public ResponseEntity<List<PurchaseOrder>> getAllOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订单")
    public ResponseEntity<List<PurchaseOrder>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(purchaseOrderService.getUserOrders(userId));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待审批订单")
    public ResponseEntity<List<PurchaseOrder>> getPendingOrders() {
        return ResponseEntity.ok(purchaseOrderService.getPendingOrders());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过")
    public ResponseEntity<PurchaseOrder> approveOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        try {
            PurchaseOrder approved = purchaseOrderService.approveOrder(
                    id,
                    body.get("approverId"),
                    body.get("approverName"),
                    body.get("remark")
            );
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            log.error("审批失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批拒绝")
    public ResponseEntity<PurchaseOrder> rejectOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        try {
            PurchaseOrder rejected = purchaseOrderService.rejectOrder(
                    id,
                    body.get("approverId"),
                    body.get("approverName"),
                    body.get("remark")
            );
            return ResponseEntity.ok(rejected);
        } catch (RuntimeException e) {
            log.error("拒绝失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        try {
            purchaseOrderService.cancelOrder(id, body.get("userId"));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("取消失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取订单统计")
    public ResponseEntity<PurchaseOrderService.OrderStats> getStats() {
        return ResponseEntity.ok(purchaseOrderService.getStats());
    }
}
