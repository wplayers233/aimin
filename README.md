# Aimin 医疗诊断 Agent 工作台 · 后端

面向医疗专业人士的辅助参考系统：本地知识库 RAG + 药品库检索 + 两阶段高风险确认，回答带可追溯证据角标。
项目由开源药品管理后台改造而来，当前只保留真实运行所需的四个服务。

## 模块与端口

| 模块 | 端口 | 说明 |
|---|---|---|
| aimin-gateway | 8080 | 统一入口；未登记路径默认拒绝（403），CORS 仅放行本地前端 |
| aimin-auth | 8081 | 小程序/用户端认证（当前工作台未使用） |
| aimin-admin | 8084 | 管理后台：登录（本地算术验证码）、管理员/角色/权限 |
| aimin-agent | 8087 | 医疗 Agent：SSE 对话、知识库上传、混合检索、评测 |
| aimin-common | - | base / ds / redis / satoken / cache / file 等公共组件 |

## 技术栈

- JDK 21（`--enable-preview`）、Spring Boot 3.4.0、Spring Cloud 2024.0.0
- Sa-Token 1.39.0（admin/user 双账号体系，请求头 `aimin-admin-token` / `aimin-auth-token`）
- MyBatis-Plus 3.5.9、MySQL 8、Redis
- LangChain4j **0.36.2**（锁死：1.x 移除了进程内 ONNX embedding）、DeepSeek（OpenAI 兼容协议）
- bge-small-zh 本地 ONNX embedding + InMemoryEmbeddingStore（JSON 持久化）
- 自研中文 bigram BM25 + RRF 混合检索（无新依赖、无外部检索服务）
- 前端：Vue 3 + Vite 6 + Element Plus + Pinia（见 `../admin-front`）

## 本地启动

```bash
# 环境变量（Git Bash 示例）
export SERVER__PORT=8084 MYSQL_URL='jdbc:mysql://127.0.0.1:3306/aimin?...' \
       MYSQL_USERNAME=root MYSQL_PASSWORD=123456 \
       SPRING_CLOUD_NACOS_CONFIG_ENABLED=false SPRING_CLOUD_NACOS_DISCOVERY_ENABLED=false

cd aimin-admin  && java --enable-preview -jar target/aimin-admin.jar
cd aimin-agent  && java --enable-preview -jar target/aimin-agent.jar     # 8087
cd aimin-auth   && java --enable-preview -jar target/aimin-auth.jar      # 8081
cd aimin-gateway && java --enable-preview -jar target/aimin-gateway.jar  # 8080
```

- 必须覆盖 `SERVER__PORT`（WorkBuddy 会泄漏该变量，Spring 松散绑定会抢走 `server.port`）。
- Agent 不配 `DEEPSEEK_API_KEY` 时自动进入演示模式（MockLLM，`start` 事件里 `mock:true`）。
- 离线构建（本机仓库已完整缓存）：

```bash
mvn -o -s ../.mvn-offline-settings.xml \
    -Daether.enhancedLocalRepository.trackingFilename=_none.repositories \
    -Daether.artifactResolver.simpleLrmInterop=true \
    -DskipTests -pl :aimin-gateway,:aimin-auth,:aimin-admin,:aimin-agent -am package
```

## 评测

评测集 `aimin-agent/data/eval/questions.json`：60 题（fact 30 / drug 12 / risk 10 / unanswerable 8）。
评测器为离线 CLI，跑完自动退出并写 `data/eval/latest.json`：

```bash
java --enable-preview -jar aimin-agent/target/aimin-agent.jar --aimin.eval.enabled=true
```

| 指标 | Phase A 向量基线 | Phase B 混合检索 |
|---|---|---|
| Recall@5 | 0.867 | **0.967** |
| MRR | 0.668 | **0.781** |
| 药品 Acc@5 | 0.000 | **1.000** |
| 风险准确率 | 0.800 | **1.000** |
| 拒答率 | 0.000 | **1.000** |

拒答门控：向量 top1 < 0.88 **且** BM25 top1 < 6.5 时返回固定拒答文案，不调用大模型。
阈值由评测集诊断分数标定（`latest.json` 的 `diagnostics`），更换语料/embedding 后需重新标定。

## 会话、审计与治理

- 三张表：`t_agent_session` / `t_agent_message` / `t_agent_audit`（`sql/20261019_phase2_persistence.sql`）。
- `AgentPersistenceService`：会话 upsert、消息落库、审计写入、手机号与身份证号落库前脱敏；每日 03:30 清理 90 天前数据。
- 后台登录（成功/失败）与管理员、角色变更由 `AdminAuditService` 写入同一张审计表。
- 前端可拉取历史会话并回放；风险问题可“跳过，不生成”（记 `RISK_REJECTED` 审计）。

## 目录

```
aimin-agent/
  src/main/java/com/oimc/aimin/agent/
    service/      MedicalAgentService（编排）、IngestionService（入库/切片/注册表）
    retrieval/    VectorRetriever、Bm25Index、HybridRetriever、DrugRetriever、RrfFusion
    risk/         RiskEvaluator（硬编码规则，提示词不可绕过）
    eval/         EvaluationRunner、EvalQuestion
  data/           kb-demo（25 篇演示语料）、docs（上传留档）、eval（评测集与结果）
```

## 已知限制

- 向量库与文档注册表是进程内 + 本地 JSON，多实例部署需换 Redis Stack / PGVector。
- 演示语料仅覆盖 25 个常见主题，不能替代真实指南库。
- 评测集为内部自建，指标只用于回归对比，不代表临床效果。
- 未接入真实 DeepSeek 时回答为演示文本，不能用于验收回答质量。

更多约定见 [AGENTS.md](AGENTS.md)、架构说明 [docs/medical-agent-architecture.md](docs/medical-agent-architecture.md)、
整体优化记录 [../优化方案.md](../优化方案.md)。
