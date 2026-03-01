package com.material.agent.service;

import com.material.agent.model.Notification;
import com.material.agent.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FeishuNotifier feishuNotifier;
    private final DingTalkNotifier dingTalkNotifier;

    public NotificationService(NotificationRepository notificationRepository,
                              FeishuNotifier feishuNotifier,
                              DingTalkNotifier dingTalkNotifier) {
        this.notificationRepository = notificationRepository;
        this.feishuNotifier = feishuNotifier;
        this.dingTalkNotifier = dingTalkNotifier;
    }

    /**
     * 发送系统通知
     */
    @Transactional
    public void sendNotification(String userId, String title, String content, String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("info");
        notification.setChannel("system");
        notification.setStatus("unread");
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        
        notificationRepository.save(notification);
        
        // 尝试发送外部通知
        trySendExternalNotification(userId, title, content);
        
        log.info("发送通知给用户 {}: {}", userId, title);
    }

    /**
     * 发送外部通知（飞书/钉钉）
     */
    private void trySendExternalNotification(String userId, String title, String content) {
        try {
            // 根据用户配置选择通知渠道
            // 这里简化处理，实际应该查询用户配置
            feishuNotifier.sendAlert(title, content);
        } catch (Exception e) {
            log.warn("飞书通知发送失败: {}", e.getMessage());
        }
    }

    /**
     * 获取用户通知列表
     */
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取用户未读通知
     */
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "unread");
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndStatus(userId, "unread");
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setStatus("read");
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, "unread");
        
        unreadNotifications.forEach(notification -> {
            notification.setStatus("read");
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
        
        log.info("标记用户 {} 的所有通知为已读", userId);
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * 批量删除已读通知
     */
    @Transactional
    public void deleteReadNotifications(String userId) {
        List<Notification> readNotifications = notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, "read");
        
        notificationRepository.deleteAll(readNotifications);
        
        log.info("删除用户 {} 的已读通知", userId);
    }

    /**
     * 发送低库存预警通知
     */
    public void sendLowStockAlert(String materialCode, String materialName, int currentStock, int safetyStock) {
        String title = "⚠️ 低库存预警";
        String content = String.format("物资 %s (%s) 当前库存 %d，低于安全库存 %d", 
                materialCode, materialName, currentStock, safetyStock);
        
        // 发送给管理员
        sendNotification("admin", title, content, "low_stock", null);
    }

    /**
     * 发送采购完成通知
     */
    public void sendPurchaseComplete(String userId, String orderNo) {
        String title = "采购完成";
        String content = "您的采购订单 " + orderNo + " 已完成采购";
        
        sendNotification(userId, title, content, "purchase_order", null);
    }
}
