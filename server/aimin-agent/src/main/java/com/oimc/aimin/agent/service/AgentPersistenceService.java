package com.oimc.aimin.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    // 手机号带数字边界：避免长数字串（证件号、卡号）内的子串被误当手机号脱敏
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(1[3-9]\\d)\\d{4}(\\d{4})(?!\\d)");
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
        // 必须先身份证后手机号：18 位身份证内含 1[3-9] 开头的 11 位子串，
        // 先跑手机号会把身份证错误截断（出生日期裸露）且让身份证规则失配
        String masked = ID_CARD.matcher(text).replaceAll("$1********$2");
        return PHONE.matcher(masked).replaceAll("$1****$2");
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

    // ---------- 工作台统计（数据全部来自落库表，天然受 90 天清理窗口约束） ----------

    public long countSessions() {
        Long n = drugJdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_agent_session", Long.class);
        return n == null ? 0 : n;
    }

    public long countMessagesByRole(String role) {
        Long n = drugJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_agent_message WHERE role=?", Long.class, role);
        return n == null ? 0 : n;
    }

    /** 按 action 计数审计记录；未出现的 action 返回 0，保证前端拿到的 key 齐全 */
    public Map<String, Long> countAuditByActions(List<String> actions) {
        String placeholders = String.join(",", actions.stream().map(a -> "?").toList());
        Map<String, Long> result = new LinkedHashMap<>();
        actions.forEach(a -> result.put(a, 0L));
        drugJdbcTemplate.query(
                "SELECT action, COUNT(*) AS cnt FROM t_agent_audit WHERE action IN (" + placeholders + ") GROUP BY action",
                rs -> {
                    String action = rs.getString("action");
                    if (result.containsKey(action)) {
                        result.put(action, rs.getLong("cnt"));
                    }
                },
                actions.toArray());
        return result;
    }

    /** 近 N 天按天按角色计数；日期连续填零（MM-dd），前端无需补点 */
    public List<Map<String, Object>> dailyMessageCounts(int days) {
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        Map<LocalDate, Map<String, Long>> byDate = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            byDate.put(start.plusDays(i), new LinkedHashMap<>());
        }
        drugJdbcTemplate.query(
                "SELECT DATE(created_at) AS d, role, COUNT(*) AS cnt FROM t_agent_message "
                        + "WHERE created_at >= ? GROUP BY DATE(created_at), role",
                rs -> {
                    Map<String, Long> roles = byDate.get(rs.getDate("d").toLocalDate());
                    if (roles != null) {
                        roles.put(rs.getString("role"), rs.getLong("cnt"));
                    }
                },
                Timestamp.valueOf(start.atStartOfDay()));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>(days);
        byDate.forEach((date, roles) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", fmt.format(date));
            row.put("questions", roles.getOrDefault("user", 0L));
            row.put("answers", roles.getOrDefault("assistant", 0L));
            result.add(row);
        });
        return result;
    }
}
