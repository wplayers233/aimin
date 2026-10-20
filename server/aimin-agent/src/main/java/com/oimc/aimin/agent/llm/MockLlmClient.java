package com.oimc.aimin.agent.llm;

import java.util.function.Consumer;

/**
 * 演示模式客户端：未配置 DEEPSEEK_API_KEY 时由 AgentBeans 装配。
 * 流式输出固定的结构化模板回复（基于检索到的证据条数），保证前端全链路可联调。
 */
public class MockLlmClient implements LlmClient {

    @Override
    public void stream(String systemPrompt, String userPrompt,
                       Consumer<String> onDelta, Consumer<Throwable> onError, Runnable onComplete) {
        String reply = """
                【演示模式】当前服务未配置 LLM API key（DEEPSEEK_API_KEY），以下为基于知识库检索结果的演示回复。

                根据知识库检索，为您整理参考信息如下：

                1. 本次检索命中了上文列出的参考资料，每条结论均可通过引用角标回溯原文。
                2. 完整版回答将由接入的大模型（DeepSeek-Chat）生成，包含候选原因分析、排查建议与用药提示。
                3. 配置方法：设置环境变量 DEEPSEEK_API_KEY 后重启 aimin-agent 服务即可。

                ⚠️ 本回复由演示模式生成，不构成任何诊疗建议。
                """;
        // 模拟打字机流式输出
        new Thread(() -> {
            try {
                for (int i = 0; i < reply.length(); i += 3) {
                    onDelta.accept(reply.substring(i, Math.min(i + 3, reply.length())));
                    Thread.sleep(18);
                }
                onComplete.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                onError.accept(e);
            }
        }, "mock-llm-stream").start();
    }

    @Override
    public boolean isMock() {
        return true;
    }
}
