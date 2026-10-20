# aimin · 医疗诊断 Agent 工作台

面向医疗专业人员的辅助参考系统。文档入库后自动切片建索引，提问时走「意图识别 → 医疗风险两阶段确认 → 混合检索 → RRF 融合 → 流式生成 → 证据引用」的完整链路，回答附带可回溯的文档出处与固定免责声明。

> 本项目仅供技术研究与演示，**不构成诊疗建议**。

## 核心能力

| 能力 | 实现 |
|---|---|
| 知识入库 | PDF / Markdown / TXT，章节优先切片（512 字上限、80 字重叠），本地 ONNX 向量化 |
| 混合检索 | 向量（bge-small-zh）+ BM25（中文 bigram）双路召回，RRF 融合后按阈值门控 |
| 药品召回 | 读取 `t_drug` 结构表，词频 + IDF + 药名加权，作为独立第三路 |
| 风险确认 | 5 组硬编码正则命中即中断，`confirmToken` 绑定原问题、5 分钟有效、一次性消费 |
| 流式回答 | DeepSeek（OpenAI 兼容协议）SSE 推送，逐 token 渲染 |
| 会话审计 | 会话 / 消息 / 审计三表落库，手机号与身份证号脱敏，90 天定时清理 |
| 离线评测 | 60 题集，输出 Recall@5、MRR、药品命中率、风险识别率、拒答率 |

当前基线：**Recall@5 0.967（29/30）、MRR 0.781、药品 1.0、风险 1.0、拒答 1.0**。

## 架构

```
浏览器 :5174 ──▶ 网关 :8080 ──┬──▶ admin :8084   RBAC / 验证码 / 菜单
                              └──▶ agent :8087   检索 / 风险 / 流式生成
                                      │
                     MySQL(aimin) ◀────┴────▶ DeepSeek API
```

完整拓扑、数据流与检索细节见仓库根目录的 [`架构图.html`](架构图.html) 与 [`数据流图.html`](数据流图.html)，文字版见 [`架构说明.md`](架构说明.md)。

## 目录结构

```
aimin/
├── server/                  后端（Maven 多模块，父 POM 在此）
│   ├── aimin-gateway/       :8080  统一入口 + Sa-Token 鉴权 + CORS
│   ├── aimin-auth/          :8081  微信登录（网页端暂未接入）
│   ├── aimin-admin/         :8084  管理后台 / RBAC / 算术验证码
│   ├── aimin-agent/         :8087  诊断 Agent（检索、风险、生成、评测）
│   ├── aimin-common/        公共组件（base / ds / redis / cache / satoken）
│   ├── sql/                 建库与增量脚本
│   └── data/                知识库、向量快照、评测集（运行时数据）
├── admin-front/             前端（Vue 3 + Vite 6 + Element Plus）
├── 架构说明.md               落地架构（as-built）
├── 优化方案.md               审计与整改记录
└── ADR-001-可信度阶段.md     近期技术选型决策
```

## 技术栈

- **后端**：Java 21（`--enable-preview`）、Spring Boot 3.4.0、Spring Cloud 2024.0.0、Spring Cloud Alibaba 2023.0.1.0、Sa-Token 1.39.0、MyBatis-Plus 3.5.9
- **AI**：LangChain4j 0.36.2（锁定版本）、DeepSeek `deepseek-chat`、本地 ONNX `bge-small-zh`（512 维）
- **存储**：MySQL 8（库 `aimin`，19 张表）、Redis 6+（Sa-Token 会话与缓存）
- **前端**：Vue 3.5、Vite 6、Element Plus 2.9、Pinia 3

> 运行模式为**无 Nacos 的本地直连**：网关三条路由写死 `127.0.0.1:{8081,8084,8087}`，需 `SPRING_CLOUD_NACOS_*_ENABLED=false`。

## 快速开始

### 1. 环境

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | 必须带 `--enable-preview` 运行 |
| Maven | 3.8+ | 仓库路径避免中文，如 `C:/Users/Public/aimin-mvn-repo` |
| Node.js | 18+ | 前端构建 |
| MySQL | 8.x | 库名 `aimin` |
| Redis | 6+ | 默认 6379 |

### 2. 建库（按顺序执行）

```bash
mysql -uroot -p aimin < server/sql/aimin2025_05_10_01.sql
mysql -uroot -p aimin < server/sql/20261018_phase0_hardening.sql
mysql -uroot -p aimin < server/sql/20261019_demo_drugs.sql
mysql -uroot -p aimin < server/sql/20261019_phase2_persistence.sql
```

### 3. 启动后端

```bash
cd server
mvn -DskipTests -Dmaven.repo.local=C:/Users/Public/aimin-mvn-repo -pl aimin-agent -am package

# 四个进程，逐个启动（端口必须显式指定，环境变量可能泄漏覆盖）
SERVER__PORT=8081 java --enable-preview -jar aimin-auth/target/aimin-auth.jar
SERVER__PORT=8084 MYSQL_URL="jdbc:mysql://127.0.0.1:3306/aimin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" MYSQL_USERNAME=root MYSQL_PASSWORD=123456 java --enable-preview -jar aimin-admin/target/aimin-admin.jar
SERVER__PORT=8087 MYSQL_URL="..." MYSQL_USERNAME=root MYSQL_PASSWORD=123456 DEEPSEEK_API_KEY=sk-xxx java --enable-preview -jar aimin-agent/target/aimin-agent.jar
SERVER__PORT=8080 SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false java --enable-preview -jar aimin-gateway/target/aimin-gateway.jar
```

未配置 `DEEPSEEK_API_KEY` 时 agent 自动进入**演示模式**，返回固定模板但全链路可跑通。

### 4. 启动前端

```bash
cd admin-front
npm install
npm run dev     # http://localhost:5174
```

登录：`admin / 123456`（验证码为本地算术题，点题面可刷新）。

## 配置

| 变量 | 作用 | 默认 |
|---|---|---|
| `MYSQL_URL` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据源 | `127.0.0.1:3306/aimin`，root / 123456 |
| `DEEPSEEK_API_KEY` | 生成模型密钥 | 空 → 演示模式 |
| `AIMIN_CORS_ORIGINS` | 网关允许来源 | `localhost:5174,127.0.0.1:5174` |
| `aimin.agent.retrieval.min-vector-score` | 向量门控阈值 | `0.88` |
| `aimin.agent.retrieval.min-bm25-score` | BM25 门控阈值 | `6.5` |
| `aimin.agent.top-k` | 证据条数 | `5` |

> 换语料或换 embedding 模型后，两个阈值必须用评测集重新标定。

## 知识库与评测

- 演示语料 25 篇位于 `server/data/kb-demo/`，知识库为空时由 `DemoSeedRunner` 自动灌入。
- 上传入口：管理台「知识库」页，或 `POST /aimin-agent/agent/docs`（仅 pdf / md / markdown / txt，≤50MB）。
- 离线评测：

```bash
cd server
SERVER__PORT=18087 java --enable-preview -jar aimin-agent/target/aimin-agent.jar --aimin.eval.enabled=true
# 结果写入 data/eval/latest.json，跑完自动退出
```

## 医疗安全边界

- 处方、剂量、联合用药、特殊人群等关键词命中后**必须二次确认**才生成内容，规则硬编码于 `RiskEvaluator`，不可通过提示词绕过。
- 检索结果为空时直接拒答，不调用大模型编造。
- 回答中出现具体剂量时追加核对提示；免责声明固定输出、不可关闭。
- 会话与审计落库前对手机号、身份证号脱敏。

## 已知限制

- 向量库为 `InMemoryEmbeddingStore` + JSON 快照，**单机单实例**，不支持横向扩展。
- 待确认风险项、会话记忆存于进程内，服务重启即失效。
- 多轮指代依赖「上一问 + 当前问」拼接检索，长句省略主语时会漏判。
- 证据过滤按 `top1 × 0.6` 截断，对 RRF 分数压缩明显，实际削弱有限。
- agent 服务自身不校验 token，仅信任网关；生产部署请绑定 `127.0.0.1`。
- 仓库无自动化单测（离线 Maven 缺 `junit-platform-launcher`），回归依赖 CLI 评测。

## 文档

| 文件 | 内容 |
|---|---|
| [架构说明.md](架构说明.md) | 落地架构、模块职责、核心链路 |
| [优化方案.md](优化方案.md) | 审计问题清单与分阶段整改记录 |
| [ADR-001-可信度阶段.md](ADR-001-可信度阶段.md) | 检索与会话相关选型决策 |
| [架构图.html](架构图.html) / [数据流图.html](数据流图.html) | 可视化架构与数据流 |

## License

见 [server/LICENSE](server/LICENSE)。
