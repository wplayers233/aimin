package com.oimc.aimin.cache.formal.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.extension.mapping.base.MPJDeepService;
import com.oimc.aimin.cache.formal.AiminCacheManager;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Transactional
public interface BaseCacheService<T> extends MPJDeepService<T> {

    // 子类必须实现，提供缓存名称
    String getCacheName();

    ApplicationContext getApplicationContext();

    // 获取缓存管理器
    default AiminCacheManager getCacheManager() {
        ApplicationContext applicationContext = getApplicationContext();
        return applicationContext.getBean(AiminCacheManager.class);
    }

    default String keyGen(String methodName, Object... params) {
        String delimitedString = StringUtils.arrayToDelimitedString(params, "_");
        String md5Hex16 = DigestUtil.md5Hex16(delimitedString);
        return STR."\{methodName}:\{md5Hex16}";
    }

    default List<T> cacheGetAll(){
        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName);
        assert cache != null;
        @SuppressWarnings("unchecked")
        List<T> result = cache.get(cacheKey, List.class);
        if (result != null) {
            return result;
        }

        result = list();

        cache.put(cacheKey, result);
        return result;
    }

    default List<T> cacheDeepGetAll(){
        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName);
        assert cache != null;
        @SuppressWarnings("unchecked")
        List<T> result = cache.get(cacheKey, List.class);
        if (result != null) {
            return result;
        }

        result = listDeep(new LambdaQueryWrapper<>());

        cache.put(cacheKey, result);
        return result;
    }

    default List<T> cacheGetByIds(Collection<Integer> ids){
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName, ids.toArray());
        assert cache != null;
        @SuppressWarnings("unchecked")
        List<T> result = cache.get(cacheKey, List.class);
        if (result != null) {
            return result;
        }

        result = listByIds(ids);

        cache.put(cacheKey, result);
        return result;
    }

    default List<T> cacheDeepGetByIds(Collection<Integer> ids){
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName, ids.toArray());
        assert cache != null;
        @SuppressWarnings("unchecked")
        List<T> result = cache.get(cacheKey, List.class);
        if (result != null) {
            return result;
        }

        result = listByIdsDeep(ids);
        cache.put(cacheKey, result);
        return result;
    }

    default T cacheGetById(Integer id){
        if (id == null) {
            return null;
        }

        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName, id);
        assert cache != null;
        T result = cache.get(cacheKey, getEntityClass());
        if (result != null) {
            return result;
        }

        result = MPJDeepService.super.getById(id);

        if (result != null) {
            cache.put(cacheKey, result);
        }
        return result;
    }

    default T cacheDeepGetById(Integer id){
        if (null == id) {
            return null;
        }

        Cache cache = getCacheManager().getCache(getCacheName());
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String cacheKey = keyGen(methodName, id);
        assert cache != null;
        T result = cache.get(cacheKey, getEntityClass());
        if (result != null) {
            return result;
        }

        result = getByIdDeep(id);

        if (result != null) {
            cache.put(cacheKey, result);
        }
        return result;
    }

    default void cacheInsert(T entity){
        if (entity == null) {
            return;
        }
        save(entity);
        clearCache();
    }

    default void cacheInsert(Collection<T> entities){
        if (entities == null || entities.isEmpty()) {
            return;
        }
        saveBatch(entities);
        clearCache();
    }

    default void cacheUpdate(T entity){
        if (entity == null) {
            return;
        }
        updateById(entity);
        clearCache();
    }

    default void cacheUpdate(List<T> list){
        if (list.isEmpty()) {
            return;
        }
        updateBatchById(list);
        clearCache();
    }


    default void cacheDelete(Integer id){
        if (id == null) {
            return;
        }
        removeById(id);
        clearCache();
    }

    default void cacheDelete(Collection<Integer> ids){
        if (ids == null || ids.isEmpty()) {
            return;
        }
        removeByIds(ids);
        clearCache();
    }

    default void clearCache() {
        Cache cache = getCacheManager().getCache(getCacheName());
        if (cache != null) {
            cache.clear();
        }
    }
}