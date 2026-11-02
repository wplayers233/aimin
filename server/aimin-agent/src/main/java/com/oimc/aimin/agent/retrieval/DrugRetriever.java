package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 药品库检索：一次性加载 t_drug 到内存，按中文 bigram 词频 + IDF 打分。
 * 旧实现用整句 LIKE '%问题%'，中文长句几乎永远匹配不到，导致药品召回长期为 0。
 *
 * <p>门控（与知识库侧对齐；缺了它，药品会把无关片段顶进证据集）：
 * ① 停用词过滤——"一次/患者/药物"这类词在 101 条药品里 df 很小、IDF 反而很高，
 * 不滤掉就会让"汽车多久保养一次"命中 3 条胰岛素；<br>
 * ② 最低分 min-score——低于阈值不入榜，作用等同知识库的 min-vector-score / min-bm25-score。<br>
 * 阈值在本语料（101 条药品 / 60 题）上标定为 12：12 道药品题的 gold 最低 13.35，
 * 全部错配样本去停用词后 ≤ 5.84。换药品库或扩库后必须重标。</p>
 */
@Component
public class DrugRetriever {

    private static final Logger log = LoggerFactory.getLogger(DrugRetriever.class);

    /**
     * 药品检索专用停用词。只作用于本类，不影响 {@link ChineseTokenizer} 的其他调用方（如 Bm25Index）。
     * 前 5 个是频次表述，中间 4 个是功能词，末 4 个是 bigram 跨词边界产生的碎片（"肺炎的" → "炎的"）。
     */
    private static final Set<String> STOPWORDS = Set.of(
            "一次", "每日", "每周", "每月", "每次",
            "患者", "病患", "药物", "服用", "用于", "常见",
            "炎的", "病的", "者的");

    private final JdbcTemplate drugJdbcTemplate;
    private final double minScore;
    private final AtomicReference<List<DrugRow>> cache = new AtomicReference<>();

    public DrugRetriever(@Qualifier("drugJdbcTemplate") JdbcTemplate drugJdbcTemplate,
                         @Value("${aimin.agent.drug.min-score:12}") double minScore) {
        this.drugJdbcTemplate = drugJdbcTemplate;
        this.minScore = minScore;
    }

    public List<Evidence> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        return rank(load(), query, topK);
    }

    /**
     * 打分与门控，不触数据库，便于单测。
     *
     * @param drugs 已加载的药品行（生产路径由 {@link #load()} 提供）
     */
    List<Evidence> rank(List<DrugRow> drugs, String query, int topK) {
        if (drugs == null || drugs.isEmpty() || query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        List<String> queryTokens = retrieveTokens(query);
        if (queryTokens.isEmpty()) {
            return List.of();
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        Set<String> unique = new LinkedHashSet<>(queryTokens);
        int total = drugs.size();
        Map<String, Integer> docFreq = new HashMap<>();
        for (DrugRow drug : drugs) {
            for (String token : drug.tokens().keySet()) {
                docFreq.merge(token, 1, Integer::sum);
            }
        }

        List<ScoredDrug> scored = new ArrayList<>();
        for (DrugRow drug : drugs) {
            double score = 0;
            for (String token : unique) {
                Integer freq = drug.tokens().get(token);
                if (freq == null || freq == 0) {
                    continue;
                }
                int df = docFreq.getOrDefault(token, 0);
                double idf = Math.log(1 + (total - df + 0.5) / (df + 0.5));
                score += idf * (1 + Math.log(freq));
            }
            String name = drug.name() == null ? "" : drug.name();
            String generic = drug.genericName() == null ? "" : drug.genericName();
            if (name.length() >= 2 && normalizedQuery.contains(name.toLowerCase(Locale.ROOT))) {
                score += 8;
            }
            if (generic.length() >= 2 && normalizedQuery.contains(generic.toLowerCase(Locale.ROOT))) {
                score += 6;
            }
            if (normalizedQuery.length() >= 2 && name.contains(query)) {
                score += 4;
            }
            // 门控：低于 min-score 的弱命中不入证据集（旧实现是 score > 0，任何碎片词都能挤进榜单）
            if (score >= minScore) {
                scored.add(new ScoredDrug(drug, score));
            }
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<Evidence> result = new ArrayList<>();
        int rank = 1;
        for (ScoredDrug item : scored) {
            if (result.size() >= topK) {
                break;
            }
            result.add(new Evidence("d" + rank++, "drug", item.drug().name(), null, item.drug().snippet(), item.score));
        }
        return result;
    }

    public void refresh() {
        cache.set(null);
    }

    /** 检索用分词：在通用分词基础上滤掉停用词 */
    static List<String> retrieveTokens(String text) {
        List<String> tokens = ChineseTokenizer.tokenize(text);
        if (tokens.isEmpty()) {
            return tokens;
        }
        List<String> kept = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            if (!STOPWORDS.contains(token)) {
                kept.add(token);
            }
        }
        return kept;
    }

    private List<DrugRow> load() {
        List<DrugRow> cached = cache.get();
        if (cached != null) {
            return cached;
        }
        synchronized (cache) {
            cached = cache.get();
            if (cached != null) {
                return cached;
            }
            List<DrugRow> rows = new ArrayList<>();
            try {
                drugJdbcTemplate.query(
                        "SELECT name, generic_name, dosage_form, strength, manufacturer, description " +
                                "FROM t_drug WHERE (status IS NULL OR status = 1) AND (deleted IS NULL OR deleted = 0)",
                        rs -> {
                            String name = rs.getString("name");
                            String generic = rs.getString("generic_name");
                            String form = rs.getString("dosage_form");
                            String strength = rs.getString("strength");
                            String manufacturer = rs.getString("manufacturer");
                            String description = rs.getString("description");
                            StringBuilder text = new StringBuilder();
                            if (name != null) text.append(name).append(' ');
                            if (generic != null) text.append(generic).append(' ');
                            if (form != null) text.append(form).append(' ');
                            if (strength != null) text.append(strength).append(' ');
                            if (description != null) text.append(description);
                            Map<String, Integer> tokens = new HashMap<>();
                            for (String token : retrieveTokens(text.toString())) {
                                tokens.merge(token, 1, Integer::sum);
                            }
                            StringBuilder snippet = new StringBuilder();
                            if (generic != null && !generic.isBlank()) snippet.append("通用名: ").append(generic).append("；");
                            if (form != null && !form.isBlank()) snippet.append("剂型: ").append(form).append("；");
                            if (strength != null && !strength.isBlank()) snippet.append("规格: ").append(strength).append("；");
                            if (manufacturer != null && !manufacturer.isBlank()) snippet.append("厂家: ").append(manufacturer).append("；");
                            if (description != null && !description.isBlank()) {
                                String desc = description.length() > 220 ? description.substring(0, 220) + "…" : description;
                                snippet.append(desc);
                            }
                            rows.add(new DrugRow(name, generic, form, strength, manufacturer, description,
                                    snippet.length() == 0 ? name : snippet.toString(), tokens));
                        });
            } catch (Exception e) {
                log.warn("药品库加载失败：{}", e.getMessage());
            }
            List<DrugRow> immutable = List.copyOf(rows);
            cache.set(immutable);
            log.info("药品库已加载 {} 条（status=1）", immutable.size());
            return immutable;
        }
    }

    /** 包级可见，供单测构造样本行 */
    record DrugRow(String name, String genericName, String form, String strength,
                   String manufacturer, String description, String snippet,
                   Map<String, Integer> tokens) {
    }

    private record ScoredDrug(DrugRow drug, double score) {
    }
}
