package com.material.agent.controller;

import com.material.agent.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/approvals")
@Tag(name = "审批管理", description = "审批任务管理接口")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping
    @Operation(summary = "创建审批任务")
    public ResponseEntity<ApprovalService.ApprovalTask> create(@RequestBody ApprovalService.ApprovalTask task) {
        ApprovalService.ApprovalTask created = approvalService.createApprovalTask(task);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取审批任务详情")
    public ResponseEntity<ApprovalService.ApprovalTask> get(@PathVariable String taskId) {
        ApprovalService.ApprovalTask task = approvalService.getTask(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待审批任务")
    public ResponseEntity<List<ApprovalService.ApprovalTask>> getPending(
            @RequestParam String approverId) {
        return ResponseEntity.ok(approvalService.getPendingTasks(approverId));
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的申请")
    public ResponseEntity<List<ApprovalService.ApprovalTask>> getMyApplications(
            @RequestParam String userId) {
        return ResponseEntity.ok(approvalService.getUserTasks(userId));
    }

    @PostMapping("/{taskId}/approve")
    @Operation(summary = "审批通过")
    public ResponseEntity<ApprovalService.ApprovalTask> approve(
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        
        try {
            ApprovalService.ApprovalTask task = approvalService.approve(
                    taskId,
                    body.get("approverId"),
                    body.get("approverName"),
                    body.get("comment")
            );
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            log.error("审批失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}/reject")
    @Operation(summary = "审批拒绝")
    public ResponseEntity<ApprovalService.ApprovalTask> reject(
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        
        try {
            ApprovalService.ApprovalTask task = approvalService.reject(
                    taskId,
                    body.get("approverId"),
                    body.get("approverName"),
                    body.get("reason")
            );
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            log.error("审批拒绝失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}/withdraw")
    @Operation(summary = "撤回申请")
    public ResponseEntity<Void> withdraw(
            @PathVariable String taskId,
            @RequestBody Map<String, String> body) {
        
        try {
            approvalService.withdraw(taskId, body.get("userId"));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("撤回失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
