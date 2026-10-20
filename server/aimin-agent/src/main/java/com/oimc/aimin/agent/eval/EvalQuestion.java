package com.oimc.aimin.agent.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** 离线评测题：fact=知识库检索，drug=药品检索，risk=风险识别，unanswerable=应拒答 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvalQuestion {

    public String id;
    public String type;
    public String question;
    /** fact 题：命中证据中应包含的片段（任一命中即算召回） */
    public List<String> goldSnippets;
    /** drug 题：期望命中的药品名或通用名 */
    public String goldDrug;
    /** risk 题：期望的风险类别（与 RiskEvaluator 返回一致） */
    public String expectRisk;
}
