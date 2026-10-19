package com.oimc.aimin.cache.formal.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Aimin缓存配置类
 * 定义了二级缓存所需的全部配置参数
 * 包括Caffeine(一级缓存)、Redis(二级缓存)和组合缓存的配置
 */
@Data
@Accessors(chain = true)
public class AiminCacheConfig {

    /**
     * 是否存储空值
     * 设置为true时，可防止缓存穿透，默认允许
     */
    private boolean allowNullValues = true;

    /**
     * 是否动态创建缓存
     * true: 允许根据需要动态创建任意cacheName的缓存实例
     * false: 只允许创建指定的cacheNames中的缓存实例
     */
    private boolean dynamic = true;

    /**
     * 允许创建的缓存名称集合
     * 当dynamic=false时生效，只有此集合中的缓存名称才能被创建
     */
    private Set<String> cacheNames = new HashSet<>();

    /**
     * 组合缓存配置，控制一、二级缓存的协同工作机制
     */
    private final Composite composite = new Composite();
    
    /**
     * Caffeine缓存配置(一级缓存)
     */
    private final Caffeine caffeine = new Caffeine();
    
    /**
     * Redis缓存配置(二级缓存)
     */
    private final Redis redis = new Redis();

    /**
     * 组合缓存配置类
     * 定义了一、二级缓存的协同工作机制
     */
    @Data
    @Accessors(chain = true)
    public static class Composite {

        /**
         * 是否全部启用一级缓存
         * true: 所有缓存键都启用一级缓存
         * false: 根据配置选择性启用一级缓存
         */
        private boolean L1EnabledGlobally = false;

        /**
         * 是否手动启用一级缓存
         * true: 根据手动配置的缓存名称或键启用一级缓存
         * false: 禁用一级缓存
         */
        private boolean l1Enabled4Manually = false;

        /**
         * 手动配置走一级缓存的缓存键集合
         * 针对单个缓存键维度，精确控制哪些键启用一级缓存
         */
        private Set<String> l1KeySet = new HashSet<>();

        /**
         * 手动配置走一级缓存的缓存名称集合
         * 针对缓存名称维度，控制整个缓存实例是否启用一级缓存
         */
        private Set<String> l1NameSet = new HashSet<>();
    }

    /**
     * Caffeine缓存配置类
     * 定义了一级缓存(本地内存缓存)的参数
     */
    @Data
    @Accessors(chain = true)
    public static class Caffeine {
        /**
         * 是否自动刷新过期缓存
         * true: 缓存过期时自动刷新
         * false: 缓存过期后不自动刷新
         */
        private boolean autoRefreshExpireCache = false;

        /**
         * 缓存刷新调度线程池的大小
         * 默认为CPU核心数的两倍
         */
        private Integer refreshPoolSize = Runtime.getRuntime().availableProcessors();

        /**
         * 缓存刷新的频率，单位秒
         * 默认每30秒刷新一次
         */
        private Long refreshPeriod = 30L;

        /**
         * 同一个键的发布消息频率，单位毫秒
         * 用于控制缓存同步消息的发送频率，防止消息风暴
         */
        private Long publishMsgPeriodMilliSeconds = 500L;

        /**
         * 访问后过期时间，单位秒
         * 缓存项在最后一次访问后多久过期
         */
        private long expireAfterAccess;

        /**
         * 写入后过期时间，单位秒
         * 缓存项在最后一次写入后多久过期
         */
        private long expireAfterWrite;

        /**
         * 写入后刷新时间，单位秒
         * 缓存项在写入后多久需要刷新
         */
        private long refreshAfterWrite;

        /**
         * 初始化大小
         * 缓存的初始容量
         */
        private int initialCapacity;

        /**
         * 最大缓存对象个数
         * 当缓存数量超过此值时，会根据策略淘汰部分缓存
         */
        private long maximumSize;
    }

    /**
     * Redis缓存配置类
     * 定义了二级缓存(分布式缓存)的参数
     */
    @Data
    @Accessors(chain = true)
    public static class Redis {

        /**
         * 全局默认过期时间，单位毫秒
         * 默认为0，表示不过期
         */
        private long defaultExpiration = 0;

        /**
         * 每个缓存名称的过期时间，单位毫秒
         * 优先级高于defaultExpiration
         */
        private Map<String, Long> expires = new HashMap<>();

        /**
         * 缓存同步的消息主题名称
         * 用于多节点间的缓存同步
         */
        private String topic = "l2cache:topic";
    }
}
