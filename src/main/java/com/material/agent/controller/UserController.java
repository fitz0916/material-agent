package com.material.agent.controller;

import com.material.agent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理 API
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息")
    public ResponseEntity<UserService.User> getUser(@PathVariable String userId) {
        return userService.getUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "获取所有用户")
    public ResponseEntity<List<UserService.User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ResponseEntity<UserService.User> createUser(@RequestBody UserService.User user) {
        try {
            UserService.User created = userService.createUser(user);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户")
    public ResponseEntity<UserService.User> updateUser(
            @PathVariable String userId, 
            @RequestBody UserService.User user) {
        try {
            UserService.User updated = userService.updateUser(userId, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("更新用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/enable")
    @Operation(summary = "启用用户")
    public ResponseEntity<Void> enableUser(@PathVariable String userId) {
        userService.setUserEnabled(userId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/disable")
    @Operation(summary = "禁用用户")
    public ResponseEntity<Void> disableUser(@PathVariable String userId) {
        userService.setUserEnabled(userId, false);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/validate")
    @Operation(summary = "验证用户")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.validateUser(userId));
    }
}
