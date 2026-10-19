package com.oimc.aimin.admin.service;

import com.github.yulichang.extension.mapping.base.MPJDeepService;

import java.util.Collection;
import java.util.List;

/*
 *
 * @author 渣哥
 */
public interface BaseService<T> extends MPJDeepService<T> {

    List<T> getAll();

    List<T> deepGetAll();

    List<T> getByIds(Collection<Integer> ids);

    List<T> deepGetByIds(Collection<Integer> ids);

    T getById(Integer id);

    T deepGetById(Integer id);

    void insert(T t);

    void insert(Collection<T> ts);

    void update(T t);

    void delete(Integer id);

    void delete(Collection<Integer> ids);

    boolean isExists(Integer id);


}
