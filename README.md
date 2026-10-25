# aimin · 医疗诊断 Agent 工作台

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![Vue.js](https://img.shields.io/badge/Vue.js-3-42b883)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.36.2-purple)
![License](https://img.shields.io/badge/License-GPLv3-blue)

> **aimin** —— 让每一次医疗问答，都有据可查。

面向医疗专业人员的辅助参考系统：文档入库后自动切片建索引，提问时走「意图识别 → 医疗风险两阶段确认 → 混合检索 → RRF 融合 → 流式生成 → 证据引用」的完整链路，回答附带可回溯的文档出处与固定免责声明。

> 本项目仅供技术研究与演示，**不构成诊疗建议**。

---

## What's Inside

- **混合检索引擎** -- 向量（bge-small-zh 本地 ONNX）+ BM25（中文 bigram）+ 药品结构化三路召回，RRF 融合后阈值门控，无可靠证据直接拒答
- **医疗风险两阶段确认** -- 5 组硬编码正则（剂量/处方/停换药/联合用药/特殊人群）命中即中断，`confirmToken` 绑定原问题、5 分钟有效、一次性消费，不可被提示词绕过
- **可追溯证据引用** -- 每条结论附 `[1][2]` 角标对应原文片段（文档名 + 页码），提示词层约束编号范围
- **SSE 流式生成** -- DeepSeek（OpenAI 兼容协议）逐 token 推送；无 Key 自动进入演示模式，全链路离线可跑
- **知识入库管线** -- PDF / Markdown / TXT（白名单校验 + 路径净化），章节优先切片（512 字上限、80 字重叠）
- **会话审计与脱敏** -- 会话/消息/审计三表落库，手机号与身份证号入库前脱敏，90 天定时清理
- **真实运行统计** -- 工作台首页聚合落库数据（会话/提问/风险两阶段计数/按天问答量），零演示假数据
- **离线评测集** -- 60 题 CLI 评测：Recall@5 0.967（29/30）、MRR 0.781、药品命中 1.0、风险识别 1.0、拒答 1.0

---

## Quick Start

### 1. 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | 必须带 `--enable-preview` 运行 |
| Maven | 3.8+ | 本地仓库建议放 ASCII 路径，如 `C:/Users/Public/aimin-mvn-repo` |
| Node.js | 18+ | 前端构建 |
| MySQL | 8.x | 库名 `aimin` |
| Redis | 6+ | 默认 6379（Sa-Token 会话） |

### 2. 建库（按顺序执行）

```bash
mysql -uroot -p aimin < server/sql/aimin2025_05_10_01.sql
mysql -uroot -p aimin < server/sql/20261018_phase0_hardening.sql
mysql -uroot -p aimin < server/sql/20261019_demo_drugs.sql
mysql -uroot -p aimin < server/sql/20261019_phase2_persistence.sql
```

### 3. 构建并启动后端

```bash
cd server
mvn -DskipTests -Dmaven.repo.local=C:/Users/Public/aimin-mvn-repo -pl aimin-agent -am package

# 四个进程逐个启动（端口必须显式指定，环境变量可能泄漏覆盖）
SERVER__PORT=8081 java --enable-preview -jar aimin-auth/target/aimin-auth.jar
SERVER__PORT=8084 MYSQL_URL="jdbc:mysql://127.0.0.1:3306/aimin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" MYSQL_USERNAME=root MYSQL_PASSWORD=123456 java --enable-preview -jar aimin-admin/target/aimin-admin.jar
SERVER__PORT=8087 MYSQL_URL="..." MYSQL_USERNAME=root MYSQL_PASSWORD=123456 DEEPSEEK_API_KEY=sk-xxx java --enable-preview -jar aimin-agent/target/aimin-agent.jar
SERVER__PORT=8080 SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false java --enable-preview -jar aimin-gateway/target/aimin-gateway.jar
```

未配置 `DEEPSEEK_API_KEY` 时 agent 自动进入**演示模式**（固定模板回答，检索/风险/审计链路照常）。

### 4. 启动前端

```bash
cd admin-front
npm install
npm run dev     # http://localhost:5174
```

登录：`admin / 123456`（验证码为本地算术题，点题面可刷新）。

> **Windows 中文用户名路径报错**：若 `npm run dev` 抛 `Cannot read file "package.json": winapi error #5`，是 esbuild 在非 ASCII 路径下的已知问题。把 `admin-front` 整个目录（含 `node_modules`）实体复制到纯英文路径运行即可，目录联接（junction）无效。

### 5. 交互示例（curl）

```bash
# 1) 取验证码（本地算术题）→ 2) 计算答案并登录
curl http://127.0.0.1:8080/aimin-admin/public/auth/captcha
curl -X POST http://127.0.0.1:8080/aimin-admin/public/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456","captchaId":"<id>","captchaAnswer":"<答案>"}'

# 3) SSE 流式问诊（必须带 aimin-admin-token 头）
curl -N -X POST http://127.0.0.1:8080/aimin-agent/agent/chat \
  -H 'Content-Type: application/json' -H "aimin-admin-token: <token>" \
  -d '{"sessionId":"s1","message":"高血压怎么分级"}'
```

---

## Architecture

```
浏览器 :5174 ──▶ 网关 :8080 ──┬──▶ admin :8084   RBAC / 验证码 / 菜单
                              └──▶ agent :8087   检索 / 风险 / 流式生成
                                      │
                     MySQL(aimin) ◀────┴────▶ DeepSeek API
```

问诊管线：`意图识别 → 风险评估(两阶段) → 三路召回(向量+BM25+药品) → RRF 融合 → 门控过滤 → Prompt 组装(证据+历史+编号约束) → SSE 生成 → 输出侧剂量兜底 → 落库脱敏`

完整拓扑与数据流见 [`docs/架构图.html`](docs/架构图.html)、[`docs/数据流图.html`](docs/数据流图.html)，文字版见 [`docs/架构说明.md`](docs/架构说明.md)。

---

## 项目结构

```
aimin/
├── server/ ─ Java 后端（Maven 多模块，父 POM 在此）
│   ├── aimin-gateway/ ─ 统一入口 :8080（直连路由 / Sa-Token 鉴权 / 未登记路径默认拒绝）
│   ├── aimin-agent/ ─ 诊断 Agent 服务 :8087（RAG 核心）
│   │   └── src/main/java/.../agent/
│   │       ├── controller/ ─ SSE 对话 / 风险确认 / 文档 / 会话 / 统计接口
│   │       ├── service/ ─ MedicalAgentService 编排 / IngestionService 入库 / 落库脱敏
│   │       ├── retrieval/ ─ VectorRetriever / Bm25Index / DrugRetriever / RrfFusion
│   │       ├── risk/ ─ RiskEvaluator 医疗风险规则（硬编码，两阶段确认）
│   │       ├── llm/ ─ LlmClient 抽象（DeepSeek 实现 / Mock 演示模式）
│   │       ├── memory/ ─ 进程内会话记忆（40 条窗口）
│   │       ├── eval/ ─ 60 题离线评测 Runner
│   │       └── config/ ─ embedding 模型 / 向量库 / LLM 装配
│   ├── aimin-admin/ ─ 管理后台 :8084（RBAC / 算术验证码 / 菜单 / 审计）
│   ├── aimin-auth/ ─ 微信登录 :8081（网页端暂未接入）
│   ├── aimin-common/ ─ 公共组件（base / ds / redis / cache / satoken）
│   ├── sql/ ─ 建库与增量脚本
│   └── data/ ─ 运行时数据（演示语料 / 向量快照 / 评测集）
├── admin-front/ ─ Vue 3 前端
│   └── src/
│       ├── views/ ─ 页面（诊断工作台 / 首页展台 / 知识库 / 系统管理）
│       ├── api/ ─ 接口封装（axios REST + fetch SSE 流式解析）
│       ├── store/ ─ Pinia（user / route / app）
│       ├── layout/ ─ 主框架（侧栏 + 顶栏 + 动态菜单）
│       ├── styles/ ─ 暖砂鼠尾草设计 token 体系
│       └── utils/ ─ 请求拦截 / 动态路由 / 日期
└── docs/ ─ 架构文档与决策记录（历史决策见 archive/）
```

---

## API 端点

网关 `:8080` 为唯一入口；除 `/public/**` 外均需 `aimin-admin-token` 请求头，未登记路径默认拒绝。

| Method | Path | 描述 |
|---|---|---|
| POST | `/aimin-agent/agent/chat` | SSE 流式对话（事件：start/evidence/delta/risk/done/error） |
| POST | `/aimin-agent/agent/confirm` | 高风险二次确认（SSE） |
| GET | `/aimin-agent/agent/status` | 运行状态（模型模式 / 知识库规模） |
| GET | `/aimin-agent/agent/stats?days=14` | 工作台统计（会话 / 风险计数 / 按天问答量） |
| GET | `/aimin-agent/agent/sessions` | 会话列表（按更新时间倒序） |
| GET | `/aimin-agent/agent/sessions/{id}/messages` | 会话消息回放 |
| POST | `/aimin-agent/agent/risk/reject` | 放弃高风险问题（记审计） |
| POST | `/aimin-agent/agent/docs` | 文档上传入库（pdf/md/txt，≤50MB） |
| GET | `/aimin-agent/agent/docs` | 已入库文档列表 |
| GET | `/aimin-admin/public/auth/captcha` | 本地算术验证码（TTL 3 分钟，一次性） |
| POST | `/aimin-admin/public/auth/token` | 登录换取 token |

---

## 配置

| 变量 | 作用 | 默认 |
|---|---|---|
| `MYSQL_URL` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据源 | `127.0.0.1:3306/aimin`，root / 123456 |
| `DEEPSEEK_API_KEY` | 生成模型密钥 | 空 → 演示模式 |
| `SERVER__ADDRESS` | 服务监听地址 | `127.0.0.1`（对外暴露需显式设置） |
| `AIMIN_CORS_ORIGINS` | 网关允许来源 | `localhost:5174,127.0.0.1:5174` |
| `aimin.agent.retrieval.min-vector-score` | 向量门控阈值 | `0.88` |
| `aimin.agent.retrieval.min-bm25-score` | BM25 门控阈值 | `6.5` |
| `aimin.agent.top-k` | 证据条数 | `5` |

> 换语料或换 embedding 模型后，两个检索阈值必须用评测集重新标定。

---

## 测试与评测

```bash
# 纯函数单测（风险规则 / RRF 融合 / 脱敏 / 记忆裁剪 / 检索门控）
cd server && mvn -pl aimin-agent test

# 离线评测（60 题，跑完自动退出）
SERVER__PORT=18087 java --enable-preview -jar aimin-agent/target/aimin-agent.jar --aimin.eval.enabled=true
# 结果写入 data/eval/latest.json
```

当前基线：**Recall@5 0.967、MRR 0.781、药品 12/12、风险 10/10、拒答 8/8**。改动 `RiskEvaluator` 规则后必须重跑评测确认不回归。

---

## 医疗安全边界

- 处方、剂量、联合用药、特殊人群等关键词命中后**必须二次确认**才生成内容，规则硬编码于 `RiskEvaluator`，不可通过提示词绕过。
- 检索结果为空时直接拒答，不调用大模型编造。
- 回答中出现具体剂量时追加核对提示；免责声明固定输出、不可关闭。
- 会话与审计落库前对手机号、身份证号脱敏。

---

## 技术栈

- **后端**：Java 21（`--enable-preview`）· Spring Boot 3.4 · Spring Cloud 2024 · Sa-Token 1.39 · MyBatis-Plus 3.5.9 · Maven 多模块
- **AI**：LangChain4j 0.36.2（锁定版本）· DeepSeek `deepseek-chat` · 本地 ONNX `bge-small-zh` · 自研 BM25/RRF
- **前端**：Vue 3.5 · Vite 6 · Element Plus 2.9 · Pinia 3 · 手写 SVG 图表
- **存储**：MySQL 8（19 表）· Redis 6+ · InMemory 向量库 + JSON 快照（Redis Stack 迁移在 roadmap）

---

## 已知限制

- 向量库为 `InMemoryEmbeddingStore` + JSON 快照，**单机单实例**，不支持横向扩展。
- 待确认风险项、会话记忆存于进程内，服务重启即失效。
- 多轮指代依赖「上一问 + 当前问」拼接检索，长句省略主语时会漏判。
- agent 服务自身不校验 token，仅信任网关；四个服务默认已绑定 `127.0.0.1`。
- 运行模式为无 Nacos 的本地直连（网关路由写死 `127.0.0.1`），需 `SPRING_CLOUD_NACOS_*_ENABLED=false`。

---

## 文档

| 文件 | 内容 |
|---|---|
| [docs/架构说明.md](docs/架构说明.md) | 落地架构、模块职责、核心链路 |
| [docs/优化方案.md](docs/优化方案.md) | 审计问题清单与分阶段整改记录 |
| [docs/ADR-001-可信度阶段.md](docs/ADR-001-可信度阶段.md) | 检索与会话相关选型决策 |
| [docs/架构图.html](docs/架构图.html) / [docs/数据流图.html](docs/数据流图.html) | 可视化架构与数据流 |

## License

[GPL-3.0](server/LICENSE)
