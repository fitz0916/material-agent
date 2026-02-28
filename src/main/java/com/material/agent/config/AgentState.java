package com.material.agent.config;

public enum AgentState {
    INTENT_RECOGNIZED,   // 意图识别完成
    PARAM_EXTRACTED,      // 参数提取完成
    DATA_QUERY,          // 数据查询中
    RESULT_VERIFIED,     // 结果验证完成
    RESPONSE_GENERATED,  // 响应已生成
    ERROR                // 错误
}
