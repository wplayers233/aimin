package com.oimc.aimin.agent.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.function.Consumer;

/**
 * DeepSeek 客户端（OpenAI 兼容协议）。由 AgentBeans 在配置了 api-key 时装配。
 */
public class DeepSeekLlmClient implements LlmClient {

    private final StreamingChatLanguageModel model;

    public DeepSeekLlmClient(String baseUrl, String apiKey, String modelName, Double temperature) {
        this.model = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Override
    public void stream(String systemPrompt, String userPrompt,
                       Consumer<String> onDelta, Consumer<Throwable> onError, Runnable onComplete) {
        model.generate(
                List.of(SystemMessage.from(systemPrompt), UserMessage.from(userPrompt)),
                new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        onDelta.accept(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        onComplete.run();
                    }

                    @Override
                    public void onError(Throwable error) {
                        onError.accept(error);
                    }
                }
        );
    }

    @Override
    public boolean isMock() {
        return false;
    }
}
