package com.oimc.aimin.agent.model;

public record ChatRequest(String sessionId, String message, String confirmToken) {
}
