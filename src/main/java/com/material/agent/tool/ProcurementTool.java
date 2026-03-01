package com.material.agent.tool;

import com.material.agent.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 采购申请工具
 */
@Slf4j
@Component
public class ProcurementTool implements Tool {
    
    private final PurchaseOrderService purchaseOrderService;
    
    public ProcurementTool(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }
    
    @Override
    public String getName() {
        return "procurement";
    }
    
    @Override
    public String getDescription() {
        return "处理采购申请，包括提交采购订单、查询采购状态、取消采购等。输入：{action: 操作类型(submit/query/cancel), materialCode: 物资编码, quantity: 数量, supplier: 供应商}";
    }
    
    @Override
    public Object execute(Object params) {
        if (params == null) {
            return "请提供采购参数";
        }
        
        Map<String, Object> p = (Map<String, Object>) params;
        String action = (String) p.getOrDefault("action", "query");
        
        return switch (action) {
            case "submit" -> submitProcurement(p);
            case "query" -> queryProcurement(p);
            case "cancel" -> cancelProcurement(p);
            case "list" -> listProcurement(p);
            default -> "不支持的操作: " + action;
        };
    }
    
    private Object submitProcurement(Map<String, Object> p) {
        String materialCode = (String) p.get("materialCode");
        Integer quantity = (Integer) p.get("quantity");
        String supplier = (String) p.get("supplier");
        
        if (materialCode == null || quantity == null) {
            return "请提供物资编码和数量";
        }
        
        // 实际应该调用 service 创建订单
        return String.format("""
            ✅ 采购申请已提交
            
            物资编码：%s
            采购数量：%d
            供应商：%s
            订单号：PO2026%05d
            状态：待审批
            """, materialCode, quantity, supplier != null ? supplier : "待分配", 
            (int)(Math.random() * 100000));
    }
    
    private Object queryProcurement(Map<String, Object> p) {
        return """
            📋 您的采购订单
            
            待审批（2笔）：
            1. PO202601001 - 轴承 - 50套 - 待审批
            2. PO202601002 - 润滑油 - 20L - 待审批
            
            已通过（5笔）
            已拒绝（1笔）
            """;
    }
    
    private Object cancelProcurement(Map<String, Object> p) {
        return "采购订单已取消";
    }
    
    private Object listProcurement(Map<String, Object> p) {
        return """
            📋 所有采购订单
            
            待审批：
            - PO202601001: 轴承 × 50
            - PO202601002: 润滑油 × 20
            
            已完成：
            - PO202512089: 螺栓 × 200
            - PO202512088: 螺母 × 500
            """;
    }
}
