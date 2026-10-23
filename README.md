# AI 智能文档协作平台

> 一个 **AI 驱动的全文档协作平台**：React 前端 + Spring Boot 3 / Spring AI 后端，集成讯飞星火大模型，支持文档摘要、纠错、润色、问答，并自主实现了完整的 **RAG 检索增强生成** 链路。
>
> 项目全程使用 AI 编码工具（Cursor / Claude Code 等）辅助完成端到端交付：需求拆解 → 架构设计 → 前后端编码 → 联调测试 → Docker 部署。

| 入口 | 链接 |
|---|---|
| 在线 Demo | *(部署后填写)* |
| 演示视频 | *(录制后填写)* |
| 接口文档 | 启动后访问 `http://localhost:8080/doc.html`（Knife4j 可交互调试） |

---

## ✨ 核心功能

### 前端（React 18 + Vite）
- 登录 / 注册（JWT 持久化，路由守卫ProtectedRoute）
- 文档管理：列表分页、富文本编辑、详情查看、分类管理
- **AI 助手页**：摘要 / 纠错 / 润色 / 文档问答一站式操作

### 后端（Spring Boot 3 + Spring AI）
- **认证体系**：JWT 签发校验 + BCrypt 密码存储 + 拦截器鉴权
- **文档 / 分类 CRUD**：MyBatis-Plus 逻辑删除、分页查询、越权校验
- **AI 能力**：文档摘要、文本纠错、内容润色、文档问答（讯飞星火，OpenAI 兼容协议接入）
- **RAG 检索增强问答**：文档切块 → 向量化入库 → 相似度召回 → 元数据过滤 → 拼 Prompt 生成，实现**文档级数据隔离**的智能问答
- **异步通知**：文档变更经 RabbitMQ 投递，手动 ACK + 死信队列兜底
- **限流防护**：Redis + Lua 滑动窗口限流（用户 × 接口维度），Redis 故障自动降级放行

## 🛠 技术栈

| 分类 | 选型 |
|---|---|
| 前端 | React 18 · Vite 5 · React Router 6 · Axios |
| 语言 / 框架 | Java 17 · Spring Boot 3.5 |
| AI | Spring AI 1.0（讯飞星火 X2.5 对话 + Embedding）· RAG（VectorStore + TokenTextSplitter） |
| 持久层 | MyBatis-Plus 3.5 · MySQL 8.0 |
| 缓存 / 限流 | Redis（Cache Aside · Lua 原子滑动窗口 · 故障降级） |
| 消息队列 | RabbitMQ（手动 ACK + 死信队列） |
| 认证 | JWT（jjwt 0.12.6）+ BCrypt |
| 接口文档 | Knife4j（OpenAPI 3） |
| 部署 | Docker（Temurin 17 JRE，非 root 运行 + HEALTHCHECK + 优雅停机） |

## 🏗 架构

```
React 前端 (Vite)
   │  Authorization: Bearer <JWT>
   ▼
LoginInterceptor ── 鉴权，userId 入 ThreadLocal
   ▼
Controller ── @RateLimiter AOP 切面（Redis+Lua 滑动窗口限流）
   ▼
Service ──► Redis 缓存（Cache Aside）      ──► 星火 LLM / Embedding
   │        │                                  │
   │        └── RAG：切块→向量化→召回→过滤→生成 ─┘
   ▼
Mapper ── MySQL（逻辑删除）    MQ：doc.exchange → 通知队列 →（失败）死信队列
```

**数据模型**：`user`（用户名唯一，BCrypt 密码）→ `document`（归属用户 + 分类，逻辑删除）→ `category`

## 🔍 技术亮点速览

1. **真 RAG 而非截断塞 Prompt**：从「整篇截断」朴素问答迭代为完整检索增强链路，用 docId/ownerId 元数据过滤保证多用户数据隔离
2. **限流原子性**：计数窗口全部收敛在一段 Lua 脚本内完成，避免竞态；Redis 挂掉时降级放行，不拖垮主流程
3. **AI 降级兜底**：星火不可用时摘要返回原文前 200 字，问答返回「未找到相关信息」，接口永不 500 裸奔
4. **消息可靠投递**：手动 ACK + 死信队列，消费失败不丢消息
5. **统一响应体系**：`Result<T>` + 全局异常处理器，429（限流）/ 503（AI 不可用）等语义化错误码
6. **密钥零硬编码**：API Key、数据库密码全部环境变量注入，`docker.env` 不入库

## 🚀 快速启动

### 环境要求
JDK 17+ · Maven · MySQL 8.0 · Redis · RabbitMQ（可选）· Node 18+ · 讯飞星火 API Key

### 后端

```bash
# 敏感信息通过环境变量注入
export SPARK_API_KEY=ak-xxxx DB_PASSWORD=你的密码   # Windows PowerShell 用 $env:XXX="..."
mvn spring-boot:run
# 健康检查 http://localhost:8080/hello    接口文档 http://localhost:8080/doc.html
```

### 前端

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173，已配置代理转发 /api → 8080
```

### Docker 部署

```bash
mvn clean package -DskipTests
docker build -t ai-doc-platform .
docker run -d --name aidoc -p 8080:8080 --env-file docker.env ai-doc-platform
```

`docker.env`（含密钥，勿提交）示例：
```properties
SPARK_API_KEY=ak-xxxx
DB_PASSWORD=你的密码
SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ai_doc_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
SPRING_DATA_REDIS_HOST=host.docker.internal
SPRING_RABBITMQ_HOST=host.docker.internal
```

## 📁 项目结构

```
├── frontend/                 # React 前端
│   └── src/{api, components, context, pages}
└── src/main/java/com/aidoc/aidocplatform
    ├── common/               # Result、异常、限流注解/切面/Lua、JWT/缓存工具
    ├── config/               # AI、RAG、Redis、RabbitMQ、Security、Knife4j
    ├── controller/           # Auth / User / Category / Document / AI / RAG
    ├── interceptor/          # JWT 登录拦截器
    ├── mq/                   # 文档变更消费者（手动 ACK + 死信）
    └── service/              # 业务实现（含 RAG 索引与问答）
```

## 📄 License

MIT
