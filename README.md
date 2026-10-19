# Aimin 医疗诊断工作台 · 前端

管理端 Web（Vue 3 + Vite 6 + Element Plus + Pinia），对接 aimin-gateway（8080）。

## 页面

- `/login`：账号登录（后端本地算术验证码，一次有效）
- `/index`：工作台首页（文档/切片统计、快捷入口、安全提示）
- `/agent/index`：三栏问诊工作台（会话列表 / SSE 对话 + 证据卡 + 风险确认 / 会话概览）
- `/knowledge/index`：知识库（拖拽上传 PDF/MD/TXT、文档列表）
- `/system/*`：管理员、角色、菜单权限

## 启动

```bash
npm install
npm run dev      # http://localhost:5174
npm run build
```

开发代理：`/api` → `http://localhost:8080` 并去掉 `/api` 前缀（见 `vite.config.js`）。
SSE 流式接口走 `/api/aimin-agent/agent/chat`，鉴权头由 `api/agent.js` 从 Pinia 用户态读取
（`aimin-admin-token`），401 时统一清理登录态并跳转登录页。

## 目录

```
src/
  api/          agent.js（SSE + 文档）、system/*（管理员/角色/菜单）、auth.js
  components/   BrandLogo.vue
  layout/       侧栏 + 顶栏 + 面包屑
  router/       静态路由 + 后端动态路由合并
  store/        user / route（持久化）
  styles/       index.scss（设计令牌 + Element Plus 覆盖）
  views/        agent、knowledge、dashboard、system、login
```

## 约定

- 接口统一返回 `{code, msg, data}`，`code=200` 为成功；裸数组/空体已在拦截器做兼容。
- 聊天流用 `AbortController`，切会话/卸载组件时中断，避免幽灵请求。
- 图标由 `main.js` 全局注册，模板里可直接写 `<el-icon><UploadFilled /></el-icon>`。
