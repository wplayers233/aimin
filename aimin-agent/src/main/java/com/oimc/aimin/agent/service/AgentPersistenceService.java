package com.oimc.aimin.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 会话、消息与审计的落库服务。
 *
 * 说明：Agent 侧不区分登录账号（令牌校验在网关与 admin 完成），admin_id 预留但不填充；
 * 所有文本在入库前做手机号 / 身份证脱敏，避免把敏感信息带进审计库。
 */
@Service
public class AgentPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(AgentPersistenceService.class);

    private static final Pattern PHONE = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)(\\d{6})\\d{8}(\\d{3}[\\dXx])(?!\\d)");
    private static final int TITLE_MAX = 60;

    private final JdbcTemplate drugJdbcTemplate;

    public AgentPersistenceService(JdbcTemplate drugJdbcTemplate) {
        this.drugJdbcTemplate = drugJdbcTemplate;
    }

    public String mask(String text) {
        if (text == null) {
            return null;
        }
        String masked = PHONE.matcher(text).replaceAll("$1****$2");
        return ID_CARD.matcher(masked).replaceAll("$1********$2");
    }

    public void ensureSession(String sessionId, String firstQuestion) {
        try {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            String title = mask(firstQuestion);
            if (title != null && title.length() > TITLE_MAX) {
                title = title.substring(0, TITLE_MAX);
            }
            drugJdbcTemplate.update(
                    "INSERT INTO t_agent_session(session_id, title, created_at, updated_at) VALUES (?,?,?,?) "
                            + "ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at), title = COALESCE(title, VALUES(title))",
                    sessionId, title, now, now);
        } catch (Exception e) {
            log.warn("会话落库失败 session={} err={}", sessionId, e.getMessage());
        }
    }

    public void saveMessage(String sessionId, String role, String content) {
        try {
            drugJdbcTemplate.update(
                    "INSERT INTO t_agent_message(session_id, role, content, created_at) VALUES (?,?,?,?)",
                    sessionId, role, mask(content), Timestamp.valueOf(LocalDateTime.now()));
            drugJdbcTemplate.update("UPDATE t_agent_session SET updated_at=? WHERE session_id=?",
                    Timestamp.valueOf(LocalDateTime.now()), sessionId);
        } catch (Exception e) {
            log.warn("消息落库失败 session={} role={} err={}", sessionId, role, e.getMessage());
        }
    }

    public List<Map<String, Object>> listSessions(int limit) {
        return drugJdbcTemplate.queryForList(
                "SELECT s.session_id AS sessionId, s.title, s.created_at AS createdAt, s.updated_at AS updatedAt, "
                        + "(SELECT COUNT(*) FROM t_agent_message m WHERE m.session_id = s.session_id) AS messageCount "
                        + "FROM t_agent_session s ORDER BY s.updated_at DESC LIMIT ?", Math.max(1, Math.min(limit, 200)));
    }

    public List<Map<String, Object>> listMessages(String sessionId) {
        return drugJdbcTemplate.queryForList(
                "SELECT role, content, created_at AS timestamp FROM t_agent_message WHERE session_id=? ORDER BY id",
                sessionId);
    }

    public void audit(String actor, String action, String target, String detail) {
        try {
            drugJdbcTemplate.update(
                    "INSERT INTO t_agent_audit(actor, action, target, detail, created_at) VALUES (?,?,?,?,?)",
                    actor, action, target, mask(detail), Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            log.warn("审计落库失败 action={} err={}", action, e.getMessage());
        }
    }

    /** 每天 03:30 清理 90 天前的会话、消息与审计记录 */
    @Scheduled(cron = "0 30 3 * * *")
    public void purgeExpired() {
        Timestamp before = Timestamp.valueOf(LocalDateTime.now().minusDays(90));
        try {
            int messages = drugJdbcTemplate.update("DELETE FROM t_agent_message WHERE created_at < ?", before);
            int sessions = drugJdbcTemplate.update("DELETE FROM t_agent_session WHERE updated_at < ?", before);
            int audits = drugJdbcTemplate.update("DELETE FROM t_agent_audit WHERE created_at < ?", before);
            log.info("审计清理完成：会话 {} 条、消息 {} 条、审计 {} 条（90 天前）", sessions, messages, audits);
        } catch (Exception e) {
            log.warn("审计清理失败 err={}", e.getMessage());
        }
    }
}
