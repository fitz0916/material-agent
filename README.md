# Material Agent 🏭

> 确定性增强型工业物资智能 Agent - 基于 Spring AI

[English](./README.md) | [中文](./README_CN.md)

## 📖 简介

Material Agent 是一个生产级的工业物资智能管理系统，基于 Spring AI 构建。它结合了大语言模型的智能能力与传统企业系统的确定性，提供物资查询、库存管理、文档检索、智能选型等核心功能。

## ✨ 功能特性

### 🤖 Agent 能力
- **多模型支持** - 支持 Kimi、OpenAI、Claude、Ollama 等多种大模型
- **意图识别** - 智能识别用户意图（物资查询、库存分析、文档搜索等）
- **ReAct 推理** - 增强型 ReAct 循环，支持反思和工具调用
- **对话历史** - 支持多轮对话，上下文记忆
- **模型熔断** - 智能故障转移，自动切换可用模型

### 📊 业务功能
- **物资管理** - 物资 CRUD、分页查询、库存预警
- **采购审批** - 完整的审批流程（创建、审批、撤回）
- **文档管理** - 文档上传、向量入库、混合检索
- **通知系统** - 飞书/钉钉消息推送
- **定时任务** - 每日库存检查、低库存预警、周报生成

### 🔧 技术特性
- **RAG 向量检索** - 混合检索 + RRF 融合算法
- **ETL 管道** - 文档自动分块、向量化
- **监控指标** - Prometheus + Micrometer
- **审计日志** - 完整操作记录
- **异步处理** - 基于 Spring Async

## 🏗️ 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.3 + JDK 21 |
| AI | Spring AI 1.0 (M4) |
| 数据库 | PostgreSQL + PGVector |
| 缓存 | Redis |
| 对象存储 | MinIO |
| 安全 | Spring Security |
| 监控 | Spring Boot Actuator |

## 🚀 快速开始

### 环境要求

- JDK 21+
- PostgreSQL 16+ (带 pgvector 扩展)
- Redis 7+
- MinIO (可选)

### Docker 部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/fitz0916/material-agent.git
cd material-agent

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 填入配置

# 3. 启动服务
docker-compose up -d
```

### 本地运行

```bash
# 1. 编译
mvn clean package -DskipTests

# 2. 配置 application.yml 或环境变量

# 3. 运行
java -jar target/material-agent-1.0.0-SNAPSHOT.jar
```

## ⚙️ 配置说明

### 环境变量

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `DB_HOST` | 否 | localhost | 数据库地址 |
| `DB_PORT` | 否 | 5432 | 数据库端口 |
| `DB_NAME` | 否 | material_agent | 数据库名称 |
| `DB_USER` | 否 | postgres | 数据库用户 |
| `DB_PASSWORD` | 是 | - | 数据库密码 |
| `REDIS_HOST` | 否 | localhost | Redis 地址 |
| `REDIS_PORT` | 否 | 6379 | Redis 端口 |
| `KIMI_API_KEY` | 是 | - | Kimi API Key |
| `OPENAI_API_KEY` | 否 | - | OpenAI API Key |
| `ANTHROPIC_API_KEY` | 否 | - | Claude API Key |
| `MINIO_ENDPOINT` | 否 | http://localhost:9000 | MinIO 地址 |
| `MINIO_ACCESS_KEY` | 否 | minioadmin | MinIO Access Key |
| `MINIO_SECRET_KEY` | 否 | minioadmin | MinIO Secret Key |
| `FEISHU_WEBHOOK_URL` | 否 | - | 飞书 Webhook |
| `DINGTALK_WEBHOOK_URL` | 否 | - | 钉钉 Webhook |

### application.yml 关键配置

```yaml
spring:
  ai:
    model:
      type: kimi  # 默认模型
    vectorstore:
      pgvector:
        initialize-schema: true

agent:
  max-iterations: 10
  max-history-size: 20
  default-model: kimi
```

## 📡 API 接口

### 对话

```bash
# 对话
POST /api/agent/chat
{
  "message": "查询物资 SP-2024-X9",
  "sessionId": "session-001",
  "userId": "user-001"
}

# 流式对话
GET /api/stream/chat?message=查询物资
```

### 物资管理

```bash
# 物资列表（分页）
GET /api/materials?page=0&size=20

# 物资查询
GET /api/materials/code/SP-2024-X9

# 低库存物资
GET /api/materials/low-stock
```

### 审批管理

```bash
# 创建审批
POST /api/approvals

# 审批通过
POST /api/approvals/{taskId}/approve

# 审批拒绝
POST /api/approvals/{taskId}/reject
```

### 文档管理

```bash
# 上传文档
POST /api/documents/upload

# 搜索文档
GET /api/documents/search?keyword=技术手册

# 文档列表
GET /api/documents
```

### 监控

```bash
# 健康检查
GET /actuator/health

# 指标
GET /actuator/metrics

# Prometheus
GET /actuator/prometheus
```

## 📁 项目结构

```
material-agent/
├── src/main/java/com/material/agent/
│   ├── agent/          # Agent 核心
│   ├── config/         # 配置类
│   ├── constant/      # 常量
│   ├── controller/    # API 控制器
│   ├── dto/           # 数据传输对象
│   ├── enums/         # 枚举
│   ├── exception/     # 异常处理
│   ├── model/         # 数据模型
│   ├── repository/    # 数据访问层
│   ├── router/        # 意图路由
│   ├── service/       # 业务服务
│   └── tool/          # 工具定义
├── src/main/resources/
│   ├── application.yml # 配置文件
│   └── logback-spring.xml
├── src/test/          # 单元测试
├── docker-compose.yml  # Docker 部署
└── pom.xml           # Maven 配置
```

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 📋 待实现

- [ ] SSO 认证集成（LDAP/OAuth2）
- [ ] 多租户支持
- [ ] Elasticsearch 全文检索
- [ ] BGE Reranker 集成
- [ ] 完整的 CI/CD 流水线

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 License

MIT License
