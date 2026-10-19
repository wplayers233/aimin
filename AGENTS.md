# AGENTS.md — aimin 医疗 Agent 工作台

> 面向 AI Agent / 新加入开发者的项目说明书。读完即可上手开发，无需再问"代码在哪"。

## 项目定位

aimin 是一个**医疗诊断 Agent 工作台**（网页端，无小程序）：Spring Cloud 微服务后端 + Vue3 管理前端。
核心能力：多模态知识库入库（PDF/MD）→ 混合检索（向量 + 药品结构化，RRF 融合）→ 医疗风险两阶段确认 → SSE 流式诊断回答（带可追溯证据引用）。

技术栈：Java 21（`--enable-preview` 编译）/ Spring Boot 3.4.0 / Spring Cloud 2024.0.0 / LangChain4j 0.36.2 / Vue 3 + Vite + Element Plus / MySQL + Redis（本机 Windows 服务）。

**运行模式**：无 Nacos 的本地直连模式（bootstrap.yml 已改直连路由）。改回 Nacos 需还原三个 bootstrap.yml 的 `config.import` 与 gateway 路由 `lb://` 前缀。

## 目录结构与职责

```
aimin/                          # 工作区根
├── server/                     # ★ 后端（Maven 多模块，父 pom 在此）
│   ├── pom.xml                 # 父 POM：版本管理、--enable-preview、模块注册
│   ├── docker-compose-infra.yml# 中间件编排（备用；本机已直装 MySQL/Redis，平时不用）
│   ├── sql/                    # 全量建库脚本（aimin 库，16+ 表）
│   ├── docs/                   # 架构方案文档（medical-agent-architecture.md）
│   ├── data/                   # agent 运行时数据（向量库持久化 JSON、上传文档）
│   ├── logs_*.log              # 各服务启动日志（排错先看这里）
│   │
│   ├── aimin-gateway/          # 网关 :8080（Netty）。统一入口 + 鉴权，
│   │                           # 直连路由 /aimin-{auth,admin,agent}/** → localhost；
│   │                           # 未登记路径默认拒绝（StrategyFactory → DenyPathStrategy）
│   ├── aimin-auth/             # 认证服务 :8081。微信登录/鉴权（网页端暂不用）
│   ├── aimin-admin/            # 后台管理 :8084。用户/角色/科室/医生管理；
│   │                           # 登录验证码=本地算术题（GET /public/auth/captcha）
│   ├── aimin-agent/            # ★ 医疗诊断 Agent :8085 逻辑所在 → 实际端口 8087
│   │   └── com.oimc.aimin.agent
│   │       ├── AgentApplication        # 启动类（排除 DataSourceAutoConfiguration）
│   │       ├── config/AgentBeans       # 装配：embedding 模型、向量库、LLM(有key→DeepSeek/无key→Mock)、
│   │       │                           #   药品库 JdbcTemplate（CORS 统一由网关处理）
│   │       ├── controller/AgentController
│   │       │       POST /agent/chat    # 对话（SSE 流式）
│   │       │       POST /agent/confirm # 风险确认（两阶段第二段）
│   │       │       GET  /agent/sessions/{id}/messages
│   │       │       POST /agent/docs    # 知识文档上传入库
│   │       │       GET  /agent/docs    # 已入库文档列表
│   │       ├── service/MedicalAgentService  # ★ 编排核心：意图→风险→检索→流式生成→记忆
│   │       ├── service/IngestionService     # 入库管线：PDF/MD/TXT（白名单）→按页提文本→章节切片(512字)→向量化→持久化
│   │       ├── retrieval/VectorRetriever    # 向量召回（bge-small-zh 本地 ONNX）
│   │       ├── retrieval/DrugRetriever      # 药品库召回（t_drug 表 LIKE 查询）
│   │       ├── retrieval/RrfFusion          # RRF 多路融合 + 引用编号 [1][2]
│   │       ├── risk/RiskEvaluator           # ★ 医疗安全规则（处方/剂量/禁忌关键词，
│   │       │                                #   命中即两阶段中断。硬编码，不可被提示词绕过）
│   │       ├── memory/SessionMemory         # 会话记忆（进程内 Map，上限40条）
│   │       ├── llm/LlmClient                # LLM 抽象（stream 三回调）
│   │       ├── llm/DeepSeekLlmClient        # DeepSeek 实现（OpenAI 兼容协议）
│   │       ├── llm/MockLlmClient            # 演示模式（无 DEEPSEEK_API_KEY 时自动启用）
│   │       └── model/                       # DTO：AgentEvent(SSE事件) / ChatRequest / Evidence(证据引用)
│   └── aimin-common/            # 公共组件库（被各服务依赖）
│       ├── aimin-base           # 基础响应/异常/常量 + base.yml
│       ├── aimin-ds             # 数据源封装 + ds.yml（MySQL 连接模板）
│       ├── aimin-redis          # Redis 封装 + redis.yml
│       ├── aimin-cache          # Caffeine/Spring Cache
│       ├── aimin-satoken        # 登录鉴权框架封装
│       ├── aimin-es / aimin-mq / aimin-mongo / aimin-file / aimin-generator
│
└── admin-front/                # ★ 网页前端（Vue3 + Vite + Element Plus）
    └── src
        ├── main.js             # 入口：ElementPlus、pinia、router、图标全量注册
        ├── router/index.js     # 静态路由（login/agent/knowledge/dashboard）+ 动态路由合并
        ├── styles/index.scss   # ★ 苹果风全局主题（#0A84FF/圆角12px/EP变量覆盖），改样式从这进
        ├── style.css           # 空（Vite 模板残留已清）
        ├── layout/index.vue    # 主框架：毛玻璃侧栏 + 顶栏（菜单由 router.getRoutes 动态生成）
        ├── api/                # 接口封装：auth.js(登录+验证码) / agent.js(★SSE流式+文档管理) / system/*
        ├── store/              # pinia：user(token) / route(动态菜单) / app(loading)
        ├── utils/request.js    # axios 实例（baseURL=${VITE_API_BASE_URL}，dev 指向网关 /aimin-admin）；
        │                       # 401 统一清 token 单例提示；agent 的 SSE/文档走 /api 代理
        ├── utils/dynamicRoutes.js # 后端菜单→vue-router 动态注册/重置
        └── views/
            ├── login/          # 登录页（毛玻璃卡；算术验证码，点题面可刷新）
            ├── agent/          # ★ 诊断工作台：三栏布局、SSE打字机、证据卡、琥珀确认卡（AbortController 可中断）
            ├── knowledge/      # 知识库管理（拖拽上传入库 + 文档列表）
            ├── dashboard/      # 首页
            └── system|doctor|operations  # 原有管理页面（药品页已随 aimin-drug 下线）
```

## 启动与验证

```bash
# 前置：本机 MySQL(root/123456, 库 aimin) 与 Redis(6379) 为 Windows 服务自启
# jar 已构建；改代码后：mvn -DskipTests -Dmaven.repo.local=C:/Users/Public/aimin-mvn-repo -pl <模块> [-am] package

# 四个服务（Git Bash，在 server/ 下；必须带 --enable-preview 和 SERVER__PORT）
export SERVER__PORT=8081 MYSQL_URL="jdbc:mysql://127.0.0.1:3306/aimin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" MYSQL_USERNAME=root MYSQL_PASSWORD=123456 SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false SPRING_CLOUD_NACOS_CONFIG_ENABLED=false WX_APPID=wx0000000000000000 WX_SECRET=dummy ALIBABA_CLOUD_ACCESS_KEY_ID=dummy ALIBABA_CLOUD_ACCESS_KEY_SECRET=dummy
"C:/Program Files/Java/jdk-21.0.12/bin/java" --enable-preview -jar aimin-auth/target/aimin-auth.jar &
export SERVER__PORT=8084 ...同上...  # admin
export SERVER__PORT=8087 MYSQL_URL=... MYSQL_USERNAME=root MYSQL_PASSWORD=123456  # agent（仅需这三个）
export SERVER__PORT=8080 SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false SPRING_CLOUD_NACOS_CONFIG_ENABLED=false  # gateway
# 前端：cd ../admin-front && npm run dev   （端口 5173，占用则自动 5174）
```

验证：
```bash
# 1) 取验证码 → 2) 带 captchaId/captchaAnswer 登录
curl http://127.0.0.1:8080/aimin-admin/public/auth/captcha
curl -X POST http://127.0.0.1:8080/aimin-admin/public/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456","captchaId":"<上一步的 captchaId>","captchaAnswer":"<题面算出的答案>"}'
# → code:200 + tokenValue/tokenName(aimin-admin-token) + permissions
```
agent 聊天走 `/aimin-agent/agent/chat`（SSE，事件：start/evidence/delta/risk/done），**必须带 `aimin-admin-token` 头**（网关已纳管，匿名 401）。
会话/审计接口：`GET /aimin-agent/agent/sessions`、`GET /aimin-agent/agent/sessions/{id}/messages`、`POST /aimin-agent/agent/risk/reject`（放弃高风险问题）。

评测（离线 CLI，跑完自动退出，结果写 `aimin-agent/data/eval/latest.json`）：
```bash
SERVER__PORT=18087 java --enable-preview -jar aimin-agent/target/aimin-agent.jar --aimin.eval.enabled=true
```

离线构建（外网不可达时）：`mvn -o -s ../.mvn-offline-settings.xml '-Daether.enhancedLocalRepository.trackingFilename=_none.repositories' '-Daether.artifactResolver.simpleLrmInterop=true' -DskipTests -pl :aimin-admin -am compile`（cwd=server，JAVA_HOME 指 JDK21）。

## 铁律与已踩的坑

1. **运行必须 `--enable-preview`**（父 POM 编译启用了 Java 21 预览特性）。
2. **启动必须设 `SERVER__PORT`**：WorkBuddy 等环境会泄漏 `SERVER__PORT` 变量，不设会被抢占端口。
3. **LangChain4j 锁定 0.36.2**：1.x 没有 in-process embedding 模型（`langchain4j-embed-*` 不存在，正确坐标是 `langchain4j-embeddings-bge-small-zh`）。升级前先验证坐标存在。
4. **医疗安全**：RiskEvaluator 的关键词规则是合规底线，禁止删除或提示词绕过；处方/剂量内容必须两阶段确认后才能生成；免责声明固定输出。
5. **Maven 用阿里云镜像**（settings.xml 已配，repo.maven.org 直连 403）；本地仓库 `C:/Users/Public/aimin-mvn-repo`（ASCII 路径，避免中文用户名乱码）。
6. **element-plus 样式改动**走 `styles/index.scss` 的 CSS 变量；layout 的 style 标签必须 `lang="scss"`（曾因 `//` 注释炸编译）。
7. **服务在会话后台任务中运行，会话结束即停**——重启按上面命令，jar 无需重构建（除非改了代码）。
8. 外网受限环境（FlClash TUN）下 docker 拉镜像/Maven Central 常失败：优先国内源，向量库当前用 InMemory+文件持久化，Redis Stack 就绪后切 `aimin.agent.vector-store: redis`。
9. **鉴权不允许再放行**：网关 `StrategyFactory` 未匹配路径默认 `DenyPathStrategy`；admin 的 `SaTokenConfigure` 除 `/public/**` 外全部要求 `StpAdminUtil` 登录，`@SaCheckPermission` 必须带 `type = "ADMIN"`（默认 StpUtil 逻辑是另一套 token）。
10. **验证码不可关闭**：`/public/auth/captcha` 发本地算术题，TTL 3 分钟、一次性消费；登录必须带 `captchaId` + `captchaAnswer`，阿里云 SDK 已移除，勿再加回 `aimin.captcha.enabled` 开关。
11. **上传只允许 pdf/md/markdown/txt**：`IngestionService.sanitizeFileName` 会剥离目录、过滤非法字符并校验落地路径必须在上传目录内；新增格式先改白名单。
12. **数据库增量脚本**：`server/sql/20261018_phase0_hardening.sql`（t_admin 唯一索引 + 删除药品菜单权限），`server/sql/20261019_demo_drugs.sql`（100 条演示药品），`server/sql/20261019_phase2_persistence.sql`（会话/消息/审计三表），新环境建库后按序执行。
13. **会话与审计必须落库**：Agent 侧统一走 `AgentPersistenceService`（手机号/身份证落库前脱敏），后台登录与管理员/角色变更走 `AdminAuditService`；两者都写 `t_agent_audit`，不要绕过它们直接 insert 明文。
14. **评测不是单测**：离线仓库缺 `junit-platform-launcher`，`mvn test` 跑不起来；用 `--aimin.eval.enabled=true` 的 CLI。检索阈值 `min-vector-score=0.88` / `min-bm25-score=6.5` 由该数据集标定，换语料或换 embedding 后必须重跑并重标。
15. **演示语料只在知识库为空时灌入**（`aimin.seed-kb.enabled`，目录 `aimin-agent/data/kb-demo`）；要重灌就同时删 `data/agent-docs.json` 与 `data/agent-embeddings.json`。

## 待办（Roadmap）

- [ ] 配置 `DEEPSEEK_API_KEY` 环境变量重启 agent → 退出演示模式
- [ ] 部署 Redis Stack(6380) 替换 InMemory 向量库；会话记忆迁 Redis
- [ ] 多模态：检查单图片识别（通义 qwen-vl，接口已预留 attachments）
- [ ] 右侧患者档案面板接真实数据（长期记忆/患者表）
- [ ] 评估集（faithfulness）与知识库审核流
