package com.oimc.aimin.ds.translate;


import com.oimc.aimin.ds.annotation.DictTranslate;
import com.oimc.aimin.ds.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;


@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class DictTranslateInterceptor implements Interceptor {

    private final DictService dictService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 执行原始查询，获取结果
        Object result = invocation.proceed();

        // 处理查询结果
        if (result instanceof List) {
            for (Object entity : (List<?>) result) {
                translateEntity(entity); // 替换字段值
            }
        } else {
            translateEntity(result); // 处理单个对象
        }
        return result;
    }

    private void translateEntity(Object entity) throws IllegalAccessException {
        if (entity == null) {
            return;
        }

        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(DictTranslate.class)) {
                // 获取注解信息
                DictTranslate annotation = field.getAnnotation(DictTranslate.class);
                String dictType = annotation.dictType();
                String valueTo = annotation.valueTo();

                // 获取字段值（关联 ID）
                field.setAccessible(true);
                // 查询字典表中的名称
                String name;
                Object o = field.get(entity);
                if(o != null){
                    if("NONE".equals(dictType)){
                        int value = (int) o;
                        name = dictService.getById(value).getDictName();
                    }else{
                        String value = (String) o;
                        name = dictService.getByTypeAndCode(dictType, value).getDictName();
                    }

                    // 将名称设置到对应的字段中
                    Field nameField = getFieldByName(clazz, valueTo);
                    if (nameField != null) {
                        nameField.setAccessible(true);
                        nameField.set(entity, name);
                    }
                }
            }
        }
    }

    private Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
