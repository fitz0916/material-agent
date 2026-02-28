# Material Agent

确定性增强型工业物资智能 Agent - 基于 Spring AI

## 功能特性

- 🎯 意图路由 - 智能识别用户意图
- 🔧 工具约束 - Schema 校验确保安全
- 📊 状态机 - 业务流程可控
- 📚 RAG - 向量检索智能问答
- 📄 ETL - 文档自动入库
- 🔔 通知 - 飞书/钉钉消息推送

## 技术栈

- JDK 21 + Spring Boot 3.3
- Spring AI 1.0
- PostgreSQL + PGVector
- Redis + MinIO

## 快速开始

```bash
# 编译
mvn clean package

# 运行
java -jar target/material-agent-1.0.0.jar
```

## 环境变量

| 变量 | 说明 |
|------|------|
| KIMI_API_KEY | Kimi API Key |
| DB_PASSWORD | 数据库密码 |
| MINIO_ACCESS_KEY | MinIO Access Key |
| MINIO_SECRET_KEY | MinIO Secret Key |
| FEISHU_WEBHOOK_URL | 飞书 Webhook |
| DINGTALK_WEBHOOK_URL | 钉钉 Webhook |

## API

- `POST /api/agent/chat` - 对话
- `GET /api/materials` - 物资列表
- `GET /api/materials/{code}` - 物资详情

## License

MIT
