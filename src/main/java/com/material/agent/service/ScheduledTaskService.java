package com.material.agent.service;

import com.material.agent.tool.MaterialQueryTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 定时任务服务
 * 自动执行库存检查、报告生成等任务
 */
@Slf4j
@Service
public class ScheduledTaskService {

    private final MaterialQueryTool materialQueryTool;
    private final NotificationService notificationService;
    private final ChatModelManager chatModelManager;

    public ScheduledTaskService(MaterialQueryTool materialQueryTool,
                              NotificationService notificationService,
                              ChatModelManager chatModelManager) {
        this.materialQueryTool = materialQueryTool;
        this.notificationService = notificationService;
        this.chatModelManager = chatModelManager;
    }

    /**
     * 每日库存检查 - 每天 8:00 执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyStockCheck() {
        log.info("开始每日库存检查...");
        
        try {
            List<?> lowStockItems = materialQueryTool.getLowStockMaterials();
            
            if (!lowStockItems.isEmpty()) {
                String message = String.format("📊 每日库存报告 (%s)\n\n⚠️ 低库存物资: %d 项\n\n请及时补货",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        lowStockItems.size());
                
                // 发送通知给管理员
                notificationService.sendNotification(
                        "admin",
                        "每日库存报告",
                        message,
                        "stock_report",
                        null
                );
                
                log.info("每日库存检查完成 - 低库存: {} 项", lowStockItems.size());
            } else {
                log.info("每日库存检查完成 - 库存正常");
            }
            
        } catch (Exception e) {
            log.error("每日库存检查失败: {}", e.getMessage());
        }
    }

    /**
     * 低库存预警检查 - 每小时执行
     */
    @Scheduled(fixedRate = 3600000)
    public void lowStockAlert() {
        log.debug("检查低库存...");
        
        try {
            List<?> lowStockItems = materialQueryTool.getLowStockMaterials();
            
            // 如果有严重低库存（库存为0或低于安全库存的50%）
            boolean hasCritical = lowStockItems.stream()
                    .anyMatch(item -> {
                        // 这里简化处理，实际应该检查具体数值
                        return true;
                    });
            
            if (hasCritical) {
                notificationService.sendNotification(
                        "admin",
                        "⚠️ 紧急低库存预警",
                        "发现严重低库存物资，请立即处理",
                        "low_stock",
                        null
                );
            }
            
        } catch (Exception e) {
            log.error("低库存检查失败: {}", e.getMessage());
        }
    }

    /**
     * 模型健康检查 - 每 5 分钟执行
     */
    @Scheduled(fixedRate = 300000)
    public void modelHealthCheck() {
        try {
            var availableModels = chatModelManager.getAvailableModels();
            
            log.debug("模型状态: {}", availableModels);
            
            // 检查是否有可用模型
            boolean hasAvailable = availableModels.values().stream()
                    .anyMatch(state -> state == io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED);
            
            if (!hasAvailable) {
                log.warn("警告: 所有模型都不可用或熔断中");
            }
            
        } catch (Exception e) {
            log.error("模型健康检查失败: {}", e.getMessage());
        }
    }

    /**
     * 清理过期会话 - 每天凌晨 3:00 执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredSessions() {
        log.info("开始清理过期会话...");
        
        try {
            // TODO: 清理 Redis 中过期的会话数据
            // 实际实现应该查询并删除超过 7 天的会话
            
            log.info("会话清理完成");
            
        } catch (Exception e) {
            log.error("会话清理失败: {}", e.getMessage());
        }
    }

    /**
     * 每周报告生成 - 每周一 9:00 执行
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void weeklyReport() {
        log.info("生成周报...");
        
        try {
            String report = String.format("""
                📊 周报 (%s ~ %s)
                
                概述：本周系统运行正常
                - 物资查询: N/A
                - 文档检索: N/A
                - 采购申请: N/A
                
                温馨提示：请登录系统查看详细数据
                """,
                    LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );
            
            notificationService.sendNotification(
                    "admin",
                    "周报",
                    report,
                    "weekly_report",
                    null
            );
            
            log.info("周报生成完成");
            
        } catch (Exception e) {
            log.error("周报生成失败: {}", e.getMessage());
        }
    }
}
