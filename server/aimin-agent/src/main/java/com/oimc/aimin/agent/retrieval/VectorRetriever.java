package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 知识库向量检索（bge-small-zh + EmbeddingStore） */
@Component
public class VectorRetriever {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    public VectorRetriever(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> store) {
        this.embeddingModel = embeddingModel;
        this.store = store;
    }

    public List<Evidence> retrieve(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> result = store.search(
                new EmbeddingSearchRequest(queryEmbedding, topK, null, null));
        List<Evidence> evidences = new ArrayList<>();
        int rank = 1;
        for (var match : result.matches()) {
            TextSegment seg = match.embedded();
            var meta = seg.metadata();
            evidences.add(new Evidence(
                    "v" + rank++,
                    "vector",
                    meta.getString("title"),
                    meta.getInteger("page"),
                    seg.text(),
                    match.score()
            ));
        }
        return evidences;
    }

    public int docCount() {
        // EmbeddingStore 无统一 size 接口，用搜索近似判断是否为空
        try {
            Embedding e = embeddingModel.embed("测试").content();
            return store.search(new EmbeddingSearchRequest(e, 1, null, null)).matches().size() > 0 ? 1 : 0;
        } catch (Exception ex) {
            return 0;
        }
    }
}
