package com.oimc.aimin.cache.formal.util;

import cn.hutool.json.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * 封装了Redis的常用操作，提供了更便捷的接口
 * 作为二级缓存的实现，负责与Redis交互
 */
@Setter
@Getter
public class L2RedisCache {


    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 向Redis中设置键值对，并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间(毫秒)
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 向Redis中设置键值对(永久有效)
     * 
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 从Redis中获取值
     * 
     * @param key 键
     * @return 值，不存在则返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 向Redis的hash结构中批量添加映射
     * 
     * @param key hash的键
     * @param m 要添加的映射集合
     */
    public void hashAddAll(String key, Map<String, JSONObject> m) {
        redisTemplate.opsForHash().putAll(key, m);
    }

    /**
     * 向Redis的hash结构中添加单个映射
     * 
     * @param key hash的键
     * @param filed hash字段
     * @param value 值
     */
    public void hashAdd(String key, String filed, Object value) {
        redisTemplate.opsForHash().put(key, filed, value);
    }

    /**
     * 从Redis的hash结构中获取单个字段值
     * 
     * @param key hash的键
     * @param filed hash字段
     * @return 字段值，不存在则返回null
     */
    public Object hashSelectOne(String key, String filed) {
        return redisTemplate.opsForHash().get(key, filed);
    }

    /**
     * 获取Redis的hash结构中所有键值对
     * 
     * @param key hash的键
     * @return 所有键值映射
     */
    public Map<Object, Object> hashSelectAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Redis中的键
     * 
     * @param key 要删除的键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除Redis中的键
     * 
     * @param keys 要删除的键集合
     */
    public void deleteKeys(Set<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 删除Redis的hash结构中的单个字段
     * 
     * @param key hash的键
     * @param filed 要删除的hash字段
     */
    public void deleteHashOne(String key, String filed) {
        redisTemplate.opsForHash().delete(key, filed);
    }

    /**
     * 向Redis的set集合中添加元素
     * 
     * @param key set的键
     * @param value 要添加的元素
     * @return 添加成功的元素数量
     */
    public Long setAdd(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 获取Redis的set集合中的所有元素
     * 
     * @param key set的键
     * @return 所有元素组成的集合
     */
    public Set getSetData(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否存在于Redis的set集合中
     * 
     * @param key set的键
     * @param value 要检查的元素
     * @return 是否存在
     */
    public boolean isValiDateSet(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 获取两个Redis的set集合的交集
     * 
     * @param key1 第一个set的键
     * @param key2 第二个set的键
     * @return 交集集合
     */
    public Set intersect(String key1, String key2) {
        return redisTemplate.opsForSet().intersect(key1, key2);
    }
    
    /**
     * 获取两个Redis的set集合的并集
     * 
     * @param key1 第一个set的键
     * @param key2 第二个set的键
     * @return 并集集合
     */
    public Set unionSet(String key1, String key2) {
        return redisTemplate.opsForSet().union(key1, key2);
    }
    
    /**
     * 向Redis的list结构中添加字符串元素(左侧推入)
     * 
     * @param key list的键
     * @param value 要添加的字符串
     */
    public void listAdd(String key, String value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从Redis的list结构中获取并移除右端元素
     * 
     * @param key list的键
     * @return 移除的元素
     */
    public String listGet(String key) {
        return (String) redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 向Redis的list结构中添加对象元素(左侧推入)
     * 
     * @param key list的键
     * @param object 要添加的对象
     */
    public void listAddObject(String key, Object object) {
        redisTemplate.opsForList().leftPush(key, object);
    }

    /**
     * 从Redis的list结构中获取并移除右端对象
     * 
     * @param key list的键
     * @return 移除的对象
     */
    public Object listGetObject(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 对Redis中的数字值进行原子递增操作
     * 
     * @param key 键
     * @param value 增量
     * @return 增加后的值
     */
    public long getSerialNumber(String key, long value) {
        return redisTemplate.opsForValue().increment(key, value);
    }

    /**
     * 对Redis的hash结构中的数字字段进行原子递增操作
     * 
     * @param key hash的键
     * @param hashKey hash的字段
     * @param value 增量
     * @return 增加后的值
     */
    public long getSurplusNumber(String key, String hashKey, long value) {
        return redisTemplate.opsForHash().increment(key, hashKey, value);
    }
    
    /**
     * 设置Redis的hash结构中的字段值
     * 
     * @param key hash的键
     * @param filed hash的字段
     * @param value 要设置的值
     */
    public void hashPutSurplusNumber(String key, String filed, Long value) {
        redisTemplate.opsForHash().put(key, filed, value.toString());
    }
    
    /**
     * 获取Redis的hash结构中的字段值
     * 
     * @param key hash的键
     * @param filed hash的字段
     * @return 字段值
     */
    public Object getSurplusNumber(String key, String filed) {
        return redisTemplate.opsForHash().get(key, filed);
    }

    /**
     * 对Redis中的值进行原子递增操作
     * 
     * @param key 键
     * @param expire 增量
     * @return 增加后的值
     */
    public long incr(String key, long expire) {
        return redisTemplate.opsForValue().increment(key, expire);
    }

    /**
     * 查找符合给定模式的所有Redis键
     * 
     * @param key 键模式
     * @return 匹配的键集合
     */
    public Set<String> keys(String key) {
        return redisTemplate.keys(key);
    }

    /**
     * 批量获取Redis中的值
     * 
     * @param keyList 键列表
     * @return 对应的值列表
     */
    public List multiGet(List keyList) {
        return redisTemplate.opsForValue().multiGet(keyList);
    }

    /**
     * 当键不存在时设置值（SETNX操作）
     *
     * @param key 键
     * @param value 值
     * @return 是否设置成功（true表示键不存在并已设置，false表示键已存在）
     */
    public boolean setIfAbsent(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    /**
     * 当键不存在时设置值（SETNX操作），并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间(毫秒)
     * @return 是否设置成功（true表示键不存在并已设置，false表示键已存在）
     */
    public boolean setIfAbsent(String key, Object value, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.MILLISECONDS));
    }
}
