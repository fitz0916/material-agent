package com.material.agent.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class FeishuNotifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String WEBHOOK_URL = System.getenv("FEISHU_WEBHOOK_URL");

    /**
     * 发送审批通知
     */
    public void sendApprovalNotification(String taskId, String content, String requester) {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            log.warn("飞书 webhook 未配置");
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                "msg_type", "interactive",
                "card", Map.of(
                    "header", Map.of(
                        "title", Map.of("tag", "plain_text", "content", "审批提醒"),
                        "template", "blue"
                    ),
                    "elements", new Object[]{
                        Map.of("tag", "div", Map.of("", "texttag", "plain_text", "content", content)),
                        Map.of("tag", "div", "fields", new Object[]{
                            Map.of("is_short", true, "text", Map.of("tag", "plain_text", "content", "申请人: " + requester)),
                            Map.of("is_short", true, "text", Map.of("tag", "plain_text", "content", "任务ID: " + taskId))
                        }),
                        Map.of("tag", "action", "actions", new Object[]{
                            Map.of("tag", "button", "text", Map.of("tag", "plain_text", "content", "审批"), 
                                   "type", "primary", "url", "https://your-approval-page.com/" + taskId)
                        })
                    }
                )
            );

            restTemplate.postForObject(WEBHOOK_URL, body, String.class);
            log.info("飞书通知发送成功: {}", taskId);
        } catch (Exception e) {
            log.error("飞书通知发送失败: {}", e.getMessage());
        }
    }

    /**
     * 发送预警通知
     */
    public void sendAlert(String title, String message) {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                "msg_type", "text",
                "content", Map.of("text", "【" + title + "】" + message)
            );
            restTemplate.postForObject(WEBHOOK_URL, body, String.class);
        } catch (Exception e) {
            log.error("预警通知发送失败: {}", e.getMessage());
        }
    }
}
