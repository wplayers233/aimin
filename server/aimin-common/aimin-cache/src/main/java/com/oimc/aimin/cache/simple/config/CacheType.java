package com.oimc.aimin.cache.simple.config;

/**
 * 缓存操作类型枚举
 * 定义了缓存操作的不同行为模式
 */
public enum CacheType {

    /**
     * 完整缓存模式：先查询缓存，缓存不存在时查询数据库并更新缓存
     * 查询顺序：Caffeine -> Redis -> 数据库
     */
    FULL,   
    
    /**
     * 只存缓存模式：强制更新缓存，不查询现有缓存
     * 直接查询数据库并更新缓存，用于确保缓存与数据库同步
     */
    PUT,    
    
    /**
     * 删除缓存模式：删除指定的缓存内容
     * 用于清除可能过期或失效的缓存数据
     */
    DELETE;  

    /**
     * 默认构造函数
     */
    CacheType() {
    }
}
