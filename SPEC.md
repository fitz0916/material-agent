# 确定性增强型工业物资智能 Agent 规范文档

> 版本：1.0.0
> 状态：草稿
> 日期：2026-02-28

---

## 1. 项目概述

### 1.1 项目名称
**Material-Agent** - 确定性增强型工业物资智能分析 Agent

### 1.2 项目定位
基于 Spring AI 框架开发的智能物资管理分析 Agent，通过三层工程外壳（意图路由、工具约束、状态机）确保输出的确定性和可控性。

### 1.3 核心功能
- 物资智能查询（RAG + SQL）
- 库存数据分析
- 物资选型推荐
- 文档智能理解
- 人在回路审核

---

## 2. 技术规范

### 2.1 技术栈

| 类别 | 技术选型 | 版本 |
|------|----------|------|
| Java 运行时 | JDK | 21+ |
| 框架 | Spring Boot | 3.3+ |
| AI 框架 | Spring AI | 1.0+ |
| 关系数据库 | PostgreSQL | 15+ |
| 向量数据库 | PGVector | - |
| 缓存 | Redis | 7+ |
| 文件存储 | MinIO | - |
| 状态机 | Spring StateMachine | 4.0+ |

### 2.2 大模型支持

| 模型 | 用途 | API |
|------|------|-----|
| Kimi | 长文本分析 | Anthropic API |
| 智谱 | 通用对话 | OpenAI API |
| MiniMax | 多模态 | Anthropic API |
| Claude | 复杂推理 | Anthropic API |

---

## 3. 架构规范

### 3.1 三层工程外壳

```
用户输入 → 意图路由层 → 工具约束层 → 状态机层 → 数据层
```

#### 3.1.1 意图路由层 (Intent Router)
- 功能：轻量模型判断用户意图
- 输出：Intent 枚举（MATERIAL_QUERY, STOCK_ANALYSIS, MATERIAL_SELECTION, DOCUMENT_SEARCH, PROCUREMENT, CHAT）
- 路由：根据意图分发到不同处理链路

#### 3.1.2 工具约束层 (Tool Constraint)
- 功能：所有数据库操作通过预定义 Tool
- 约束：JSON Schema 校验 + 后端二次验证
- 禁止：模型直接生成 SQL

#### 3.1.3 状态机层 (State Machine)
- 功能：定义业务流转
- 状态：INTENT_RECOGNIZED → PARAM_EXTRACTED → DATA_QUERY → RESULT_VERIFIED → RESPONSE_GENERATED
- 控制：模型只负责填充参数，不决定下一步

### 3.2 模块划分

```
material-agent/
├── config/           # 配置层
├── router/          # 意图路由
├── advisor/         # 拦截器（验证、审计、权限）
├── tool/            # 工具层
├── service/        # 业务服务
├── repository/     # 数据访问
├── model/          # 实体
└── controller/    # 接口
```

---

## 4. 功能规范

### 4.1 核心功能列表

#### 4.1.1 物资查询 (Material Query)
- 按编码查询
- 关键词搜索
- 分类统计

#### 4.1.2 库存分析 (Stock Analysis)
- 库存量查询
- 消耗趋势分析
- 安全库存预警

#### 4.1.3 物资选型 (Material Selection)
- 参数匹配选型
- 替代品推荐
- 性能差异对比

#### 4.1.4 文档理解 (Document Understanding)
- MinIO 文件读取
- OCR 识别
- 关键信息提取

#### 4.1.5 人在回路 (Human-in-Loop)
- 审批任务创建
- 审批状态管理
- 通知推送

### 4.2 数据模型

#### 4.2.1 物资表 (materials)
```sql
- id: BIGINT (PK)
- material_code: VARCHAR(50) (UNIQUE)
- name: VARCHAR(200)
- category: VARCHAR(50)
- specification: JSONB
- unit: VARCHAR(20)
- unit_price: DECIMAL(18,2)
- safety_stock: INT
- current_stock: INT
- supplier: VARCHAR(200)
- status: VARCHAR(20)
- created_at: TIMESTAMP
- updated_at: TIMESTAMP
```

#### 4.2.2 向量表 (material_embeddings)
```sql
- id: BIGINT (PK)
- material_id: BIGINT (FK)
- chunk_text: TEXT
- embedding: vector(1536)
- source: VARCHAR(100)
- file_path: VARCHAR(500)
- created_at: TIMESTAMP
```

#### 4.2.3 对话历史表 (chat_sessions)
```sql
- id: BIGINT (PK)
- session_id: VARCHAR(100)
- user_id: VARCHAR(100)
- role: VARCHAR(20)
- content: TEXT
- model: VARCHAR(50)
- tokens_used: INT
- tool_calls: JSONB
- created_at: TIMESTAMP
```

#### 4.2.4 审计日志表 (agent_audit_log)
```sql
- id: BIGINT (PK)
- session_id: VARCHAR(100)
- user_id: VARCHAR(100)
- action_type: VARCHAR(50)
- input_summary: JSONB
- output_summary: JSONB
- model: VARCHAR(50)
- duration_ms: INT
- created_at: TIMESTAMP
```

---

## 5. 接口规范

### 5 API

| 方法.1 REST | 路径 | 功能 |
|------|------|------|
| POST | /api/agent/chat | 对话入口 |
| GET | /api/agent/session/{id} | 获取| POST | /会话 |
api/agent/approve | 审批操作 |
| GET | /api/materials | 物资列表 |
| GET | /api/materials/{code} | 物资详情 |
| POST | /api/analysis/stock | 库存分析 |
| POST | /api/etl/ingest | 文档入库 |

### 5.2 请求响应格式

#### 5.2.1 ChatRequest
```json
{
  "message": "用户消息",
  "sessionId": "会话ID",
  "userId": "用户ID"
}
```

#### 5.2.2 ChatResponse
```json
{
  "message": "Agent 回复",
  "sessionId": "会话ID",
  "intent": "识别的意图",
  "tokens": "使用 token 数"
}
```

---

## 6. 安全规范

### 6.1 权限控制
- 数据分级访问
- Tenant_ID 过滤
- 角色权限校验

### 6.2 审计日志
- 记录所有操作
- 保留决策过程
- 可追溯查询

### 6.3 异常处理
- 统一异常捕获
- 友好错误提示
- 详细日志记录

---

## 7. 部署规范

### 7.1 Docker 部署
- 多阶段构建
- 非 root 用户运行
- 健康检查配置

### 7.2 环境配置
- 配置外部化
- 敏感信息加密
- 多环境支持（dev/test/prod）

---

## 8. 验收标准

### 8.1 功能验收
- [ ] 意图路由准确率 > 90%
- [ ] 物资查询响应时间 < 2s
- [ ] RAG 检索相关率 > 80%
- [ ] 审批流程完整可用

### 8.2 性能验收
- [ ] 支持 100 并发
- [ ] 平均响应时间 < 3s
- [ ] 内存使用 < 2GB

### 8.3 安全验收
- [ ] 无 SQL 注入
- [ ] 敏感数据加密
- [ ] 完整审计日志

---

*本规范由 OpenClaw AI 生成*
