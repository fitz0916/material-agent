package com.material.agent.service;

import com.material.agent.model.PurchaseOrder;
import com.material.agent.repository.PurchaseOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 采购订单服务
 */
@Slf4j
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final NotificationService notificationService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                               NotificationService notificationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.notificationService = notificationService;
    }

    /**
     * 创建采购订单
     */
    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder order) {
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        order.setStatus("pending");
        
        // 计算总价
        if (order.getUnitPrice() != null && order.getQuantity() != null) {
            order.setTotalPrice(order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        }
        
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        
        // 发送通知给审批人
        notificationService.sendNotification(
                "admin",
                "新采购申请",
                "用户 " + order.getRequesterName() + " 提交了采购申请，请审批",
                "purchase_order",
                saved.getId()
        );
        
        log.info("创建采购订单: {}", saved.getOrderNo());
        return saved;
    }

    /**
     * 审批订单
     */
    @Transactional
    public PurchaseOrder approveOrder(Long orderId, String approverId, String approverName, String remark) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        
        PurchaseOrder order = orderOpt.get();
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许审批: " + order.getStatus());
        }
        
        order.setStatus("approved");
        order.setApproverId(approverId);
        order.setApproverName(approverName);
        order.setApproverName(remark);
        order.setApprovedAt(LocalDateTime.now());
        
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        
        // 通知申请人
        notificationService.sendNotification(
                order.getRequesterId(),
                "采购申请已审批",
                "您的采购申请 " + order.getOrderNo() + " 已通过审批",
                "purchase_order",
                saved.getId()
        );
        
        log.info("审批采购订单: {}", order.getOrderNo());
        return saved;
    }

    /**
     * 拒绝订单
     */
    @Transactional
    public PurchaseOrder rejectOrder(Long orderId, String approverId, String approverName, String remark) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        
        PurchaseOrder order = orderOpt.get();
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许拒绝: " + order.getStatus());
        }
        
        order.setStatus("rejected");
        order.setApproverId(approverId);
        order.setApproverName(approverName);
        order.setRemark(remark);
        order.setApprovedAt(LocalDateTime.now());
        
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        
        // 通知申请人
        notificationService.sendNotification(
                order.getRequesterId(),
                "采购申请被拒绝",
                "您的采购申请 " + order.getOrderNo() + " 未通过审批: " + remark,
                "purchase_order",
                saved.getId()
        );
        
        log.info("拒绝采购订单: {}", order.getOrderNo());
        return saved;
    }

    /**
     * 获取订单详情
     */
    public Optional<PurchaseOrder> getOrder(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    /**
     * 获取用户订单
     */
    public List<PurchaseOrder> getUserOrders(String userId) {
        return purchaseOrderRepository.findByRequesterId(userId);
    }

    /**
     * 获取待审批订单
     */
    public List<PurchaseOrder> getPendingOrders() {
        return purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("pending");
    }

    /**
     * 获取所有订单
     */
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long orderId, String userId) {
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("订单不存在: " + orderId);
        }
        
        PurchaseOrder order = orderOpt.get();
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待审批的订单");
        }
        
        order.setStatus("cancelled");
        purchaseOrderRepository.save(order);
        
        log.info("取消采购订单: {}", order.getOrderNo());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "PO" + LocalDateTime.now().getYear() + 
               String.format("%05d", (int)(Math.random() * 100000));
    }

    /**
     * 获取订单统计
     */
    public OrderStats getStats() {
        long pending = purchaseOrderRepository.countByStatus("pending");
        long approved = purchaseOrderRepository.countByStatus("approved");
        long rejected = purchaseOrderRepository.countByStatus("rejected");
        long total = purchaseOrderRepository.count();
        
        return new OrderStats(total, pending, approved, rejected);
    }

    public record OrderStats(long total, long pending, long approved, long rejected) {}
}
