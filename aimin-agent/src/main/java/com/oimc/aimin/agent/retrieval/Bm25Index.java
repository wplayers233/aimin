package com.oimc.aimin.agent.retrieval;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 纯 Java 中文 BM25 索引（bigram 分词，无第三方依赖）。
 * 医学问题里大量精确术语（CURB-65、150 分钟、SGLT2）向量召回容易漏，BM25 负责兜住。
 */
@Component
public class Bm25Index {

    private static final double K1 = 1.2;
    private static final double B = 0.75;

    private final List<KnowledgeChunk> chunks = new ArrayList<>();
    private final List<Map<String, Integer>> termFreqs = new ArrayList<>();
    private final List<Integer> lengths = new ArrayList<>();
    private final Map<String, Integer> docFreq = new HashMap<>();
    private double avgLength;
    private boolean dirty = true;

    public synchronized void add(KnowledgeChunk chunk) {
        if (chunk == null || chunk.text() == null || chunk.text().isBlank()) {
            return;
        }
        chunks.add(chunk);
        dirty = true;
    }

    public synchronized void addAll(List<KnowledgeChunk> newChunks) {
        for (KnowledgeChunk chunk : newChunks) {
            add(chunk);
        }
    }

    public synchronized int size() {
        return chunks.size();
    }

    public synchronized List<ScoredChunk> search(String query, int topK) {
        if (chunks.isEmpty() || query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        ensureIndex();
        List<String> tokens = ChineseTokenizer.tokenize(query);
        if (tokens.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>(tokens);
        int total = chunks.size();
        List<ScoredChunk> scored = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            Map<String, Integer> tf = termFreqs.get(i);
            int docLength = lengths.get(i);
            double score = 0;
            for (String token : unique) {
                Integer freq = tf.get(token);
                if (freq == null || freq == 0) {
                    continue;
                }
                int df = docFreq.getOrDefault(token, 0);
                double idf = Math.log(1 + (total - df + 0.5) / (df + 0.5));
                double norm = freq + K1 * (1 - B + B * docLength / Math.max(1.0, avgLength));
                score += idf * freq * (K1 + 1) / norm;
            }
            if (score <= 0) {
                continue;
            }
            // 整串命中（药品名/疾病名）额外加权，抵消 bigram 稀释
            if (query.length() >= 2 && chunks.get(i).text().contains(query)) {
                score += 2.5;
            }
            scored.add(new ScoredChunk(chunks.get(i), score));
        }
        Collections.sort(scored, (a, b) -> Double.compare(b.score, a.score));
        return scored.size() > topK ? new ArrayList<>(scored.subList(0, topK)) : scored;
    }

    private void ensureIndex() {
        if (!dirty) {
            return;
        }
        termFreqs.clear();
        lengths.clear();
        docFreq.clear();
        int totalLength = 0;
        for (KnowledgeChunk chunk : chunks) {
            Map<String, Integer> tf = new HashMap<>();
            List<String> tokens = ChineseTokenizer.tokenize(chunk.text());
            for (String token : tokens) {
                tf.merge(token, 1, Integer::sum);
            }
            for (String token : tf.keySet()) {
                docFreq.merge(token, 1, Integer::sum);
            }
            termFreqs.add(tf);
            lengths.add(tokens.size());
            totalLength += tokens.size();
        }
        avgLength = chunks.isEmpty() ? 1 : (double) totalLength / chunks.size();
        dirty = false;
    }

    public record ScoredChunk(KnowledgeChunk chunk, double score) {
    }
}
