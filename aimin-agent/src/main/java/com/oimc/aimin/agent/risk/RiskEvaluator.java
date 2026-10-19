package com.oimc.aimin.agent.risk;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 医疗风险规则：命中即两阶段中断，等待用户确认后才生成处方/剂量级内容。
 * 硬编码安全规则，不可由提示词绕过。
 *
 * <p>v2 相比 v1：同义词/正则覆盖"怎么服用、用法用量、开点药、频次、几片"等漏判说法，
 * 分类返回命中原因，并补一个输出侧的剂量兜底检测。</p>
 */
@Component
public class RiskEvaluator {

    /** 输入侧规则：类别 + 正则（中文不分词，直接用正则覆盖常见表述） */
    private static final List<Rule> RULES = List.of(
            new Rule("剂量与用法", Pattern.compile(
                    "剂量|用量|用法|服法|给药|怎么(吃|服用|用|喝)|如何(吃|服用|用)|"
                            + "吃多少|喝多少|几片|几粒|一天(几|多少)次|每(日|天)(几|多少)次|频次|间隔多久")),
            new Rule("处方与给药操作", Pattern.compile(
                    "处方|开点药|开药|配药|静滴|输液|注射|打针|手术|插管|麻醉|透析")),
            new Rule("剂量调整与过量", Pattern.compile(
                    "停药|加量|减量|过量|超量|中毒|overdose")),
            new Rule("联合用药", Pattern.compile(
                    "配伍|联合用药|一起吃|同服|相互作用|相冲")),
            new Rule("特殊人群与肝肾功能", Pattern.compile(
                    "孕|妊娠|哺乳|备孕|儿童|小儿|婴幼儿|婴儿|新生儿|老人|老年|"
                            + "肝功能|肾功能|肝损伤|肾损伤|肝不全|肾不全"))
    );

    private static final Pattern OUTPUT_DOSE = Pattern.compile(
            "\\d+(\\.\\d+)?\\s*(mg|g|ml|μg|ug|iu|片|粒|袋|支|滴|单位)",
            Pattern.CASE_INSENSITIVE);

    private record Rule(String category, Pattern pattern) {
    }

    /** @return 命中的风险类别；未命中返回 empty */
    public Optional<String> hit(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }
        String normalized = message.toLowerCase();
        return RULES.stream()
                .filter(rule -> rule.pattern().matcher(normalized).find())
                .map(Rule::category)
                .findFirst();
    }

    /** 输出侧兜底：模型回答里出现具体剂量数值时给额外提示类别 */
    public Optional<String> outputRisk(String answer) {
        if (answer == null || answer.isBlank()) {
            return Optional.empty();
        }
        return OUTPUT_DOSE.matcher(answer).find() ? Optional.of("具体剂量数值") : Optional.empty();
    }
}
