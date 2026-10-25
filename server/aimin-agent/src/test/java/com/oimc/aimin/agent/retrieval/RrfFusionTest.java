package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RrfFusionTest {

    private static Evidence ev(String type, String title, String snippet) {
        return new Evidence("raw", type, title, 1, snippet, 0.9);
    }

    @Test
    void 两路各自排序_按RRF得分统一重排() {
        // fuse 的去重 key 含 type：知识路与药品路同文本不合并（生产上两路本就传不同来源）
        String shared = "两路都会召回的同一段文本";
        List<List<Evidence>> lists = List.of(
                List.of(ev("vector", "a.md", shared), ev("vector", "a.md", "仅向量命中")),
                List.of(ev("drug", "药品X", shared), ev("drug", "药品X", "仅药品命中")));

        List<Evidence> fused = RrfFusion.fuse(lists, 5);

        assertEquals(4, fused.size());
        // 两路各自的 rank1 得分相同（各 1/(K+1)），稳定排序保持先入先出
        assertEquals(1.0 / 61, fused.get(0).score(), 1e-9);
        assertEquals(1.0 / 61, fused.get(1).score(), 1e-9);
        assertEquals(1.0 / 62, fused.get(2).score(), 1e-9);
        assertEquals("[1]", fused.get(0).refId());
        assertEquals("[4]", fused.get(3).refId());
    }

    @Test
    void topK_截断() {
        List<Evidence> list = List.of(
                ev("vector", "a.md", "文本1"), ev("vector", "a.md", "文本2"),
                ev("vector", "a.md", "文本3"), ev("vector", "a.md", "文本4"));

        List<Evidence> fused = RrfFusion.fuse(List.of(list), 2);

        assertEquals(2, fused.size());
        assertEquals("[1]", fused.get(0).refId());
        assertEquals("文本1", fused.get(0).snippet());
        assertEquals("[2]", fused.get(1).refId());
    }

    @Test
    void 同片段不同来源_按type分开不去重() {
        // HybridRetriever 在融合前已按内容去重；RRF 的 key 含 type，此处锁定该语义
        List<Evidence> list = List.of(ev("vector", "a.md", "同一段"), ev("drug", "药品X", "同一段"));
        List<Evidence> fused = RrfFusion.fuse(List.of(list), 5);
        assertEquals(2, fused.size());
    }

    @Test
    void 空输入返回空() {
        assertEquals(List.of(), RrfFusion.fuse(List.of(), 5));
        assertEquals(List.of(), RrfFusion.fuse(List.of(List.of()), 5));
    }
}
