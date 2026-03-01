package com.material.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatModelManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ChatModelManagerTest {

    @Mock
    private org.springframework.ai.chat.client.ChatClient defaultClient;

    private ChatModelManager chatModelManager;

    @BeforeEach
    void setUp() {
        // 简化测试
        // 实际应该 mock 多个 ChatClient
    }

    @Test
    void testGetCurrentModel() {
        // TODO: 完善测试
    }

    @Test
    void testSwitchModel() {
        // TODO: 完善测试
    }
}
