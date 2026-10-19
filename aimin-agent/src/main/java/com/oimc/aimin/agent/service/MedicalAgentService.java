package com.oimc.aimin.agent.service;

import com.oimc.aimin.agent.llm.LlmClient;
import com.oimc.aimin.agent.memory.SessionMemory;
import com.oimc.aimin.agent.model.AgentEvent;
import com.oimc.aimin.agent.model.Evidence;
import com.oimc.aimin.agent.retrieval.DrugRetriever;
import com.oimc.aimin.agent.retrieval.RrfFusion;
import com.oimc.aimin.agent.retrieval.VectorRetriever;
import com.oimc.aimin.agent.risk.RiskEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 医疗 Agent 编排：意图识别 → 混合检索(RRF) → 风险评估(两阶段) → 流式生成 → 记忆写回。
 * 医疗安全规则硬编码于 RiskEvaluator，不可被提示词绕过。
 */
@Service
public class MedicalAgentService {

    private static final Logger log = LoggerFactory.getLogger(MedicalAgentService.class);

    public static final String DISCLAIMER = "本回复由 AI 生成，仅供专业参考，不构成诊疗建议；涉及用药请以医生处方为准。";

    /** 待确认项有效期：5 分钟 */
    private static final long PENDING_TTL_MS = 5 * 60_000L;

    /** 待确认项上限，防止被刷爆内存 */
    private static final int MAX_PENDING = 2_000;

    /** 单条提问长度上限 */
    private static final int MAX_MESSAGE_CHARS = 2_000;

    private static final String SYSTEM_PROMPT = """
            你是一个面向医疗专业人士的辅助参考助手。规则：
            1. 回答必须仅基于提供的参考资料，禁止编造。资料未覆盖时明确说明"知识库未覆盖该问题"。
            2. 每条结论后必须附带引用角标（如 [1][2]），对应资料编号。
            3. 涉及处方、剂量、禁忌的内容必须附加风险提示。
            4. 输出结构：候选原因/分析 → 排查或处理建议 → 风险提示。
            5. 全程使用简体中文。
            """;

    private final LlmClient llm;
    private final VectorRetriever vectorRetriever;
    private final DrugRetriever drugRetriever;
    private final RiskEvaluator riskEvaluator;
    private final SessionMemory memory;
    private final int topK;

    /** 待确认问题：sessionId -> (confirmToken, question, createdAt) */
    private final Map<String, PendingAsk> pendingAsks = new ConcurrentHashMap<>();

    private record PendingAsk(String token, String question, long createdAt) {
    }

    public MedicalAgentService(LlmClient llm,
                               VectorRetriever vectorRetriever,
                               DrugRetriever drugRetriever,
                               RiskEvaluator riskEvaluator,
                               SessionMemory memory,
                               @Value("${aimin.agent.top-k}") int topK) {
        this.llm = llm;
        this.vectorRetriever = vectorRetriever;
        this.drugRetriever = drugRetriever;
        this.riskEvaluator = riskEvaluator;
        this.memory = memory;
        this.topK = topK;
    }

    /** 主入口：SSE 流式对话 */
    public void chat(String sessionId, String message, String confirmToken, SseEmitter emitter) {
        AtomicBoolean finished = new AtomicBoolean(false);
        Runnable finish = () -> {
            if (finished.compareAndSet(false, true)) {
                emitter.complete();
            }
        };
        Consumer<AgentEvent> send = event -> {
            if (finished.get()) {
                return;
            }
            try {
                emitter.send(event);
            } catch (IOException | IllegalStateException e) {
                // 客户端已断开：停止把后续内容再推给已关闭的连接
                log.debug("SSE 发送失败，关闭连接: {}", e.getMessage());
                finish.run();
            }
        };

        try {
            if (message == null || message.isBlank()) {
                send.accept(AgentEvent.of("error", Map.of("message", "问题不能为空")));
                finish.run();
                return;
            }
            if (message.length() > MAX_MESSAGE_CHARS) {
                send.accept(AgentEvent.of("error", Map.of("message", "问题过长，请控制在 " + MAX_MESSAGE_CHARS + " 字以内")));
                finish.run();
                return;
            }

            send.accept(AgentEvent.of("start", Map.of("mock", llm.isMock())));

            boolean confirmed = confirmToken != null && !confirmToken.isBlank();
            String question = message;

            if (confirmed) {
                // 确认令牌必须绑定到发起确认时的原问题：忽略客户端本次传入的 message，防止拿旧令牌问新问题
                PendingAsk ask = consume(sessionId, confirmToken);
                if (ask == null) {
                    send.accept(AgentEvent.of("error", Map.of("message", "确认令牌无效或已过期，请重新提问")));
                    send.accept(AgentEvent.of("done", Map.of()));
                    finish.run();
                    return;
                }
                question = ask.question();
            } else {
                // 1. 意图识别（轻量规则）：问候/闲聊直接回复
                if (message.strip().matches("(你好|您好|hi|hello|在吗|谢谢|thanks)[!！。~～]?")) {
                    String greeting = "您好，我是医疗辅助参考助手。请描述症状、药品或诊疗问题，我将基于知识库给出可追溯的参考信息。";
                    memory.addUser(sessionId, message);
                    for (int i = 0; i < greeting.length(); i += 3) {
                        send.accept(AgentEvent.of("delta", Map.of("text", greeting.substring(i, Math.min(i + 3, greeting.length())))));
                    }
                    memory.addAssistant(sessionId, greeting);
                    send.accept(AgentEvent.of("done", Map.of("disclaimer", DISCLAIMER)));
                    finish.run();
                    return;
                }

                // 2. 风险评估第一阶段：命中则中断出确认卡
                var hit = riskEvaluator.hit(message);
                if (hit.isPresent()) {
                    String token = UUID.randomUUID().toString();
                    rememberPending(sessionId, new PendingAsk(token, message, System.currentTimeMillis()));
                    memory.addUser(sessionId, message);
                    send.accept(AgentEvent.of("risk", Map.of(
                            "confirmToken", token,
                            "keyword", hit.get(),
                            "message", "该问题涉及「" + hit.get() + "」等高风险内容（处方/剂量级建议）。确认后 AI 才会生成相关内容，请确认使用场景合规。"
                    )));
                    send.accept(AgentEvent.of("done", Map.of("riskInterrupt", true)));
                    finish.run();
                    return;
                }
            }

            // 3. 混合检索 + RRF 融合
            List<Evidence> vectorHits = vectorRetriever.retrieve(question, topK);
            List<Evidence> drugHits = drugRetriever.retrieve(question, topK);
            List<Evidence> evidence = RrfFusion.fuse(List.of(vectorHits, drugHits), topK);

            send.accept(AgentEvent.of("evidence", Map.of("items", evidence)));

            // 4. 组装提示词
            StringBuilder ctx = new StringBuilder();
            for (Evidence ev : evidence) {
                ctx.append(ev.refId()).append("（来源: ").append(ev.title());
                if (ev.page() != null) {
                    ctx.append(" 第").append(ev.page()).append("页");
                }
                ctx.append("）\n").append(ev.snippet()).append("\n\n");
            }
            String userPrompt = (ctx.isEmpty() ? "【知识库未命中】\n\n" : "【参考资料】\n" + ctx + "【问题】\n")
                    + question;

            memory.addUser(sessionId, question);

            // 5. 流式生成
            StringBuilder answer = new StringBuilder();
            llm.stream(
                    SYSTEM_PROMPT,
                    userPrompt,
                    delta -> {
                        answer.append(delta);
                        send.accept(AgentEvent.of("delta", Map.of("text", delta)));
                    },
                    err -> {
                        log.error("LLM 调用失败", err);
                        send.accept(AgentEvent.of("error", Map.of("message", "模型调用失败，请稍后重试")));
                        send.accept(AgentEvent.of("done", Map.of()));
                        finish.run();
                    },
                    () -> {
                        memory.addAssistant(sessionId, answer.toString());
                        // 输出侧兜底：出现具体剂量时追加提示（已确认的高风险问题除外）
                        if (!confirmed) {
                            riskEvaluator.outputRisk(answer.toString()).ifPresent(category ->
                                    send.accept(AgentEvent.of("delta", Map.of(
                                            "text", "\n\n> ⚠️ 回答中出现了具体剂量信息，请结合患者情况与说明书核对后使用。"))));
                        }
                        send.accept(AgentEvent.of("done", Map.of("disclaimer", DISCLAIMER)));
                        finish.run();
                    }
            );
        } catch (Exception e) {
            log.error("Agent 处理失败", e);
            send.accept(AgentEvent.of("error", Map.of("message", "服务处理失败，请稍后重试")));
            finish.run();
        }
    }

    /** 两阶段第二阶段：确认后放行（校验通过后由 chat 原子消费令牌） */
    public void confirm(String sessionId, String confirmToken, SseEmitter emitter) {
        PendingAsk ask = peek(sessionId, confirmToken);
        if (ask == null) {
            try {
                emitter.send(AgentEvent.of("error", Map.of("message", "无待确认问题或令牌已过期")));
            } catch (IOException ignored) {
                // 客户端已断开
            }
            emitter.complete();
            return;
        }
        chat(sessionId, ask.question(), confirmToken, emitter);
    }

    public List<SessionMemory.Msg> history(String sessionId) {
        return memory.history(sessionId);
    }

    // ---------- 待确认项 ----------

    private void rememberPending(String sessionId, PendingAsk ask) {
        if (pendingAsks.size() >= MAX_PENDING) {
            pendingAsks.values().removeIf(this::isExpired);
        }
        pendingAsks.put(sessionId, ask);
    }

    private PendingAsk peek(String sessionId, String token) {
        PendingAsk ask = pendingAsks.get(sessionId);
        if (ask == null || isExpired(ask) || !ask.token().equals(token)) {
            return null;
        }
        return ask;
    }

    /** 原子消费：只有 token 正确且未过期才移除，避免错误令牌把待确认项销毁 */
    private PendingAsk consume(String sessionId, String token) {
        PendingAsk ask = peek(sessionId, token);
        if (ask == null) {
            return null;
        }
        return pendingAsks.remove(sessionId, ask) ? ask : null;
    }

    private boolean isExpired(PendingAsk ask) {
        return System.currentTimeMillis() - ask.createdAt() > PENDING_TTL_MS;
    }
}
