# AI 智能文档协作平台（ai-doc-platform）

基于 **Spring Boot 3 + Spring AI** 的 Java+AI 智能文档协作平台后端，集成讯飞星火大模型，提供文档摘要、文本纠错、内容润色、文档问答等 AI 能力，并内置 JWT 认证、Redis 缓存、RabbitMQ 消息通知与滑动窗口限流。

## 技术栈

| 分类 | 选型 |
|---|---|
| 语言 / 运行时 | Java 17 |
| 框架 | Spring Boot 3.5.16 |
| AI | Spring AI 1.0.0（OpenAI 兼容协议接入讯飞星火 X2.5） |
| 持久层 | MyBatis-Plus 3.5.7 + MySQL 8.0 |
| 缓存 / 限流 | Redis（滑动窗口限流，含内存降级） |
| 消息队列 | RabbitMQ（文档变更通知 + 死信队列） |
| 认证 | JWT（jjwt 0.12.6）+ BCrypt 密码加密 |
| 接口文档 | Knife4j 4.5.0（OpenAPI 3） |
| 构建 | Maven |
| 容器化 | Docker（eclipse-temurin:17-jre） |

## 功能模块

- **认证管理**：用户注册 / 登录，签发 JWT Token
- **用户管理**：用户信息（带 Redis 缓存）
- **分类管理**：文档分类 CRUD
- **文档管理**：文档增删改查、分页、详情（逻辑删除 + 缓存 + MQ 通知）
- **AI 能力**：摘要 / 纠错 / 润色 / 问答，均带限流与降级

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0（库名 `ai_doc_platform`，含 `user` / `document` / `category` 表）
- Redis
- RabbitMQ（可选，仅文档通知功能需要）
- 讯飞星火 API Key（AI 功能需要）

## 配置说明

敏感信息全部通过**环境变量**注入，不落代码：

| 环境变量 | 用途 | 默认值 |
|---|---|---|
| `SPARK_API_KEY` | 星火 API Key（`ak-` 开头） | 无（必填） |
| `DB_PASSWORD` | MySQL 密码 | `root` |
| `SPARK_BASE_URL` | 星火 base-url | `https://maas-api.cn-huabei-1.xf-yun.com` |
| `SPARK_MODEL` | 星火模型名 | `spark-x2.5-1.7b` |
| `JWT_SECRET` | JWT 签名密钥 | 内置默认（生产务必覆盖） |

## 快速启动

### 1. 准备中间件

确保 MySQL、Redis（及可选 RabbitMQ）已启动，并建好 `ai_doc_platform` 库。

### 2. 配置环境变量

**从 IDEA 启动**：Run/Debug Configurations → `AiDocPlatformApplication` → Environment variables 填入：
```
SPARK_API_KEY=ak-xxxx;DB_PASSWORD=你的密码
```

**从命令行启动**：
```bash
# Linux / macOS
export SPARK_API_KEY=ak-xxxx
export DB_PASSWORD=你的密码
mvn spring-boot:run

# Windows PowerShell
$env:SPARK_API_KEY="ak-xxxx"; $env:DB_PASSWORD="你的密码"; mvn spring-boot:run
```

### 3. 访问

- 健康检查：<http://localhost:8080/hello>
- 接口文档（Knife4j）：<http://localhost:8080/doc.html>

## Docker 部署

### 1. 打包 jar

```bash
mvn clean package -DskipTests
```

### 2. 构建镜像

```bash
docker build -t ai-doc-platform .
```

### 3. 运行容器

容器内 `localhost` 指向容器自身，访问宿主机中间件需用 `host.docker.internal`。
运行参数通过 `docker.env` 注入（该文件含密钥，已加入 `.gitignore`，勿提交）：

```bash
docker run -d --name aidoc -p 8080:8080 --env-file docker.env ai-doc-platform
```

`docker.env` 示例：
```properties
SPARK_API_KEY=ak-xxxx
DB_PASSWORD=你的密码
SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ai_doc_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
SPRING_RABBITMQ_HOST=host.docker.internal
SPRING_DATA_REDIS_HOST=host.docker.internal
```

验证：
```bash
curl http://localhost:8080/hello
```

## API 概览

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录，返回 Token | 否 |
| POST | `/api/documents` | 创建文档 | 是 |
| PUT | `/api/documents/{id}` | 编辑文档 | 是 |
| DELETE | `/api/documents/{id}` | 删除文档（逻辑删除） | 是 |
| GET | `/api/documents/page` | 分页查询我的文档 | 是 |
| GET | `/api/documents/{id}` | 文档详情 | 是 |
| POST | `/api/ai/summary` | 文档摘要 | 是 |
| POST | `/api/ai/proofread` | 文本纠错 | 是 |
| POST | `/api/ai/polish` | 内容润色 | 是 |
| POST | `/api/ai/qa` | 文档问答 | 是 |
| GET | `/hello` | 健康检查 | 否 |

> 鉴权接口需在请求头携带 `Authorization: Bearer <token>`。

## 架构与数据模型

**分层架构**：`Controller → Service → Mapper → MySQL`，横切能力由拦截器、AOP 切面、全局异常处理器提供。

```
客户端
  │  Authorization: Bearer <JWT>
  ▼
LoginInterceptor（鉴权，解析 userId 入 ThreadLocal）
  ▼
Controller ── @RateLimiter 切面（Redis 滑动窗口限流）
  ▼
Service ── Redis 缓存（Cache Aside） / RabbitMQ 生产者
  ▼
Mapper ── MySQL（MyBatis-Plus 逻辑删除）
```

**核心数据模型**：
- `user`：用户，`username` 唯一；密码 BCrypt 存储
- `document`：文档，归属 `owner_id`，逻辑删除 `is_deleted`，可关联 `category_id`
- `category`：文档分类，归属用户

**关键机制**：
- **认证**：JWT（jjwt 0.12.6）+ BCrypt；`/api/auth/**` 放行，其余 `/api/**` 需 Token
- **缓存**：用户/文档详情走 Redis（Cache Aside + 1 小时 TTL），更新时主动失效
- **消息**：文档变更投递 `doc.exchange` → `doc.notify.queue`，手动 ACK，失败进死信 `doc.dead.queue`

## 统一响应与错误码

所有接口返回统一结构 `Result<T>`：

```json
{ "code": 200, "message": "成功", "data": null }
```

| code | 含义 | 触发场景 |
|---|---|---|
| 200 | 成功 | 正常返回 |
| 400 | 参数校验失败 | `@Valid` 不通过（空文本、超 2000 字等） |
| 401 | 未登录 / Token 无效 | 拦截器校验失败 |
| 403 | 无权操作 | 操作他人文档/分类 |
| 404 | 资源不存在 | 文档不存在或已删除 |
| 429 | 请求过于频繁 | 触发限流（每用户每接口 60 秒 5 次） |
| 500 | 服务器内部错误 | 未捕获异常（含根因回显） |
| 503 | AI 服务不可用 | 星火调用异常 `AiServiceException` |

## 限流与降级

- **限流**：`@RateLimiter` 基于 Redis + Lua 的**滑动窗口**，按 `用户ID + 接口` 维度计数，默认 60 秒 5 次，超限返回 429。
- **Redis 故障降级**：限流切面在 Redis 不可用时**放行请求**（不因限流组件拖垮主流程），仅打 WARN 日志。
- **AI 降级**：星火不可用时，摘要降级返回原文前 200 字；问答对无关问题返回“未找到相关信息”。

## 常见问题（FAQ）

- **登录报 500 “Unable to connect to Redis”**：Redis 未启动。本项目 Redis 若跑在 WSL2，重启后需手动 `sudo service redis-server start`。
- **AI 接口报占位符/启动失败**：`SPARK_API_KEY` 未配置（yml 中无默认值，必须非空）。
- **容器内连不上中间件**：容器里的 `localhost` 是容器自身，连宿主机服务要用 `host.docker.internal`；Redis 若绑在 WSL2 的 `127.0.0.1`，容器经 `host.docker.internal` 可能连不到，建议将 Redis 一并容器化并与应用同网络。
- **星火报 11200 / AppIdNoAuthError**：`SPARK_MODEL` 与授权模型不匹配，需用调用示例中的真实 model 与 base-url。

## 项目结构

```
src/main/java/com/aidoc/aidocplatform
├── common        # 通用：Result、异常、限流注解/切面、工具类
├── config        # 配置：AI、Redis、RabbitMQ、MyBatis-Plus、Security、Knife4j 等
├── controller    # 控制层：Auth/User/Category/Document/Ai/Hello
├── dto           # 请求/响应数据传输对象
├── entity        # 数据库实体
├── interceptor   # 登录拦截器
├── mapper        # MyBatis-Plus Mapper
├── mq            # RabbitMQ 消费者
└── service       # 业务接口与实现
```
