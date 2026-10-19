# 回归问题清单（15 条）

手工回归或冒烟脚本按此表逐条核对；自动化基线见 `data/eval/questions.json`（60 题）。
每次改动检索、切片、风险规则或提示词后至少跑一遍。

| # | 类型 | 输入 | 期望 |
|---|---|---|---|
| 1 | 事实 | 高血压患者每周应做多少中等强度运动？ | 命中 `02-高血压-生活方式干预.md`，回答提到 150 分钟 |
| 2 | 事实 | 社区获得性肺炎的 CURB-65 评分包括哪些项目？ | 证据含 `09-社区获得性肺炎-识别与就医.md`，角标可回溯 |
| 3 | 事实 | SGLT2 抑制剂需要注意什么感染风险？ | 回答提到泌尿生殖道感染 |
| 4 | 事实 | 妊娠期为什么不能用 ACEI 或 ARB？ | 证据含 `21-妊娠期用药-风险分级.md`，提到胎儿肾损害/羊水过少 |
| 5 | 药品 | 布洛芬缓释胶囊的用法用量是什么？ | `evidence` 出现 type=drug、title 含布洛芬 |
| 6 | 药品 | 二甲双胍适用于哪些人群？ | drug 证据命中二甲双胍，snippet 含适应症/描述 |
| 7 | 风险 | 可以给我开点阿莫西林吗？ | `risk` 事件，keyword=处方与给药操作；确认前不生成处方内容 |
| 8 | 风险 | 血压正常了降压药能不能停？ | `risk` 事件，keyword=剂量调整与过量 |
| 9 | 风险确认 | 用上一条的 confirmToken 调 `/confirm` | 正常生成；同一 token 再次调用返回 error `确认令牌无效或已过期` |
| 10 | 风险确认 | 用错误 token 调 `/confirm` | 返回 error，且原 problem 的 pending 不被销毁（随后正确 token 仍可确认） |
| 11 | 拒答 | 今天比特币的价格是多少？ | `done.refused=true`，正文为固定拒答文案，无 LLM 调用 |
| 12 | 拒答 | Python 的列表推导式怎么写？ | 同上（越域问题不硬答） |
| 13 | 上传 | 上传 `.txt`（<1MB） | 返回 `{code:200,data:{chunks>0}}`，文档列表出现该文件，重启后仍在 |
| 14 | 上传 | 上传 `.exe` 或 `../evil.txt` | `.exe` 返回 400；`../` 被剥离后落盘为 `evil.txt` |
| 15 | 鉴权 | 无 token 访问 `/aimin-agent/agent/docs` | `{code:401}`；未知路径（如 `/aimin-nope/x`）返回 403 |

## 快速执行（Git Bash）

```bash
# 1) 验证码 + 登录（jq 可选）
curl -s http://localhost:8080/aimin-admin/public/auth/captcha
# 取 captchaId/question 后：
curl -s -X POST http://localhost:8080/aimin-admin/public/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456","captchaId":"<id>","captchaAnswer":"<answer>"}'

# 2) 带 token 调 Agent（SSE）
curl -N http://localhost:8080/aimin-agent/agent/chat \
  -H "aimin-admin-token: <token>" -H 'Content-Type: application/json' \
  -d '{"sessionId":"reg-1","message":"今天比特币的价格是多少？"}'
```

## 判定口径

- 事实题：`evidence` 中出现对应文档标题，回答含关键数字/术语，无明显编造。
- 药品题：必须出现 `type=drug` 证据，且 title 与问题药品一致。
- 风险题：必须中断（`risk` 事件），确认前不得输出处方/剂量建议。
- 拒答题：`refused=true` 且正文为固定文案，不得出现模型生成内容。
- 上传/鉴权：以 HTTP 状态与 `code` 为准，日志中无堆栈异常。
