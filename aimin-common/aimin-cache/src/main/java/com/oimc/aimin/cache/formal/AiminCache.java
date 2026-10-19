package com.oimc.aimin.cache.formal;

import com.github.benmanes.caffeine.cache.Cache;
import com.oimc.aimin.cache.formal.config.AiminCacheConfig;
import com.oimc.aimin.cache.formal.sync.CacheMessage;
import com.oimc.aimin.cache.formal.util.L2RedisCache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 二级缓存实现类，继承自Spring的AbstractValueAdaptingCache
 * 结合了Caffeine(一级缓存)和Redis(二级缓存)的优势
 * 提供了本地缓存和分布式缓存的协同工作机制
 */
@Slf4j
@Getter
@Setter
public class AiminCache extends AbstractValueAdaptingCache {

    /**
     * 缓存名称，用于识别不同的缓存实例
     */
    private String cacheName;
    
    /**
     * Redis缓存工具类，作为二级缓存实现
     */
    private L2RedisCache l2RedisCache;
    
    /**
     * Caffeine缓存实例，作为一级缓存实现
     */
    private Cache<Object, Object> l1CaffeineCache;
    
    /**
     * Redis缓存配置信息
     */
    private AiminCacheConfig.Redis redisConfig;
    
    /**
     * 组合缓存配置信息
     */
    private AiminCacheConfig.Composite composite;
    
    /**
     * 默认缓存过期时间(毫秒)，0表示永不过期
     */
    private long defaultExpiration = 0;
    
    /**
     * 不同缓存名称的过期时间配置
     */
    private Map<String, Long> expires;
    
    /**
     * 缓存同步的消息主题
     */
    private String topic = "l2cache:topic";

    /**
     * 是否启用一级缓存的状态标志
     */
    private AtomicBoolean enableL1Cache = new AtomicBoolean();

    /**
     * 缓存键的锁映射，用于防止缓存击穿
     */
    private Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<String, ReentrantLock>();

    private final ReentrantLock[] lockArray = new ReentrantLock[8]; // 配置合适大小
    
    // 初始化
    {
        for (int i = 0; i < lockArray.length; i++) {
            lockArray[i] = new ReentrantLock();
        }
    }
    
    // 获取锁
    private ReentrantLock getLock(Object key) {
        int index = Math.abs(key.hashCode() % lockArray.length);
        return lockArray[index];
    }

    /**
     * 构造函数，初始化缓存实例
     * 
     * @param cacheName 缓存名称
     * @param level2Cache Redis缓存实例
     * @param level1Cache Caffeine缓存实例
     * @param aiminCacheConfig 缓存配置信息
     */
    public AiminCache(String cacheName, L2RedisCache level2Cache,
                      Cache<Object, Object> level1Cache, AiminCacheConfig aiminCacheConfig) {
        super(aiminCacheConfig.isAllowNullValues());
        this.cacheName = cacheName;
        this.l2RedisCache = level2Cache;
        this.l1CaffeineCache = level1Cache;
        this.defaultExpiration = aiminCacheConfig.getRedis().getDefaultExpiration();
        this.expires = aiminCacheConfig.getRedis().getExpires();
        this.topic = aiminCacheConfig.getRedis().getTopic();
        this.composite = aiminCacheConfig.getComposite();
        this.redisConfig = aiminCacheConfig.getRedis();
    }

    /**
     * 查找缓存项
     * 先从一级缓存(Caffeine)查找，如果未命中再从二级缓存(Redis)查找
     * 如果二级缓存命中，则将数据回填到一级缓存
     * 
     * @param key 缓存键
     * @return 缓存值，未找到返回null
     */
    @Override
    protected Object lookup(Object key) {
        Object value = null;
        String cacheKey = getKey(key);
        boolean l1CacheEnabled = l1CacheEnabled(key);

        // 如果启用了一级缓存，先从Caffeine查找
        if(l1CacheEnabled){
            value = l1CaffeineCache.getIfPresent(key);
            if (value != null) {
                log.info("L1-CaffeineCache获取数据");
                return value;
            }
        }
        // 从Redis中查找
        value = l2RedisCache.get(cacheKey);
        
        // 如果Redis中存在且一级缓存已启用，则回填到Caffeine
        if (value != null && l1CacheEnabled) {
            l1CaffeineCache.put(key, toStoreValue(value));
            log.info("L2-RedisCache获取数据");
        }
        return value;
    }

    /**
     * 获取缓存名称
     * 
     * @return 缓存名称
     */
    @Override
    public String getName() {
        return this.cacheName;
    }



    /**
     * 获取底层缓存实现
     * 
     * @return 当前缓存实例
     */
    @Override
    public Object getNativeCache() {
        return this;
    }

    /**
     * 获取缓存值，如果不存在则通过valueLoader加载
     * 实现了缓存加载的同步控制，防止缓存击穿
     * 
     * @param key 缓存键
     * @param valueLoader 值加载器
     * @return 缓存值
     * @throws ValueRetrievalException 值获取异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) value;
        }
        // 获取锁或创建锁，确保同一时间只有一个线程加载缓存
        ReentrantLock lock = getLock(key);
        try {
            lock.lock();
            value = lookup(key);
            if (value != null) {
                return (T) value;
            }
            //代表走被拦截的方法逻辑,并返回方法的返回结果
            value = valueLoader.call();
            Object storeValue = toStoreValue(value);
            put(key, storeValue);
            return (T) value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e.getCause());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 异步获取缓存值
     * 
     * @param key 缓存键
     * @return 异步结果
     */
    @Override
    public CompletableFuture<?> retrieve(Object key) {
        return super.retrieve(key);
    }

    /**
     * 异步获取缓存值，支持自定义值加载器
     * 
     * @param key 缓存键
     * @param valueLoader 值加载器
     * @return 异步结果
     */
    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
        return super.retrieve(key, valueLoader);
    }

    /**
     * 存入缓存
     * 先存入Redis，再根据配置决定是否存入Caffeine
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    @Override
    public void put(Object key, Object value) {
        //如果value不能放空，但实际value为空，那么把数据情清掉就好。
        if (!super.isAllowNullValues() && value == null) {
            this.evict(key);
            return;
        }
        
        // 获取过期时间并存入Redis
        long expire = getExpire();
        if (expire > 0) {
            l2RedisCache.set(getKey(key), toStoreValue(value), expire);
        } else {
            l2RedisCache.set(getKey(key), toStoreValue(value));
        }

        // 是否开启一级缓存
        boolean enabled = checkL1Enabled4Key(getKey(key));
        if (enabled) {
            // 通知其它节点清除本地缓存
            push(new CacheMessage(this.cacheName, key));
            // 存入本地缓存
            l1CaffeineCache.put(key, toStoreValue(value));
        }
    }

    /**
     * 仅当缓存不存在时存入缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @return 原缓存值的包装类，如果不存在返回null
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String cacheKey = getKey(key);
        Object storeValue = toStoreValue(value);
        long expire = getExpire();
        boolean setSuccess;

        if (expire > 0) {
            setSuccess = l2RedisCache.setIfAbsent(cacheKey, storeValue, expire);
        } else {
            setSuccess = l2RedisCache.setIfAbsent(cacheKey, storeValue);
        }

        // 如果设置成功，表示之前没有值
        if (setSuccess) {
            // 设置成功时才更新本地缓存
            boolean l1CacheEnabled = checkL1Enabled4Key(cacheKey);
            if (l1CacheEnabled) {
                push(new CacheMessage(this.cacheName, key));
                l1CaffeineCache.put(key, storeValue);
            }
            return null; // 之前没有值
        } else {
            // 设置未成功，获取当前值
            Object existingValue = l2RedisCache.get(cacheKey);
            return toValueWrapper(existingValue);
        }
    }


    /**
     * 移除缓存
     * 先从Redis删除，再通知其他节点清除本地缓存，最后清除本地缓存
     * 
     * @param key 缓存键
     */
    @Override
    public void evict(Object key) {
        // 先清除redis中缓存数据，然后清除caffeine中的缓存，避免短时间内如果先清除caffeine缓存后其他请求会再从redis里加载到caffeine中
        l2RedisCache.delete(getKey(key));
        push(new CacheMessage(this.cacheName, key));
        l1CaffeineCache.invalidate(key);

    }

    /**
     * 如果缓存项存在则删除
     * 
     * @param key 缓存键
     * @return 是否成功删除
     */
    @Override
    public boolean evictIfPresent(Object key) {
        return super.evictIfPresent(key);
    }

    /**
     * 清空缓存
     * 先清空Redis，再通知其他节点，最后清空本地缓存
     */
    @Override
    public void clear() {
        Set<String> keys = l2RedisCache.keys(this.cacheName.concat(":*"));
        l2RedisCache.deleteKeys(keys);
        push(new CacheMessage(this.cacheName, null));
        l1CaffeineCache.invalidateAll();
    }

    /**
     * 清除本地缓存
     * 
     * @param key 缓存键，如果为null则清除所有
     */
    public void clearLocal(Object key) {
        if (key == null) {
            l1CaffeineCache.invalidateAll();
        } else {
            l1CaffeineCache.invalidate(key);
        }
    }

    /**
     * 生成完整的缓存键
     * 
     * @param key 原始键
     * @return 添加了缓存名称前缀的完整键
     */
    private String getKey(Object key) {
        return this.cacheName.concat(":").concat(key.toString());
    }

    /**
     * 判断是否启用一级缓存
     * 
     * @param key 缓存键
     * @return 是否启用
     */
    private boolean l1CacheEnabled(Object key) {
        return checkL1EnabledGlobally() || checkL1Enabled4Key(key);
    }

    /**
     * 检查全局一级缓存启用状态
     * 
     * @return 是否启用
     */
    private boolean checkL1EnabledGlobally() {
        if (composite.isL1EnabledGlobally() || composite.isL1Enabled4Manually()) {
            enableL1Cache.compareAndSet(false, true);
        }

        if (composite.isL1EnabledGlobally()) {
            return true;
        }
        if (composite.isL1Enabled4Manually()) {
            // 手动匹配缓存名字集合，针对cacheName维度
            Set<String> l1ManualCacheNameSet = composite.getL1NameSet();
            return !CollectionUtils.isEmpty(l1ManualCacheNameSet) && composite.getL1NameSet().contains(this.getName());
        }

        // 检测key
        return false;
    }
    /**
     * 检查是否启用了指定Key开启一级缓存
     * 
     * @param key 缓存键
     * @return 是否启用
     */
    private boolean checkL1Enabled4Key(Object key) {
        // 是否使用手动匹配开关
        if (composite.isL1Enabled4Manually()) {
            // 手动匹配缓存key集合，针对单个key维度
            Set<String> l1ManualKeySet = composite.getL1KeySet();
            return !CollectionUtils.isEmpty(l1ManualKeySet) && l1ManualKeySet.contains(getKey(key));
        }

        return false;
    }

    /**
     * 获取缓存过期时间
     * 
     * @return 过期时间(毫秒)
     */
    private long getExpire() {
        long expire = defaultExpiration;
        Long cacheNameExpire = expires.get(this.cacheName);
        return cacheNameExpire == null ? expire : cacheNameExpire;
    }

    /**
     * 发送缓存同步消息
     * 
     * @param message 缓存消息
     */
    private void push(CacheMessage message) {
        l2RedisCache.getRedisTemplate().convertAndSend(topic, message);
    }
}
