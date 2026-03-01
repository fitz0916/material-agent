package com.material.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * 系统配置服务
 * 动态配置管理
 */
@Slf4j
@Service
public class ConfigService {

    private final StringRedisTemplate redisTemplate;
    private static final String CONFIG_PREFIX = "config:";
    private static final Duration EXPIRE = Duration.ofDays(30);

    public ConfigService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取配置
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(CONFIG_PREFIX + key);
    }

    /**
     * 获取配置（带默认值）
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数配置
     */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 设置配置
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(CONFIG_PREFIX + key, value, EXPIRE);
        log.info("配置已更新: {} = {}", key, value);
    }

    /**
     * 设置配置（带过期时间）
     */
    public void set(String key, String value, Duration expire) {
        redisTemplate.opsForValue().set(CONFIG_PREFIX + key, value, expire);
    }

    /**
     * 删除配置
     */
    public void delete(String key) {
        redisTemplate.delete(CONFIG_PREFIX + key);
    }

    /**
     * 获取所有配置
     */
    public Map<String, String> getAll() {
        Set<String> keys = redisTemplate.keys(CONFIG_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }
        
        return redisTemplate.opsForValue().multiGet(keys)
                .stream()
                .reduce((k, v) -> {})
                .orElse(Map.of());
    }

    /**
     * 检查配置是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CONFIG_PREFIX + key));
    }

    // ==================== 业务配置 ====================

    /**
     * 获取当前 AI 模型
     */
    public String getCurrentModel() {
        return get("ai.model", "kimi");
    }

    /**
     * 设置 AI 模型
     */
    public void setCurrentModel(String model) {
        set("ai.model", model);
    }

    /**
     * 获取系统开关
     */
    public boolean isFeatureEnabled(String feature) {
        return getBoolean("feature." + feature, false);
    }

    /**
     * 启用/禁用功能
     */
    public void setFeatureEnabled(String feature, boolean enabled) {
        set("feature." + feature, String.valueOf(enabled));
    }
}
