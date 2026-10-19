package com.oimc.aimin.cache.formal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Aimin缓存配置属性类
 * 用于绑定配置文件中以"aimin-cache"为前缀的配置项
 * 负责从配置文件中加载二级缓存的相关配置
 */
@Data
@ConfigurationProperties(prefix = "aimin-cache")
public class AiminCacheProperties {

    /**
     * 缓存配置对象
     * 包含缓存的所有配置信息，如Caffeine参数、Redis参数等
     */
    private AiminCacheConfig config;

}
