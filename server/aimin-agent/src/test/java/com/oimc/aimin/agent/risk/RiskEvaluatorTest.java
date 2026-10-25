package com.oimc.aimin.agent.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 医疗风险规则的纯函数单测：规则是合规底线，改动必须先红后绿。
 */
class RiskEvaluatorTest {

    private final RiskEvaluator evaluator = new RiskEvaluator();

    @Test
    void 命中_剂量与用法() {
        assertEquals("剂量与用法", evaluator.hit("布洛芬怎么服用").orElseThrow());
        assertEquals("剂量与用法", evaluator.hit("一次吃几片").orElseThrow());
        assertEquals("剂量与用法", evaluator.hit("一天吃几次").orElseThrow());
        assertEquals("剂量与用法", evaluator.hit("两顿间隔多久").orElseThrow());
    }

    @Test
    void 命中_处方与给药操作() {
        assertEquals("处方与给药操作", evaluator.hit("帮我开点药").orElseThrow());
        assertEquals("处方与给药操作", evaluator.hit("需要手术吗").orElseThrow());
        assertEquals("处方与给药操作", evaluator.hit("这个病要打针吗").orElseThrow());
    }

    @Test
    void 命中_剂量调整与过量() {
        assertEquals("剂量调整与过量", evaluator.hit("能不能停药").orElseThrow());
        assertEquals("剂量调整与过量", evaluator.hit("吃过量了怎么办").orElseThrow());
        assertEquals("剂量调整与过量", evaluator.hit("想换药").orElseThrow());
    }

    @Test
    void 命中_联合用药() {
        assertEquals("联合用药", evaluator.hit("这两个药能一起吃吗").orElseThrow());
        assertEquals("联合用药", evaluator.hit("和头孢有相互作用吗").orElseThrow());
    }

    @Test
    void 命中_特殊人群与肝肾功能() {
        assertEquals("特殊人群与肝肾功能", evaluator.hit("孕妇能用吗").orElseThrow());
        assertEquals("特殊人群与肝肾功能", evaluator.hit("儿童可以用吗").orElseThrow());
        assertEquals("特殊人群与肝肾功能", evaluator.hit("肾功能不全怎么选药").orElseThrow());
    }

    @Test
    void 不命中_常规咨询() {
        assertTrue(evaluator.hit("布洛芬的适应症是什么").isEmpty());
        assertTrue(evaluator.hit("高血压有哪些分级").isEmpty());
        assertTrue(evaluator.hit("感冒了一般几天能好").isEmpty());
    }

    @Test
    void 空输入不命中() {
        assertTrue(evaluator.hit(null).isEmpty());
        assertTrue(evaluator.hit("  ").isEmpty());
    }

    @Test
    void 输出侧_具体剂量数值命中() {
        assertTrue(evaluator.outputRisk("建议每次 0.3g").isPresent());
        assertTrue(evaluator.outputRisk("每日 500mg 分两次").isPresent());
        assertTrue(evaluator.outputRisk("每次 2 片").isPresent());
        assertTrue(evaluator.outputRisk("加 10 ml 温水").isPresent());
        assertTrue(evaluator.outputRisk("剂量 25μg").isPresent());
        assertTrue(evaluator.outputRisk("20 IU 起始").isPresent());
    }

    @Test
    void 输出侧_无剂量数值不命中() {
        assertTrue(evaluator.outputRisk("每天三次，饭后服用").isEmpty());
        assertTrue(evaluator.outputRisk("请遵医嘱使用").isEmpty());
        assertTrue(evaluator.outputRisk("mg 与 ml 是不同单位").isEmpty());
        assertTrue(evaluator.outputRisk(null).isEmpty());
    }
}
