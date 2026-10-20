package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RRF（Reciprocal Rank Fusion）多路召回融合 */
public final class RrfFusion {

    private static final int K = 60;

    private RrfFusion() {
    }

    /**
     * @param rankedLists 各路召回结果（每路内部已按相关度排序）
     * @param topK        融合后保留条数
     */
    public static List<Evidence> fuse(List<List<Evidence>> rankedLists, int topK) {
        Map<String, Double> scores = new LinkedHashMap<>();
        Map<String, Evidence> best = new LinkedHashMap<>();

        for (List<Evidence> list : rankedLists) {
            for (int rank = 0; rank < list.size(); rank++) {
                Evidence ev = list.get(rank);
                String key = ev.type() + ":" + ev.title() + ":" + hash(ev.snippet());
                double s = 1.0 / (K + rank + 1);
                scores.merge(key, s, Double::sum);
                best.putIfAbsent(key, ev);
            }
        }

        // 重排 refId（融合后统一编号，供前端引用角标使用）
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        List<Evidence> fused = new ArrayList<>();
        int i = 1;
        for (var entry : sorted) {
            if (fused.size() >= topK) break;
            Evidence ev = best.get(entry.getKey());
            fused.add(new Evidence("[" + i++ + "]", ev.type(), ev.title(), ev.page(), ev.snippet(), entry.getValue()));
        }
        return fused;
    }

    private static int hash(String s) {
        return s == null ? 0 : s.hashCode();
    }
}
