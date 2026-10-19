package com.oimc.aimin.cache.formal.sync;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存同步消息类
 * 用于在集群环境中不同节点间同步缓存操作
 * 当一个节点更新缓存时，会向Redis发布消息，通知其他节点清除本地缓存
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     * 指定要操作的缓存实例
     */
    private String cacheName;
    
    /**
     * 缓存键
     * 指定要操作的缓存项，如果为null则表示清除整个缓存
     */
    private Object key;
}