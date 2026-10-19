package com.oimc.aimin.agent.config;

import com.oimc.aimin.agent.llm.DeepSeekLlmClient;
import com.oimc.aimin.agent.llm.LlmClient;
import com.oimc.aimin.agent.llm.MockLlmClient;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AgentBeans {

    /** 本地 ONNX 中文 embedding 模型（bge-small-zh，进程内运行，无外部依赖） */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhEmbeddingModel();
    }

    /** 向量库：memory = InMemoryEmbeddingStore + JSON 文件持久化（Redis Stack 就绪后可切换） */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Value("${aimin.agent.vector-store}") String storeType,
            @Value("${aimin.agent.vector-file}") String vectorFile) throws IOException {
        Path file = Path.of(vectorFile);
        if ("memory".equals(storeType) && Files.exists(file)) {
            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                return InMemoryEmbeddingStore.fromJson(json);
            } catch (Exception e) {
                // 文件损坏则重建
            }
        }
        return new InMemoryEmbeddingStore<>();
    }

    /** LLM：有 key 走 DeepSeek，否则演示模式 */
    @Bean
    @Primary
    public LlmClient llmClient(@Value("${aimin.agent.llm.api-key}") String apiKey,
                               @Value("${aimin.agent.llm.base-url}") String baseUrl,
                               @Value("${aimin.agent.llm.model}") String model,
                               @Value("${aimin.agent.llm.temperature}") Double temperature) {
        if (apiKey != null && !apiKey.isBlank()) {
            return new DeepSeekLlmClient(baseUrl, apiKey, model, temperature);
        }
        return new MockLlmClient();
    }

    /** 药品库独立数据源（aimin 库 t_drug 表） */
    @Bean
    public JdbcTemplate drugJdbcTemplate(
            @Value("${aimin.agent.drug.datasource.url}") String url,
            @Value("${aimin.agent.drug.datasource.username}") String username,
            @Value("${aimin.agent.drug.datasource.password}") String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, username, password);
        return new JdbcTemplate(ds);
    }
}
