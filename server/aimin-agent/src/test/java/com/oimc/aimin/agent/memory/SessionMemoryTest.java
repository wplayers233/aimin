package com.oimc.aimin.agent.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionMemoryTest {

    private final SessionMemory memory = new SessionMemory();

    @Test
    void 超过40条裁掉最早消息() {
        for (int i = 1; i <= 45; i++) {
            memory.addUser("s1", "消息" + i);
        }
        List<SessionMemory.Msg> history = memory.history("s1");
        assertEquals(40, history.size());
        assertEquals("消息6", history.get(0).content());
        assertEquals("消息45", history.get(history.size() - 1).content());
    }

    @Test
    void 会话之间相互隔离() {
        memory.addUser("s1", "会话一");
        memory.addUser("s2", "会话二");
        assertEquals(1, memory.history("s1").size());
        assertEquals("会话一", memory.history("s1").get(0).content());
        assertEquals(1, memory.history("s2").size());
    }

    @Test
    void 未知会话返回空列表() {
        assertTrue(memory.history("不存在").isEmpty());
    }

    @Test
    void 角色正确落位() {
        memory.addUser("s", "问");
        memory.addAssistant("s", "答");
        List<SessionMemory.Msg> h = memory.history("s");
        assertEquals("user", h.get(0).role());
        assertEquals("assistant", h.get(1).role());
    }
}
