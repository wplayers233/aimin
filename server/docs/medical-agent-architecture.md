# aimin 医疗 Agent 改造方案（v1.0 评审稿）

> 目标：把 aimin 项目从「药品管理后台」升级为**医疗诊断 Agent 工作台**，对标 wind-ops-agent 的架构模式（多模态 RAG + 混合检索 + 可追溯证据 + 安全确认流）。
> 前端同步做苹果审美重构，最终形态是一个「对话式医疗诊断工作台」。

---

## 1. 现有资产盘点

| 现有模块 | 医疗 Agent 中的角色 | 处置 |
|---|---|---|
| aimin-gateway (8080) | 统一入口，新增 `/aimin-agent/**` 路由 | ✅ 复用，加一条路由 |
| aimin-auth (8081) | 登录/鉴权/权限 | ✅ 原样复用 |
| aimin-admin (8084) | 后台管理（知识库维护、用户管理、审核工单） | ✅ 复用，前端重做 |
| aimin-drug | 药品结构化数据 + ES 检索 | ✅ 升级为药品知识源 |
| aimin-ai | MongoDB + Ollama 的 RAG 雏形 | 🔧 改造：并入 agent 检索层 |
| aimin-search | 通用 ES 搜索 | 🔧 承担关键词/BM25 稀疏召回 |
| aimin-canal | MySQL binlog 同步 | ⏸️ 暂缓，Phase 4 再启用 |
| aimin-admin-front (Vue3) | 全部重构为苹果风 + 会话工作台 | 🔧 重构 |

## 2. 目标架构总览（全 Java 栈）

```
┌─────────────────────────────────────────────────────────┐
│  前端（Vue3 + 苹果风）                                    │
│  登录页 / 管理后台 / ⭐医疗诊断工作台（对话式）             │
└───────────────────────┬─────────────────────────────────┘
                        │ :8080
┌───────────────────────▼─────────────────────────────────┐
│  aimin-gateway（Spring Cloud Gateway）                   │
│  /aimin-auth/**  /aimin-admin/**  /aimin-agent/** (新)   │
└──────┬──────────────┬──────────────┬────────────────────┘
       │              │              │
┌──────▼─────┐ ┌──────▼─────┐ ┌─────▼──────────────────┐
│ aimin-auth │ │ aimin-admin│ │ ⭐aimin-agent (Java 新) │
│ 登录鉴权    │ │ 知识库后台  │ │  LangChain4j 编排:      │
│ :8081      │ │ 审核工单    │ │  意图→检索→风险→诊断→记忆│
│            │ │ :8084      │ │  :8085                  │
└────────────┘ └────┬───────┘ └───┬─────────┬───────────┘
                    │             │         │
              ┌─────▼─────────────▼───┐ ┌───▼──────────┐
              │ 知识与数据层            │ │ 模型层(API)   │
              │ MySQL(业务+患者档案)    │ │ DeepSeek-Chat│
              │ Redis Stack(向量+记忆)  │ │ 通义 embed   │
              │ MongoDB(原始文档)       │ │ (Ollama 兜底) │
              └───────────────────────┘ └──────────────┘
```

## 3. aimin-agent 编排设计（核心）

> 编排框架：**LangChain4j**（Java 原生）。借鉴 wind-ops 的流程模式，代码全部自研。
> LangChain4j 没有 LangGraph 式的图中断原语，**风险确认流用两阶段调用实现**：
> 第一轮生成「诊断草案 + 风险评估 + 待确认项」，前端弹出确认卡；用户确认后携带 `confirm_token` 发起第二轮，才允许输出处方/剂量级内容。

```
用户输入(症状/报告/检查单图片)
   │
   ▼
[意图识别] AiService 分类 ──闲聊/无关──> 直接回复（拒绝诊断）
   │ 医疗咨询
   ▼
[多模态解析]  文本 + 图片(检查单 OCR/VLM，Phase 3 接入)
   │
   ▼
[混合检索]  Redis 向量召回 ∥ drug 库结构化查询
   │        RRF 融合(自研百行) + 元数据过滤(科室/药类/剂型)
   ▼
[风险评估]  ⚠️ 命中处方/剂量/禁忌 → 两阶段中断，出确认卡
   ▼
[诊断生成]  DeepSeek-Chat 流式输出
   │        候选原因 + 排查建议 + 用药提示 + 风险声明
   │        每条结论必须带可追溯引用(文档ID+页码/段落)
   ▼
[记忆写回]  ChatMemory(Redis) + 长期患者档案(向量召回)
   ▼
响应返回（SSE 流式）
```

**医疗安全规则（硬编码在编排层，不可跳过）**：
- 任何「处方/剂量」输出必须先过风险确认节点
- 免责声明固定输出（“仅供专业参考，不构成诊疗建议”）
- 证据不足时明确说“知识库未覆盖”，禁止编造（faithfulness 检查）

## 4. 技术选型（已评审定稿）

| 决策点 | 选型 | 说明 |
|---|---|---|
| Agent 编排 | **LangChain4j**（Java 原生） | AI Services + 自研流程管道；有官方 `langchain4j-deepseek`、`langchain4j-redis`、spring-boot-starter。**约束**：无图中断原语，风险确认用两阶段调用实现（见 §3） |
| LLM | **DeepSeek API**（DeepSeek-Chat） | 国内直连、低成本、强推理；OpenAI 兼容协议，LangChain4j 官方模块直连 |
| Embedding | 通义 text-embedding-v3（API）→ 本地 bge-m3 降级 | 先 API 起步 |
| 向量库 | **Redis Stack**（RediSearch 向量检索） | 本机 Redis 已在跑，升级一个容器同时解决「向量检索 + 会话记忆缓存」 |
| 稀疏检索 | Phase 2 用 drug 库结构化查询；Phase 3 视需要引入 ES BM25 | 控制前期依赖 |
| 检索融合 | RRF 自实现（Java，~百行） | 借鉴 wind-ops 模式，代码自研 |
| 记忆 | 会话 ChatMemory → Redis；长期患者档案 → MySQL + Redis 向量 | 简单够用 |
| 医疗知识源 | 药品说明书 PDF、诊疗指南 PDF、drug 表结构化数据、检查检验字典 | 管理端做上传入库界面 |
| 模块形式 | `aimin-agent` Java 微服务（Spring Boot 3 + JDK21，与其他服务同构） | 网关加 `/aimin-agent/**` 直连路由 |

**选型原则**：零 Python、零 Milvus、零 ES（P2 阶段）——只用本机已运行的中间件 + 一个 Redis Stack 容器 + 一个 DeepSeek API key。

### 4.1 配套设计决策（已评审确认）

| # | 决策点 | 结论 |
|---|---|---|
| 1 | Redis Stack 部署 | **另起 6380 容器**，现有 6379（auth/admin 在用）不动，互不牵连 |
| 2 | 多模态（检查单图片） | **推到 P3+**；P2 先纯文本闭环，接口预留 `attachments` 字段；图片届时走通义 qwen-vl（DeepSeek 无视觉） |
| 3 | 知识切片策略 | **按章节标题切 + 512 token 兜底**；入库管线：PDF/MD → PDFBox/Tika 提文本 → 切片 → embedding → Redis Stack |
| 4 | 鉴权链路 | agent 复用现有 Redis(6379) 的 token 校验逻辑，与 admin/auth 同一套，不另造轮子 |
| 5 | aimin-agent 分层 | controller(`/chat` SSE、`/confirm`、`/sessions`) / service(MedicalAgentService 编排) / retrieval(Vector+Drug+RRF) / memory(ChatMemoryProvider) / config(starter 装配) |
| 6 | 前端工作台布局 | 左会话列表 240px 半透明 / 中对话流（证据角标+琥珀色确认卡）/ 右患者档案面板可折叠 |

## 5. 前端苹果审美重构

| 区域 | 设计 |
|---|---|
| 全局 | 字体栈 `-apple-system, "SF Pro", "PingFang SC"`；底色白/`#F5F5F7`；强调色 `#0A84FF`（医疗青蓝）；圆角 12-16px |
| 登录页 | 渐变柔光背景 + 居中毛玻璃卡片（`backdrop-filter: blur(20px)`），去掉验证码弹窗依赖（已旁路） |
| 主框架 | 左侧半透明侧栏 + 内容区大留白；菜单项选中态用浅蓝胶囊 |
| 诊断工作台 | 左：会话列表；中：对话流（证据卡片带引用角标、风险确认卡用琥珀色警示样式）；右：患者档案/引用面板 |
| 交互 | 页面淡入、卡片 hover 浮起、按钮按压 scale(0.97)、流式回复打字机效果 |

技术实现：Element Plus 主题变量覆盖 + 自定义全局 CSS（不换 UI 库，控制成本）。

## 6. 数据模型（新增部分）

```
medical_document   知识文档(id, title, doc_type[指南/说明书/教材], file_url, status)
medical_chunk      切片(id, doc_id, page, text, embedding→Milvus, metadata)
consult_session    会话(id, user_id, patient_brief, created_at)
consult_message    消息(id, session_id, role, content, evidence_ids, risk_level)
evidence_ref       证据引用(id, doc_id, chunk_id, score, modal)
patient_memory     长期记忆(user_id, fact, embedding, ttl)
```

## 7. 分期实施计划

| Phase | 内容 | 验收标准 | 预估 |
|---|---|---|---|
| **P1 前端苹果风** | 登录页+主框架+组件主题重构 | 视觉验收 | 1-2 天 |
| **P2 Agent 骨架** | aimin-agent（Java/LangChain4j）+ DeepSeek 接入 + 意图识别→检索→生成最小闭环（无向量库，drug 库结构化检索） | 网关全链路流式对话可用 | 2-3 天 |
| **P3 知识库** | 文档上传入库 + Redis Stack 向量召回 + RRF 融合 + 引用可追溯 | 回答带证据引用 | 3-4 天 |
| **P4 安全+记忆** | 两阶段风险确认流 + Redis ChatMemory + 长期患者档案 + 评估集（faithfulness） | 高风险问题强制确认、回答忠实度可量化 | 2-3 天 |
| **P5 管理后台** | 知识库维护页、审核流、苹果风统一 | 全功能闭环 | 2 天 |

## 8. 风险与依赖

| 风险 | 应对 |
|---|---|
| 外网受限（FlClash 已废） | 全部依赖走国内源/本地：Redis Stack 镜像国内源拉取；LangChain4j 依赖走 Maven 中央仓库/阿里云镜像（已验证可用） |
| LLM 稳定性 | 主用 DeepSeek API（国内直连）；不可用时降级通义（同为 OpenAI 兼容，改配置即切），最后 Ollama 本地兜底 |
| 医疗合规 | 明确定位为「专业辅助参考工具」，免责声明固定输出；高风险动作必须人工确认 |
| LangChain4j 无图中断 | 已在方案内消化：两阶段调用实现风险确认（见 §3） |

---

*评审通过后从 P1 开始执行；本文档随实施迭代维护。*
