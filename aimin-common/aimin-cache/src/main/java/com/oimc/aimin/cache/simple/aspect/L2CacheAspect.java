package com.oimc.aimin.cache.simple.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.oimc.aimin.cache.simple.annotation.L2Cache;
import com.oimc.aimin.cache.simple.config.CacheType;
import com.oimc.aimin.cache.simple.utils.ElParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存切面实现类
 * 实现了对 {@link L2Cache} 注解的AOP处理
 * 提供Caffeine（一级缓存）和Redis（二级缓存）的缓存逻辑
 */
@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class L2CacheAspect {

    /**
     * 缓存键分隔符
     */
    public static final String COLON = "::";

    /**
     * Caffeine缓存实例，作为一级缓存（本地内存缓存）
     */
    private final Cache<String, Object> cache;
    
    /**
     * Redis操作模板，作为二级缓存（分布式缓存）
     */
    private final RedisTemplate<String,Object> redisTemplate;

    /**
     * 定义切点，匹配所有带有L2Cache注解的方法
     */
    @Pointcut("@annotation(com.oimc.aimin.cache.simple.annotation.L2Cache)")
    public void cacheAspect() {
    }

    /**
     * 环绕通知，实现缓存逻辑
     * 
     * @param point 连接点
     * @return 缓存数据或方法执行结果
     * @throws Throwable 执行方法可能抛出的异常
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 构建SpEL表达式解析需要的参数映射
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            treeMap.put(paramNames[i], args[i]);
        }

        // 获取注解信息并解析缓存键
        L2Cache annotation = method.getAnnotation(L2Cache.class);
        String elResult = ElParser.parse(annotation.key(), treeMap);
        String realKey = annotation.cacheName() + COLON + elResult;

        // 根据缓存类型执行不同操作
        // 强制更新缓存
        if (annotation.type() == CacheType.PUT) {
            Object object = point.proceed();
            redisTemplate.opsForValue().set(realKey, object, annotation.l2TimeOut(), TimeUnit.SECONDS);
            cache.put(realKey, object);
            return object;
        }
        // 删除缓存
        else if (annotation.type() == CacheType.DELETE) {
            redisTemplate.delete(realKey);
            cache.invalidate(realKey);
            return point.proceed();
        }

        // 标准缓存查询流程（FULL类型）
        // 1. 先查询Caffeine本地缓存
        Object caffeineCache = cache.getIfPresent(realKey);
        if (Objects.nonNull(caffeineCache)) {
            log.info("get data from caffeine");
            return caffeineCache;
        }

        // 2. 如果本地缓存未命中，查询Redis分布式缓存
        Object redisCache = redisTemplate.opsForValue().get(realKey);
        if (Objects.nonNull(redisCache)) {
            log.info("get data from redis");
            // 将Redis中的数据写入Caffeine，加速后续查询
            cache.put(realKey, redisCache);
            return redisCache;
        }

        // 3. 如果Redis也未命中，则查询数据库
        log.info("get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            // 将数据库结果写入Redis
            redisTemplate.opsForValue().set(realKey, object, annotation.l2TimeOut(), TimeUnit.SECONDS);
            // 将数据库结果写入Caffeine
            cache.put(realKey, object);
        }
        return object;
    }
}
