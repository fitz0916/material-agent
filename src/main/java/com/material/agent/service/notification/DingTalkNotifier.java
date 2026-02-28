package com.material.agent.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class DingTalkNotifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String WEBHOOK_URL = System.getenv("DINGTALK_WEBHOOK_URL");

    /**
     * 发送钉钉消息
     */
    public void sendMessage(String title, String content) {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            log.warn("钉钉 webhook 未配置");
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                    "title", title,
                    "text", "### " + title + "\n\n" + content
                )
            );
            restTemplate.postForObject(WEBHOOK_URL, body, String.class);
            log.info("钉钉通知发送成功: {}", title);
        } catch (Exception e) {
            log.error("钉钉通知发送失败: {}", e.getMessage());
        }
    }

    /**
     * 发送审批提醒
     */
    public void sendApprovalReminder(String taskId, String content) {
        sendMessage("审批提醒", content + "\n\n任务ID: " + taskId);
    }

    /**
     * 发送预警通知
     */
    public void sendAlert(String message) {
        sendMessage("⚠️ 库存预警", message);
    }
}
