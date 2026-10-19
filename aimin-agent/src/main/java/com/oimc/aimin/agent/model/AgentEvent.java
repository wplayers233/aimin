package com.oimc.aimin.agent.model;

import java.util.Map;

/** SSE 事件：type = start | evidence | delta | risk | done | error */
public record AgentEvent(String type, Map<String, Object> data) {
    public static AgentEvent of(String type, Map<String, Object> data) {
        return new AgentEvent(type, data);
    }
}
