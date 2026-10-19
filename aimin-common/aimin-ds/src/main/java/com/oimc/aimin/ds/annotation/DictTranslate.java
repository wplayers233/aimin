package com.oimc.aimin.ds.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 注解作用在字段上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface DictTranslate {

    String dictType() default "NONE";  // 字典类型，用于区分不同的字典表

    String valueTo();

}
