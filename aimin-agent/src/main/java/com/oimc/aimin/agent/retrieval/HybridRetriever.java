package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库混合检索：向量召回（语义）+ BM25（精确术语）→ RRF 融合。
 * 两路都弱时返回空列表，由上层给出"知识库未覆盖"的固定拒答。
 */
@Component
public class HybridRetriever {

    private static final int RRF_K = 60;

    /** 融合后低于最高分 60% 的条目按噪声丢弃 */
    private static final double RELATIVE_MIN = 0.6;

    private final VectorRetriever vectorRetriever;
    private final Bm25Index bm25Index;
    private final double minVectorScore;
    private final double minBm25Score;

    public HybridRetriever(VectorRetriever vectorRetriever,
                           Bm25Index bm25Index,
                           @Value("${aimin.agent.retrieval.min-vector-score:0.6}") double minVectorScore,
                           @Value("${aimin.agent.retrieval.min-bm25-score:4.0}") double minBm25Score) {
        this.vectorRetriever = vectorRetriever;
        this.bm25Index = bm25Index;
        this.minVectorScore = minVectorScore;
        this.minBm25Score = minBm25Score;
    }

    public List<Evidence> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        int recallSize = Math.max(topK * 3, 15);
        List<Evidence> vectorHits = vectorRetriever.retrieve(query, recallSize);
        List<Bm25Index.ScoredChunk> bm25Hits = bm25Index.search(query, recallSize);

        double vectorTop = vectorHits.isEmpty() ? 0 : vectorHits.get(0).score();
        double bm25Top = bm25Hits.isEmpty() ? 0 : bm25Hits.get(0).score();
        if (vectorTop < minVectorScore && bm25Top < minBm25Score) {
            return List.of();
        }

        Map<String, Fused> fused = new LinkedHashMap<>();
        for (int i = 0; i < vectorHits.size(); i++) {
            Evidence hit = vectorHits.get(i);
            Fused item = fused.computeIfAbsent(key(hit.snippet()), k -> new Fused(hit.title(), hit.page(), hit.snippet()));
            item.score += 1.0 / (RRF_K + i + 1);
            item.sources.add("vector");
        }
        for (int i = 0; i < bm25Hits.size(); i++) {
            KnowledgeChunk chunk = bm25Hits.get(i).chunk();
            Fused item = fused.computeIfAbsent(key(chunk.text()), k -> new Fused(chunk.fileName(), null, chunk.text()));
            item.score += 1.0 / (RRF_K + i + 1);
            item.sources.add("bm25");
        }

        List<Fused> ordered = new ArrayList<>(fused.values());
        ordered.sort((a, b) -> Double.compare(b.score, a.score));
        if (ordered.isEmpty()) {
            return List.of();
        }
        double top = ordered.get(0).score;

        List<Evidence> result = new ArrayList<>();
        int rank = 1;
        for (Fused item : ordered) {
            if (result.size() >= topK) {
                break;
            }
            if (item.score < top * RELATIVE_MIN) {
                continue;
            }
            String type = item.sources.size() > 1 ? "hybrid" : item.sources.get(0);
            result.add(new Evidence("v" + rank++, type, item.title, item.page, item.snippet, item.score));
        }
        return result;
    }

    private String key(String snippet) {
        if (snippet == null) {
            return "";
        }
        return snippet.length() > 120 ? snippet.substring(0, 120) : snippet;
    }

    private static final class Fused {
        private final String title;
        private final Integer page;
        private final String snippet;
        private final List<String> sources = new ArrayList<>();
        private double score;

        private Fused(String title, Integer page, String snippet) {
            this.title = title;
            this.page = page;
            this.snippet = snippet;
        }
    }
}
