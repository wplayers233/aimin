package com.oimc.aimin.cache.simple.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * 二级缓存配置属性类
 * 用于从配置文件中读取Caffeine缓存的相关配置
 * 配置前缀为"l2cache"，如：l2cache.maximum-size
 */
@Data
@ConfigurationProperties(prefix = "l2cache")
public class SimpleCacheProperties {

    /**
     * Caffeine缓存初始容量
     * 建议根据预估的数据量设置，避免频繁扩容
     */
    private int initialCapacity;
    
    /**
     * Caffeine缓存最大容量
     * 当缓存数量超过此值时，会根据LRU算法进行淘汰
     */
    private int maximumSize;
    
    /**
     * 写入后过期时间值
     * 缓存项在写入后经过此时间会自动过期
     */
    private int expireAfterWrite;
    
    /**
     * 时间单位字符串表示
     * 如 "seconds", "minutes", "hours" 等
     */
    private String timeUnit;
    
    /**
     * 写入后过期时间单位
     * 由timeUnit字符串转换而来
     */
    private TimeUnit expireAfterWriteTimeUnit;

    /**
     * 初始化方法
     * 在属性设置完成后自动执行，用于处理时间单位的转换
     * 如果timeUnit配置有误，会抛出异常
     */
    @PostConstruct
    public void init() {
        if (timeUnit != null) {
            try {
                // 将字符串时间单位转换为TimeUnit枚举值
                this.expireAfterWriteTimeUnit = TimeUnit.valueOf(timeUnit.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的时间单位值: " + timeUnit);
            }
        } else {
            // 默认使用秒作为时间单位
            this.expireAfterWriteTimeUnit = TimeUnit.SECONDS;
        }
    }
}
