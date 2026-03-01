# Material Agent 开发计划

## 目标
完善 Agent 能力 + RAG 增强，生产级实现

## ✅ 已完成 (阶段一、二)

### 1.1 多模型支持 ✅
- [x] ChatModelManager - 多模型抽象
- [x] ChatClientConfig - 多客户端配置
- [x] 模型熔断器（resilience4j）
- [x] 指标收集

### 1.2 Function Calling ✅
- [x] 使用 Spring AI @Tool 注解（已有）
- [x] 工具自动注册

### 1.3 对话历史管理 ✅
- [x] ChatHistoryService
- [x] 内存缓存 + Redis + DB 三级存储
- [x] 历史压缩
- [x] 会话超时

### 1.4 Agent 增强 ✅
- [x] EnhancedReActAgent - 反思 + 超时 + 降级

---

### 2.1 混合检索 ✅
- [x] RRF 融合算法
- [x] 关键词检索（待 Elasticsearch）
- [x] 向量分级过滤

### 2.2 索引管理 ✅
- [x] 文档版本管理（EnhancedRagService）
- [x] 增量更新/删除
- [x] 索引状态监控

### 2.3 RAG 增强 ✅
- [x] BGE Reranker（预留接口）
- [x] 相关性反馈统计

---

## 🔄 待完成 (阶段三)

### 3.1 监控运维
- [ ] Spring Boot Actuator 完整配置
- [ ] 自定义业务指标
- [ ] 链路追踪集成

### 3.2 单元测试
- [ ] 核心服务单元测试
- [ ] 覆盖率目标 > 80%

### 3.3 企业级功能
- [ ] SSO 认证（Spring Security）
- [ ] 多租户支持
- [ ] 审计日志接入
