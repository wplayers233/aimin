package com.oimc.aimin.cache.formal;

import com.oimc.aimin.cache.formal.config.AiminCacheProperties;
import com.oimc.aimin.cache.formal.sync.CacheMessageListener;
import com.oimc.aimin.cache.formal.util.KeyGen;
import com.oimc.aimin.cache.formal.util.L2RedisCache;
import com.oimc.aimin.redis.config.RedisConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Objects;

/**
 * Aimin缓存自动配置类
 * 负责自动装配二级缓存所需的各种组件
 * 包括RedisCache、CacheManager和缓存同步的消息监听器
 */
@Configuration
@AutoConfigureAfter(RedisConfiguration.class)
@EnableConfigurationProperties(AiminCacheProperties.class)
@RequiredArgsConstructor
public class AiminCacheAutoConfiguration {

    /**
     * 缓存配置属性
     * 从配置文件中读取的缓存配置信息
     */
    private final AiminCacheProperties properties;

    /**
     * 创建Redis缓存工具类
     * 用于操作Redis，作为二级缓存存储
     * 
     * @param redisTemplate Redis操作模板
     * @return Redis缓存工具类实例
     */
    @Bean
    @ConditionalOnBean(value = RedisTemplate.class)
    @Order(1)
    public L2RedisCache redisCache(RedisTemplate<String, Object> redisTemplate) {
        L2RedisCache redisCache = new L2RedisCache();
        redisCache.setRedisTemplate(redisTemplate);
        return redisCache;
    }

    /**
     * 创建缓存管理器
     * 管理所有的缓存实例，负责创建和配置缓存
     * 
     * @param l2RedisCache Redis缓存工具类
     * @return Aimin缓存管理器实例
     */
    @Order(2)
    @Bean
    @ConditionalOnClass(L2RedisCache.class)
    public AiminCacheManager cacheManager(L2RedisCache l2RedisCache) {
        return new AiminCacheManager(properties.getConfig(), l2RedisCache);
    }

    /**
     * 创建Redis消息监听容器
     * 用于监听缓存同步消息，实现多节点间的缓存一致性
     * 
     * @param l2RedisCache Redis缓存工具类
     * @param cacheManager 缓存管理器
     * @return Redis消息监听容器
     */
    @Bean
    @ConditionalOnClass(L2RedisCache.class)
    @Order(3)
    public RedisMessageListenerContainer redisMessageListenerContainer(L2RedisCache l2RedisCache,
                                                                       AiminCacheManager cacheManager) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(Objects.requireNonNull(l2RedisCache.getRedisTemplate().getConnectionFactory()));
        CacheMessageListener cacheMessageListener = new CacheMessageListener(l2RedisCache, cacheManager);
        redisMessageListenerContainer.addMessageListener(cacheMessageListener, new ChannelTopic(properties.getConfig().getRedis().getTopic()));
        return redisMessageListenerContainer;
    }

    @Bean(name = {"keyGen", "defaultKeyGen"})
    public KeyGenerator keyGen() {
        return new KeyGen();
    }

}
