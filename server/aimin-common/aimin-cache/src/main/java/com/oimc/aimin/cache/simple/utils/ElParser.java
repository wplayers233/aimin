package com.oimc.aimin.cache.simple.utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.TreeMap;

/**
 * Spring EL表达式解析工具类
 * 用于解析L2Cache注解中的key表达式
 */
public class ElParser {

    /**
     * 解析SpEL表达式
     * 
     * @param elString SpEL表达式字符串，例如 "user.id"
     * @param map 包含表达式变量的参数映射
     * @return 解析后的结果字符串
     */
    public static String parse(String elString, TreeMap<String, Object> map) {
        // 将表达式转换为SpEL模板格式
        elString = String.format("#{%s}", elString);
        
        // 创建表达式解析器
        ExpressionParser parser = new SpelExpressionParser();
        
        // 创建表达式上下文环境
        EvaluationContext context = new StandardEvaluationContext();
        
        // 设置变量到上下文中
        map.forEach(context::setVariable);

        /*
         * 以下是另一种设置变量的方式:
         * map.entrySet().forEach(entry->
         *     context.setVariable(entry.getKey(),entry.getValue())
         * );
         */

        // 解析表达式
        Expression expression = parser.parseExpression(elString, new TemplateParserContext());
        
        // 获取表达式的计算结果并转换为字符串
        return expression.getValue(context, String.class);
    }
}
