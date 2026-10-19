package com.oimc.aimin.cache.formal.util;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;


/*
 *@title KeyGen
 *@description
 *@author WIN10
 */
public class KeyGen implements KeyGenerator {

    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            boolean.class, Boolean.class,
            String.class, BigDecimal.class
    );

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String methodName = method.getName();
        StringBuilder paramsBuilder = new StringBuilder();
        for (Object param : params) {
            if (param == null) {
                paramsBuilder.append("null");
                continue;
            }

            if (SIMPLE_TYPES.contains(param.getClass())) {
                // 简单类型直接调用 toString
                paramsBuilder.append(param);
            } else {
                // 复杂对象使用 JSON 序列化
                paramsBuilder.append(JSONUtil.toJsonStr(param));
            }
        }

        String md5Hex16 = DigestUtil.md5Hex16(paramsBuilder.toString());
        return STR."\{methodName}:\{md5Hex16}";
    }

}
