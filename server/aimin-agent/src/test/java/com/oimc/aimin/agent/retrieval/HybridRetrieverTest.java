package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用桩替换向量/BM25 检索器，只测 HybridRetriever 自己的门控与融合过滤语义。
 * 阈值取生产值（0.88 / 6.5，由评测集标定）。
 */
class HybridRetrieverTest {

    private static final double MIN_VECTOR = 0.88;
    private static final double MIN_BM25 = 6.5;

    /** 桩：固定返回预置向量结果（retrieve 不触 embedding/store） */
    static class StubVectorRetriever extends VectorRetriever {
        private final List<Evidence> out;

        StubVectorRetriever(List<Evidence> out) {
            super(null, null);
            this.out = out;
        }

        @Override
        public List<Evidence> retrieve(String query, int topK) {
            return out;
        }
    }

    /** 桩：固定返回预置 BM25 结果 */
    static class StubBm25Index extends Bm25Index {
        private final List<ScoredChunk> out;

        StubBm25Index(List<ScoredChunk> out) {
            this.out = out;
        }

        @Override
        public synchronized List<ScoredChunk> search(String query, int topK) {
            return out;
        }
    }

    private static Evidence vec(String snippet, double score) {
        return new Evidence("v", "vector", "指南.md", 1, snippet, score);
    }

    private static Bm25Index.ScoredChunk bm25(String snippet, double score) {
        return new Bm25Index.ScoredChunk(new KnowledgeChunk("c1", "指南.md", 0, "标题", snippet), score);
    }

    @Test
    void 两路都低于门控_返回空触发拒答() {
        HybridRetriever retriever = new HybridRetriever(
                new StubVectorRetriever(List.of(vec("弱相关", 0.5))),
                new StubBm25Index(List.of(bm25("弱相关", 3.0))),
                MIN_VECTOR, MIN_BM25);

        assertTrue(retriever.retrieve("某个问题", 5).isEmpty());
    }

    @Test
    void 向量单路达标_放行并融合() {
        HybridRetriever retriever = new HybridRetriever(
                new StubVectorRetriever(List.of(
                        vec("证据一", 0.95), vec("证据二", 0.93), vec("证据三", 0.91))),
                new StubBm25Index(List.of()),
                MIN_VECTOR, MIN_BM25);

        List<Evidence> result = retriever.retrieve("某个问题", 5);

        assertEquals(3, result.size());
        assertEquals("vector", result.get(0).type());
    }

    @Test
    void BM25单路达标_也放行() {
        HybridRetriever retriever = new HybridRetriever(
                new StubVectorRetriever(List.of()),
                new StubBm25Index(List.of(bm25("字面精确命中", 10.0), bm25("次要命中", 7.0))),
                MIN_VECTOR, MIN_BM25);

        List<Evidence> result = retriever.retrieve("头孢克洛", 5);

        assertEquals(2, result.size());
        assertEquals("bm25", result.get(0).type());
    }

    @Test
    void 融合后低于最高分六成的条目被丢弃() {
        // A 两路共同命中得分 2/61；B~F 单路 1/62..1/66，全部低于 0.6×top → 只剩 A
        HybridRetriever retriever = new HybridRetriever(
                new StubVectorRetriever(List.of(
                        vec("共同条目A", 0.95), vec("单路B", 0.94), vec("单路C", 0.93),
                        vec("单路D", 0.92), vec("单路E", 0.91), vec("单路F", 0.90))),
                new StubBm25Index(List.of(bm25("共同条目A", 10.0))),
                MIN_VECTOR, MIN_BM25);

        List<Evidence> result = retriever.retrieve("某个问题", 5);

        assertEquals(1, result.size());
        assertEquals("共同条目A", result.get(0).snippet());
        assertEquals("hybrid", result.get(0).type());
    }

    @Test
    void 空查询返回空() {
        HybridRetriever retriever = new HybridRetriever(
                new StubVectorRetriever(List.of(vec("任意", 0.95))),
                new StubBm25Index(List.of()),
                MIN_VECTOR, MIN_BM25);

        assertTrue(retriever.retrieve("", 5).isEmpty());
        assertTrue(retriever.retrieve(null, 5).isEmpty());
    }
}
