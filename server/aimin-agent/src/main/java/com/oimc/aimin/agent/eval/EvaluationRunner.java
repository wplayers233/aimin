package com.oimc.aimin.agent.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oimc.aimin.agent.model.Evidence;
import com.oimc.aimin.agent.retrieval.Bm25Index;
import com.oimc.aimin.agent.retrieval.DrugRetriever;
import com.oimc.aimin.agent.retrieval.HybridRetriever;
import com.oimc.aimin.agent.retrieval.VectorRetriever;
import com.oimc.aimin.agent.risk.RiskEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 离线评测器（CLI 模式）：
 * java -jar aimin-agent.jar --aimin.eval.enabled=true
 * 对 fact / drug / risk / unanswerable 四类题分别计算指标，结果打印并写入 data/eval/latest.json。
 */
@Component
@Order(2)
@ConditionalOnProperty(prefix = "aimin.eval", name = "enabled", havingValue = "true")
public class EvaluationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EvaluationRunner.class);

    private final HybridRetriever hybridRetriever;
    private final VectorRetriever vectorRetriever;
    private final Bm25Index bm25Index;
    private final DrugRetriever drugRetriever;
    private final RiskEvaluator riskEvaluator;
    private final ObjectMapper objectMapper;
    private final int topK;
    private final String questionsPath;
    private final String outputPath;
    private final boolean exitAfterRun;

    public EvaluationRunner(HybridRetriever hybridRetriever,
                            VectorRetriever vectorRetriever,
                            Bm25Index bm25Index,
                            DrugRetriever drugRetriever,
                            RiskEvaluator riskEvaluator,
                            ObjectMapper objectMapper,
                            @Value("${aimin.eval.top-k:5}") int topK,
                            @Value("${aimin.eval.questions:./data/eval/questions.json}") String questionsPath,
                            @Value("${aimin.eval.output:./data/eval/latest.json}") String outputPath,
                            @Value("${aimin.eval.exit:true}") boolean exitAfterRun) {
        this.hybridRetriever = hybridRetriever;
        this.vectorRetriever = vectorRetriever;
        this.bm25Index = bm25Index;
        this.drugRetriever = drugRetriever;
        this.riskEvaluator = riskEvaluator;
        this.objectMapper = objectMapper;
        this.topK = topK;
        this.questionsPath = questionsPath;
        this.outputPath = outputPath;
        this.exitAfterRun = exitAfterRun;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path questionFile = Path.of(questionsPath);
        if (!Files.isRegularFile(questionFile)) {
            log.error("评测题文件不存在：{}", questionFile.toAbsolutePath());
            exitIfNeeded(1);
            return;
        }
        EvalSet set = objectMapper.readValue(questionFile.toFile(), EvalSet.class);
        List<EvalQuestion> questions = set.questions == null ? List.of() : set.questions;
        log.info("开始评测：{} 题，topK={}，检索引擎=hybrid(vector+bm25)", questions.size(), topK);

        int factTotal = 0, factHit = 0;
        double mrrSum = 0;
        int drugTotal = 0, drugHit = 0;
        int riskTotal = 0, riskHit = 0;
        int unanswerableTotal = 0, refused = 0;
        List<Map<String, String>> failures = new ArrayList<>();
        List<Map<String, Object>> diagnostics = new ArrayList<>();

        for (EvalQuestion q : questions) {
            String type = q.type == null ? "" : q.type;
            double vectorTop = topVectorScore(q.question);
            double bm25Top = topBm25Score(q.question);
            List<String> hybridTop = List.of();
            try {
                switch (type) {
                    case "fact" -> {
                        factTotal++;
                        List<Evidence> evidences = hybridRetriever.retrieve(q.question, topK);
                        hybridTop = titles(evidences);
                        int rank = firstHitRank(evidences, q.goldSnippets);
                        if (rank > 0) {
                            factHit++;
                            mrrSum += 1.0 / rank;
                        } else {
                            failures.add(failure(q, "未命中，top1=" + topTitle(evidences)));
                        }
                    }
                    case "drug" -> {
                        drugTotal++;
                        List<Evidence> evidences = drugRetriever.retrieve(q.question, topK);
                        boolean hit = q.goldDrug != null && evidences.stream().anyMatch(e ->
                                (e.title() != null && e.title().contains(q.goldDrug))
                                        || (e.snippet() != null && e.snippet().contains(q.goldDrug)));
                        if (hit) {
                            drugHit++;
                        } else {
                            failures.add(failure(q, "未命中，top1=" + topTitle(evidences)));
                        }
                    }
                    case "risk" -> {
                        riskTotal++;
                        String actual = riskEvaluator.hit(q.question).orElse("NONE");
                        if (actual.equals(q.expectRisk)) {
                            riskHit++;
                        } else {
                            failures.add(failure(q, "期望=" + q.expectRisk + "，实际=" + actual));
                        }
                    }
                    case "unanswerable" -> {
                        unanswerableTotal++;
                        List<Evidence> evidences = hybridRetriever.retrieve(q.question, topK);
                        if (evidences.isEmpty()) {                            refused++;
                        } else {
                            failures.add(failure(q, "未拒答，top1=" + topTitle(evidences)));
                        }
                    }
                    default -> log.warn("未知题型：{}（{}）", type, q.id);
                }
            } catch (Exception e) {
                failures.add(failure(q, "执行异常：" + e.getMessage()));
            }
            Map<String, Object> diag = new LinkedHashMap<>();
            diag.put("id", q.id);
            diag.put("type", type);
            diag.put("vectorTop", round(vectorTop));
            diag.put("bm25Top", round(bm25Top));
            if (!hybridTop.isEmpty()) {
                diag.put("hybridTop", hybridTop);
            }
            diagnostics.add(diag);
        }

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("recallAt5", round(factTotal == 0 ? 0 : (double) factHit / factTotal));
        metrics.put("mrr", round(factTotal == 0 ? 0 : mrrSum / factTotal));
        metrics.put("drugAccuracyAt5", round(drugTotal == 0 ? 0 : (double) drugHit / drugTotal));
        metrics.put("riskAccuracy", round(riskTotal == 0 ? 0 : (double) riskHit / riskTotal));
        metrics.put("refusalRate", round(unanswerableTotal == 0 ? 0 : (double) refused / unanswerableTotal));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedAt", Instant.now().toString());
        report.put("engine", "hybrid-vector-bm25");
        report.put("topK", topK);
        report.put("questionCount", questions.size());
        report.put("metrics", metrics);
        report.put("detail", Map.of(
                "fact", factHit + "/" + factTotal,
                "drug", drugHit + "/" + drugTotal,
                "risk", riskHit + "/" + riskTotal,
                "unanswerable", refused + "/" + unanswerableTotal));
        report.put("failures", failures);
        report.put("diagnostics", diagnostics);

        Path out = Path.of(outputPath);
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report),
                StandardCharsets.UTF_8);

        log.info("评测结果：Recall@5={} ({}/{}), MRR={}, 药品 Acc@5={} ({}/{}), 风险准确率={} ({}/{}), 拒答率={} ({}/{})",
                metrics.get("recallAt5"), factHit, factTotal, metrics.get("mrr"),
                metrics.get("drugAccuracyAt5"), drugHit, drugTotal,
                metrics.get("riskAccuracy"), riskHit, riskTotal,
                metrics.get("refusalRate"), refused, unanswerableTotal);
        log.info("失败题目 {} 条，明细见 {}", failures.size(), out.toAbsolutePath());
        for (Map<String, String> failure : failures) {
            log.info("  [{}] {} -> {}", failure.get("type"), failure.get("id"), failure.get("detail"));
        }
        exitIfNeeded(0);
    }

    private double topVectorScore(String question) {
        try {
            List<Evidence> hits = vectorRetriever.retrieve(question, 1);
            return hits.isEmpty() ? 0 : hits.get(0).score();
        } catch (Exception e) {
            return 0;
        }
    }

    private double topBm25Score(String question) {
        try {
            List<Bm25Index.ScoredChunk> hits = bm25Index.search(question, 1);
            return hits.isEmpty() ? 0 : hits.get(0).score();
        } catch (Exception e) {
            return 0;
        }
    }

    private int firstHitRank(List<Evidence> evidences, List<String> snippets) {
        if (snippets == null || snippets.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < evidences.size(); i++) {
            String text = evidences.get(i).snippet();
            if (text == null) {
                continue;
            }
            for (String snippet : snippets) {
                if (snippet != null && text.contains(snippet)) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private List<String> titles(List<Evidence> evidences) {
        List<String> titles = new ArrayList<>();
        for (Evidence evidence : evidences) {
            titles.add(String.valueOf(evidence.title()));
        }
        return titles;
    }

    private String topTitle(List<Evidence> evidences) {
        return evidences.isEmpty() ? "无" : String.valueOf(evidences.get(0).title());
    }

    private Map<String, String> failure(EvalQuestion q, String detail) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", q.id);
        map.put("type", q.type);
        map.put("question", q.question);
        map.put("detail", detail);
        return map;
    }

    private double round(double value) {
        return Math.round(value * 1000) / 1000.0;
    }

    private void exitIfNeeded(int code) {
        if (!exitAfterRun) {
            return;
        }
        System.out.flush();
        System.exit(code);
    }

    public static class EvalSet {
        public int version;
        public List<EvalQuestion> questions;
    }
}
