package com.material.agent.controller;

import com.material.agent.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知 API
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "通知管理", description = "通知查询和操作接口")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "获取用户通知")
    public ResponseEntity<List<com.material.agent.model.Notification>> getUserNotifications(
            @RequestParam String userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/unread")
    @Operation(summary = "获取未读通知")
    public ResponseEntity<List<com.material.agent.model.Notification>> getUnreadNotifications(
            @RequestParam String userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读数量")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam String userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public ResponseEntity<Void> markAllAsRead(@RequestParam String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    @Operation(summary = "删除已读通知")
    public ResponseEntity<Void> deleteReadNotifications(@RequestParam String userId) {
        notificationService.deleteReadNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}
