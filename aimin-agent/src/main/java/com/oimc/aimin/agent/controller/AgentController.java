package com.oimc.aimin.agent.controller;

import com.oimc.aimin.agent.model.ChatRequest;
import com.oimc.aimin.agent.service.AgentPersistenceService;
import com.oimc.aimin.agent.service.IngestionService;
import com.oimc.aimin.agent.service.MedicalAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final MedicalAgentService agentService;
    private final IngestionService ingestionService;
    private final AgentPersistenceService persistenceService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(MedicalAgentService agentService, IngestionService ingestionService,
                           AgentPersistenceService persistenceService) {
        this.agentService = agentService;
        this.ingestionService = ingestionService;
        this.persistenceService = persistenceService;
    }

    /** 对话（SSE 流式） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(2));
        executor.submit(() -> {
            try {
                agentService.chat(request.sessionId(), request.message(), null, emitter);
            } catch (Exception e) {
                log.error("chat 执行失败", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** 风险确认（两阶段第二阶段） */
    @PostMapping(value = "/confirm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter confirm(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(2));
        executor.submit(() -> {
            try {
                agentService.confirm(request.sessionId(), request.confirmToken(), emitter);
            } catch (Exception e) {
                log.error("confirm 执行失败", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** 运行状态：模型模式 + 知识库规模（前端用于展示“演示模式/已接入”） */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        var docs = ingestionService.listDocs();
        int chunks = docs.stream().mapToInt(IngestionService.DocInfo::chunks).sum();
        return ResponseEntity.ok(Map.of("code", 200, "msg", "ok", "data", Map.of(
                "mock", agentService.isMock(),
                "docs", docs.size(),
                "chunks", chunks)));
    }

    /** 会话历史（优先读库，进程内记忆兜底） */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> history(@PathVariable String sessionId) {
        List<?> messages = persistenceService.listMessages(sessionId);
        if (messages.isEmpty()) {
            messages = agentService.history(sessionId);
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "ok", "data", messages));
    }

    /** 会话列表（按最近更新时间倒序） */    @GetMapping("/sessions")
    public ResponseEntity<?> sessions(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(Map.of("code", 200, "msg", "ok",
                "data", persistenceService.listSessions(limit)));
    }

    /** 用户放弃高风险问题：清待确认项并记审计 */
    @PostMapping("/risk/reject")
    public ResponseEntity<?> reject(@RequestBody ChatRequest request) {
        boolean removed = agentService.rejectPending(request.sessionId());
        return ResponseEntity.ok(Map.of("code", 200, "msg", "ok", "data", Map.of("rejected", removed)));
    }

    /** 知识文档上传入库 */
    @PostMapping("/docs")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(Map.of("code", 200, "msg", "ok", "data", ingestionService.ingest(file)));
        } catch (IllegalArgumentException e) {
            // 业务校验类错误（格式不支持、空文件等）可以直接告诉用户
            return ResponseEntity.ok(Map.of("code", 400, "msg", e.getMessage()));
        } catch (Exception e) {
            log.error("文档入库失败", e);
            return ResponseEntity.ok(Map.of("code", 500, "msg", "文档解析或入库失败，请检查文件后重试"));
        }
    }

    /** 已入库文档列表（统一 Result 结构，避免前端拦截器把裸数组判为失败） */
    @GetMapping("/docs")
    public ResponseEntity<?> docs() {
        return ResponseEntity.ok(Map.of("code", 200, "msg", "ok", "data", ingestionService.listDocs()));
    }
}
