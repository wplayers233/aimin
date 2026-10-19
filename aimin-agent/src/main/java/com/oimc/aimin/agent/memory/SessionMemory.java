package com.oimc.aimin.agent.memory;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 会话记忆（进程内；Redis Stack 就绪后迁移） */
@Component
public class SessionMemory {

    public record Msg(String role, String content, long timestamp) {
        public static Msg user(String c)   { return new Msg("user", c, System.currentTimeMillis()); }
        public static Msg assistant(String c) { return new Msg("assistant", c, System.currentTimeMillis()); }
    }

    private static final int MAX_MESSAGES = 40;

    private final Map<String, Deque<Msg>> sessions = new ConcurrentHashMap<>();

    public void addUser(String sessionId, String content) {
        append(sessionId, Msg.user(content));
    }

    public void addAssistant(String sessionId, String content) {
        append(sessionId, Msg.assistant(content));
    }

    public List<Msg> history(String sessionId) {
        Deque<Msg> dq = sessions.get(sessionId);
        return dq == null ? List.of() : new ArrayList<>(dq);
    }

    private void append(String sessionId, Msg msg) {
        sessions.computeIfAbsent(sessionId, k -> new ArrayDeque<>()).addLast(msg);
        Deque<Msg> dq = sessions.get(sessionId);
        while (dq.size() > MAX_MESSAGES) {
            dq.pollFirst();
        }
    }
}
