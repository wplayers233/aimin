package com.oimc.aimin.cache.simple.annotation;

import com.oimc.aimin.cache.simple.config.CacheType;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.annotation.*;

/**
 * 二级缓存注解，用于标记需要进行缓存的方法
 * 通过AOP实现，支持Caffeine本地缓存和Redis分布式缓存的二级缓存结构
 * 查询顺序：Caffeine -> Redis -> 数据库
 * 更新策略：数据库 -> Redis -> Caffeine
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAspectJAutoProxy
public @interface L2Cache {
    
    /**
     * 缓存名称，用于标识缓存的业务类型
     * 默认值为"aimin-cache"
     */
    String cacheName() default "aimin-cache";
    
    /**
     * 缓存key，支持SpEL表达式
     * 用于确定缓存的唯一标识，通常根据方法参数构建
     * 示例：#user.id 表示使用user参数的id属性作为key
     */
    String key();
    
    /**
     * Redis缓存过期时间，单位秒
     * 默认为120秒
     */
    long l2TimeOut() default 120;
    
    /**
     * 缓存操作类型
     * FULL: 查询并缓存数据（默认）
     * PUT: 仅更新缓存，不查询已有缓存
     * DELETE: 删除指定的缓存内容
     */
    CacheType type() default CacheType.FULL;
}
