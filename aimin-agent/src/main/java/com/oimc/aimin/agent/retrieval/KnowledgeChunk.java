package com.oimc.aimin.agent.retrieval;

/** BM25 索引中的最小知识单元（与向量库切片一一对应） */
public record KnowledgeChunk(
        String id,
        String fileName,
        int chunkIndex,
        String heading,
        String text
) {
}
