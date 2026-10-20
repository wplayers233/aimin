package com.oimc.aimin.agent.model;

/** 可追溯证据：诊断回答中的每条结论都应能回溯到此类条目 */
public record Evidence(
        String refId,     // 引用角标 [1]
        String type,      // vector | drug
        String title,     // 文档名 / 药品名
        Integer page,     // 页码（文档类）
        String snippet,   // 摘录片段
        double score      // 融合得分
) {
}
