# 智能内容协作平台 · 前端

基于 **React 18 + Vite + React Router + Axios** 的单页应用，对接后端 `ai-doc-platform` 的 REST 接口，覆盖登录注册、文档 CRUD、分类管理与 AI 能力（摘要 / 纠错 / 润色 / 问答）。

## 技术栈

| 能力 | 选型 |
|---|---|
| 框架 | React 18 |
| 构建 / 开发服务器 | Vite 5 |
| 路由 | React Router 6 |
| HTTP | Axios（统一实例 + 拦截器） |
| 状态 | Context（登录态）+ 组件局部 state |

## 目录结构

```
frontend/
├── index.html
├── vite.config.js          # /api 代理到后端 8080
└── src/
    ├── main.jsx            # 入口：BrowserRouter + AuthProvider
    ├── App.jsx             # 路由表（公开页 + 受保护页）
    ├── api/                # 接口层
    │   ├── client.js       # axios 实例：注入 JWT、拆 Result、401/429 处理
    │   ├── auth.js  documents.js  categories.js  ai.js
    ├── context/AuthContext.jsx   # 登录态与 /users/me 恢复
    ├── components/         # Navbar / Layout / ProtectedRoute
    ├── pages/              # Login Register Documents DocumentEditor DocumentDetail AIAssistant Categories
    └── styles/index.css
```

## 本地开发

前提：后端已在 `localhost:8080` 运行（见根目录 `README.md` 的 Docker 或本地启动方式）。

```bash
cd frontend
npm install
npm run dev          # 打开 http://localhost:5173
```

开发服务器已配置代理：前端所有 `/api/**` 请求会被转发到 `http://localhost:8080`，无需处理跨域。

## 生产构建

```bash
npm run build        # 产物输出到 frontend/dist
npm run preview      # 本地预览构建产物
```

部署时将 `dist/` 交给 Nginx，并把 `/api` 反向代理到后端服务即可。

## 与后端的约定

- 统一响应体 `{ code, message, data }`，`code === 200` 视为成功，拦截器直接返回 `data`。
- 认证：登录后端返回 JWT，前端存于 `localStorage`，请求头自动带 `Authorization: Bearer <token>`。
- 401 自动清除登录态并跳转登录页；429（触发限流）提示"操作过于频繁"。
