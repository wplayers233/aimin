package com.oimc.aimin.agent.llm;

import java.util.function.Consumer;

/** LLM 抽象：流式输出。DeepSeek 实现与演示模式(Mock)共用 */
public interface LlmClient {

    /**
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户消息（含检索上下文）
     * @param onDelta      增量文本回调
     * @param onError      异常回调
     * @param onComplete   完成回调
     */
    void stream(String systemPrompt, String userPrompt,
                Consumer<String> onDelta, Consumer<Throwable> onError, Runnable onComplete);

    /** 是否为演示模式（未配置真实 key） */
    boolean isMock();
}
