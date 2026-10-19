package com.oimc.aimin.cache.formal.sync;

import com.oimc.aimin.cache.formal.AiminCacheManager;
import com.oimc.aimin.cache.formal.util.L2RedisCache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * 缓存消息监听器
 * 监听Redis发布的缓存同步消息，用于在集群环境中保持缓存一致性
 * 当其他节点更新缓存时，会收到消息并清除本地对应的缓存
 */
public class CacheMessageListener implements MessageListener {

    /**
     * Redis缓存工具类
     * 用于反序列化接收到的消息
     */
    private final L2RedisCache l2RedisCache;

    /**
     * 缓存管理器
     * 用于清除本地缓存
     */
    private final AiminCacheManager aiminCacheManager;

    /**
     * 构造函数
     * 
     * @param redisService Redis服务，用于消息反序列化
     * @param redisCaffeineCacheManager 缓存管理器，用于清除本地缓存
     */
    public CacheMessageListener(L2RedisCache redisService,
                                AiminCacheManager redisCaffeineCacheManager) {
        super();
        this.l2RedisCache = redisService;
        this.aiminCacheManager = redisCaffeineCacheManager;
    }

    /**
     * 消息处理方法
     * 当收到缓存同步消息时，清除本地对应的缓存项
     * 
     * @param message 收到的消息
     * @param pattern 订阅模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        CacheMessage cacheMessage = (CacheMessage) l2RedisCache.getRedisTemplate().getValueSerializer().deserialize(message.getBody());
        aiminCacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
    }
}
