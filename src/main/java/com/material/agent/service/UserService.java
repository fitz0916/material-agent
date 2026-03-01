package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户服务
 * 简单的用户管理（实际生产应该用数据库）
 */
@Slf4j
@Service
public class UserService {

    // 模拟用户数据
    private final Map<String, User> users = new HashMap<>();

    public UserService() {
        // 初始化默认管理员
        User admin = new User();
        admin.setId("admin");
        admin.setName("管理员");
        admin.setRole("ADMIN");
        admin.setEmail("admin@company.com");
        admin.setEnabled(true);
        users.put("admin", admin);

        // 初始化测试用户
        User testUser = new User();
        testUser.setId("user001");
        testUser.setName("测试用户");
        testUser.setRole("USER");
        testUser.setEmail("test@company.com");
        testUser.setEnabled(true);
        users.put("user001", testUser);
    }

    /**
     * 根据 ID 获取用户
     */
    public Optional<User> getUser(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * 创建用户
     */
    public User createUser(User user) {
        if (users.containsKey(user.getId())) {
            throw new RuntimeException("用户已存在: " + user.getId());
        }
        user.setEnabled(true);
        users.put(user.getId(), user);
        log.info("创建用户: {}", user.getId());
        return user;
    }

    /**
     * 更新用户
     */
    public User updateUser(String userId, User user) {
        User existing = users.get(userId);
        if (existing == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        
        if (user.getName() != null) existing.setName(user.getName());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getRole() != null) existing.setRole(user.getRole());
        
        users.put(userId, existing);
        log.info("更新用户: {}", userId);
        return existing;
    }

    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        if (!users.containsKey(userId)) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        users.remove(userId);
        log.info("删除用户: {}", userId);
    }

    /**
     * 启用/禁用用户
     */
    public void setUserEnabled(String userId, boolean enabled) {
        User user = users.get(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        user.setEnabled(enabled);
        log.info("{} 用户: {}", enabled ? "启用" : "禁用", userId);
    }

    /**
     * 验证用户
     */
    public boolean validateUser(String userId) {
        User user = users.get(userId);
        return user != null && user.isEnabled();
    }

    /**
     * 检查角色权限
     */
    public boolean hasRole(String userId, String role) {
        User user = users.get(userId);
        return user != null && role.equals(user.getRole());
    }

    /**
     * 用户模型
     */
    public static class User {
        private String id;
        private String name;
        private String email;
        private String role;
        private String department;
        private String phone;
        private boolean enabled = true;
        private Date createdAt = new Date();

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    }
}
