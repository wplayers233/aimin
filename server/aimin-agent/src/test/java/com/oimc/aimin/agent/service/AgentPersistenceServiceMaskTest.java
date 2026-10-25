package com.oimc.aimin.agent.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 只测脱敏纯函数；JdbcTemplate 传 null，不触库。
 */
class AgentPersistenceServiceMaskTest {

    private final AgentPersistenceService service = new AgentPersistenceService(null);

    @Test
    void 手机号脱敏() {
        assertEquals("138****5678", service.mask("13812345678"));
        assertEquals("199****0001", service.mask("19912340001"));
    }

    @Test
    void 非手机号数字串不变() {
        // 不满足 1[3-9] 开头的 11 位数字不应被误伤
        assertEquals("12345678901", service.mask("12345678901"));
        assertEquals("10086", service.mask("10086"));
    }

    @Test
    void 身份证脱敏() {
        assertEquals("110101********1234", service.mask("110101199001011234"));
        // 尾号保留输入原样的大小写，不做转换
        assertEquals("110101********123x", service.mask("11010119900101123x"));
        assertEquals("110101********123X", service.mask("11010119900101123X"));
    }

    @Test
    void 混合文本中同时脱敏() {
        assertEquals("联系138****5678，证件110101********1234",
                service.mask("联系13812345678，证件110101199001011234"));
    }

    @Test
    void null与普通文本() {
        assertNull(service.mask(null));
        assertEquals("布洛芬的适应症", service.mask("布洛芬的适应症"));
    }
}
