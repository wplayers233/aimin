package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 药品库结构化检索（aimin 库 t_drug 表） */
@Component
public class DrugRetriever {

    private final JdbcTemplate drugJdbcTemplate;

    public DrugRetriever(@Qualifier("drugJdbcTemplate") JdbcTemplate drugJdbcTemplate) {
        this.drugJdbcTemplate = drugJdbcTemplate;
    }

    public List<Evidence> retrieve(String query, int topK) {
        List<String> terms = extractTerms(query);
        if (terms.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT name, generic_name, dosage_form, strength, manufacturer, description " +
                "FROM t_drug WHERE ");
        sql.append(terms.stream()
                .map(t -> "(name LIKE ? OR generic_name LIKE ? OR description LIKE ?)")
                .collect(Collectors.joining(" OR ")));
        sql.append(" LIMIT ").append(topK);

        Object[] params = terms.stream()
                .flatMap(t -> List.of("%" + t + "%", "%" + t + "%", "%" + t + "%").stream())
                .toArray();

        List<Evidence> evidences = new ArrayList<>();
        int rank = 1;
        try {
            drugJdbcTemplate.query(sql.toString(), params, rs -> {
                String name = rs.getString("name");
                StringBuilder sb = new StringBuilder();
                String generic = rs.getString("generic_name");
                if (generic != null && !generic.isBlank()) sb.append("通用名: ").append(generic).append("；");
                String form = rs.getString("dosage_form");
                if (form != null && !form.isBlank()) sb.append("剂型: ").append(form).append("；");
                String strength = rs.getString("strength");
                if (strength != null && !strength.isBlank()) sb.append("规格: ").append(strength).append("；");
                String desc = rs.getString("description");
                if (desc != null && !desc.isBlank()) sb.append(desc);
                evidences.add(new Evidence(
                        "d" + rank,
                        "drug",
                        name,
                        null,
                        sb.length() > 0 ? sb.toString() : name,
                        0.0
                ));
            });
        } catch (Exception e) {
            // 药品库不可用时不阻塞主流程
        }
        // 重新编号（lambda 内 rank 不可变，按列表序号统一处理）
        List<Evidence> numbered = new ArrayList<>();
        int i = 1;
        for (Evidence ev : evidences) {
            numbered.add(new Evidence("d" + i++, ev.type(), ev.title(), ev.page(), ev.snippet(), ev.score()));
        }
        return numbered;
    }

    /** 从查询中提取检索词：按非中英文数字切分，保留长度 ≥2 的词 */
    private List<String> extractTerms(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(query.split("[^\\u4e00-\\u9fa5A-Za-z0-9]+"))
                .filter(t -> t.length() >= 2)
                .distinct()
                .limit(4)
                .collect(Collectors.toList());
    }
}
