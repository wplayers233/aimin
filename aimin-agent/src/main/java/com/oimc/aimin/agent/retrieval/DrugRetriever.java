package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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
 */
@Component
public class DrugRetriever {

    private static final Logger log = LoggerFactory.getLogger(DrugRetriever.class);

    private final JdbcTemplate drugJdbcTemplate;
    private final AtomicReference<List<DrugRow>> cache = new AtomicReference<>();

    public DrugRetriever(@Qualifier("drugJdbcTemplate") JdbcTemplate drugJdbcTemplate) {
        this.drugJdbcTemplate = drugJdbcTemplate;
    }

    public List<Evidence> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        List<DrugRow> drugs = load();
        if (drugs.isEmpty()) {
            return List.of();
        }
        List<String> queryTokens = ChineseTokenizer.tokenize(query);
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
            if (score > 0) {
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
                            for (String token : ChineseTokenizer.tokenize(text.toString())) {
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

    private record DrugRow(String name, String genericName, String form, String strength,
                           String manufacturer, String description, String snippet,
                           Map<String, Integer> tokens) {
    }

    private record ScoredDrug(DrugRow drug, double score) {
    }
}
