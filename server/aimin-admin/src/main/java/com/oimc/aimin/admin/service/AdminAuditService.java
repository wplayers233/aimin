package com.oimc.aimin.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 后台审计：登录、管理员变更、角色变更。
 * 与 Agent 共用 t_agent_audit 表（同一数据库），detail 统一做手机号/身份证脱敏。
 */
@Service
public class AdminAuditService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditService.class);

    private static final Pattern PHONE = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)(\\d{6})\\d{8}(\\d{3}[\\dXx])(?!\\d)");

    private final JdbcTemplate jdbcTemplate;

    public AdminAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void audit(String actor, String action, String target, String detail) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO t_agent_audit (actor, action, target, detail) VALUES (?, ?, ?, ?)",
                    truncate(actor, 64), truncate(action, 32), truncate(target, 64), mask(detail));
        } catch (Exception e) {
            log.warn("写审计日志失败 action={} target={}: {}", action, target, e.getMessage());
        }
    }

    private String mask(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String masked = PHONE.matcher(text).replaceAll("$1****$2");
        masked = ID_CARD.matcher(masked).replaceAll("$1********$2");
        return truncate(masked, 500);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
