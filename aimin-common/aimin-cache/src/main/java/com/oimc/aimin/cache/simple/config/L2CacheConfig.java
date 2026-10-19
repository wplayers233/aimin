package com.oimc.aimin.cache.simple.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * 二级缓存配置类
 * 负责创建和配置Caffeine本地缓存和Spring Cache集成所需的组件
 * 启用了Spring的缓存功能
 */
@EnableCaching
@RequiredArgsConstructor
public class L2CacheConfig {

    /**
     * 缓存配置属性
     * 包含Caffeine缓存的容量、过期时间等配置
     */
    private final SimpleCacheProperties l2CacheProperties;

    /**
     * 创建Caffeine缓存实例
     * 作为一级缓存使用，存储在本地内存中
     * 
     * @return 配置好的Caffeine缓存实例
     */
    @Bean
    public Cache<String, Object> caffeineCacheManager() {
        return Caffeine.newBuilder()
                // 设置缓存初始容量
                .initialCapacity(l2CacheProperties.getInitialCapacity())
                // 设置缓存最大容量
                .maximumSize(1000)
                // 设置缓存过期时间
                .expireAfterWrite(l2CacheProperties.getExpireAfterWrite(),
                        l2CacheProperties.getExpireAfterWriteTimeUnit())
                .build();
    }
}
