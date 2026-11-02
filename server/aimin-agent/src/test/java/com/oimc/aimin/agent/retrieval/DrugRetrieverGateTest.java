package com.oimc.aimin.agent.retrieval;

import com.oimc.aimin.agent.model.Evidence;
import com.oimc.aimin.agent.retrieval.DrugRetriever.DrugRow;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 药品侧门控回归：停用词 + 最低分。
 *
 * <p>背景（实测）：旧实现是 {@code if (score > 0)}，任何碎片词都能进榜。
 * q58「汽车多久保养一次比较合适？」因 {@code 一次} 在 101 条药品里 df=3、IDF 高达 3.372，
 * 命中 3 条胰岛素类药品，导致知识库为空时仍不拒答（线上拒答率实为 7/8）。</p>
 *
 * <p>本测试用固定样本行验证三件事：<br>
 * ① 只靠频次表述命中 → 过滤后为 0；<br>
 * ② 单个弱命中词的分低于 min-score → 被门槛挡掉（对照：门槛放到 1 时它会出现）；<br>
 * ③ 按药品类别提问（不点名）仍能召回 —— 这是不能改成"必须出现药名"的原因，
 * 12 道药品题里 11 道都不点名。</p>
 */
class DrugRetrieverGateTest {

    /** 与生产同构：把各字段拼成检索文本后分词去停用词，统计词频 */
    private static DrugRow row(String name, String generic, String form, String strength, String desc) {
        String text = String.join(" ", name, generic, form, strength, desc);
        Map<String, Integer> tokens = new HashMap<>();
        for (String token : DrugRetriever.retrieveTokens(text)) {
            tokens.merge(token, 1, Integer::sum);
        }
        return new DrugRow(name, generic, form, strength, "", desc, desc, tokens);
    }

    private static List<DrugRow> sampleRows() {
        List<DrugRow> rows = new ArrayList<>();
        rows.add(row("司美格鲁肽注射液", "司美格鲁肽", "注射液", "100单位/ml", "GLP-1受体激动剂，每周一次；常见恶心"));
        rows.add(row("甘精胰岛素注射液", "甘精胰岛素", "注射液", "300单位/支", "长效胰岛素，每日一次；警惕低血糖"));
        rows.add(row("乳果糖口服溶液", "乳果糖", "口服溶液", "100ml", "渗透性泻药，用于便秘，每日一次"));
        rows.add(row("厄贝沙坦片", "厄贝沙坦", "片剂", "150mg", "ARB类降压药，用于高血压合并糖尿病肾病"));
        rows.add(row("阿莫西林胶囊", "阿莫西林", "胶囊", "0.25g", "青霉素类抗生素，用于呼吸道感染"));
        rows.add(row("维生素C片", "维生素C", "片剂", "100mg", "用于预防坏血病"));
        return rows;
    }

    @Test
    void 频次表述不再造成误命中() {
        DrugRetriever retriever = new DrugRetriever(null, 12);
        List<Evidence> hits = retriever.rank(sampleRows(), "汽车多久保养一次比较合适？", 3);
        assertTrue(hits.isEmpty(), "只靠「一次」命中的药品不应进入证据集，实际命中：" + hits.size());
    }

    @Test
    void 停用词在分词阶段就被剔除() {
        assertFalse(DrugRetriever.retrieveTokens("每周一次，每日一次").contains("一次"),
                "「一次」应被停用词过滤");
        assertFalse(DrugRetriever.retrieveTokens("患者常见不良反应").contains("患者"),
                "「患者」应被停用词过滤");
        // 跨词边界的碎片：「肺炎的」→ 炎的
        assertFalse(DrugRetriever.retrieveTokens("社区获得性肺炎的评分").contains("炎的"),
                "bigram 碎片「炎的」应被停用词过滤");
        // 正常术语不能被误伤
        assertTrue(DrugRetriever.retrieveTokens("高血压合并糖尿病肾病").contains("糖尿"),
                "领域词不应被过滤");
    }

    @Test
    void 单个弱命中词被最低分挡掉() {
        List<DrugRow> rows = sampleRows();
        // 「预防」只在维生素C的描述里出现一次，属单 token 弱命中
        List<Evidence> strict = new DrugRetriever(null, 5).rank(rows, "偏头痛预防治疗常用哪些药物？", 3);
        assertTrue(strict.stream().noneMatch(e -> e.title().contains("维生素C")),
                "单 token 弱命中应被 min-score 挡掉");

        // 对照：门槛放到 1，同一行就会进榜 —— 证明是门槛在起作用，而不是它压根没匹配上
        List<Evidence> loose = new DrugRetriever(null, 1).rank(rows, "偏头痛预防治疗常用哪些药物？", 3);
        assertTrue(loose.stream().anyMatch(e -> e.title().contains("维生素C")),
                "门槛放宽后该弱命中应重新出现（否则本用例没有验证力）");
    }

    @Test
    void 按类别提问不点名也能召回() {
        DrugRetriever retriever = new DrugRetriever(null, 5);
        List<Evidence> hits = retriever.rank(sampleRows(), "高血压合并糖尿病肾病适合用哪种降压药？", 3);
        assertFalse(hits.isEmpty(), "按类别提问必须仍能召回药品（12 道药品题里 11 道不点名）");
        assertEquals("厄贝沙坦片", hits.get(0).title(), "ARB 应排在首位");
    }
}
