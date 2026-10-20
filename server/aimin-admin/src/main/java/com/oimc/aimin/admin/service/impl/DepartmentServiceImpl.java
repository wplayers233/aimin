package com.oimc.aimin.admin.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oimc.aimin.admin.model.entity.AdminRole;
import com.oimc.aimin.admin.model.entity.Department;
import com.oimc.aimin.admin.mapper.DepartmentMapper;
import com.oimc.aimin.admin.service.DepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author root
 * @since 2025/03/13
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    public static final String CACHE_NAME = "dept";

    
    // 以下是BaseService接口方法的实现
    
    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Department> getAll() {
        return list();
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Department> deepGetAll() {
        return listDeep(new LambdaQueryWrapper<Department>());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Department> getByIds(Collection<Integer> ids) {
        return listByIds(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Department> deepGetByIds(Collection<Integer> ids) {
        return listByIdsDeep(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Department getById(Integer id) {
        return super.getById(id);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Department deepGetById(Integer id) {
        return getByIdDeep(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Department department) {
        save(department);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Collection<Department> departments) {
        saveBatch(departments);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void update(Department department) {
        updateById(department);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Integer id) {
        removeById(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Collection<Integer> ids) {
        removeByIds(ids);
    }
    @Override
    public boolean isExists(Integer id) {
        return count(new LambdaQueryWrapper<Department>().eq(Department::getId, id)) > 0;
    }
}
