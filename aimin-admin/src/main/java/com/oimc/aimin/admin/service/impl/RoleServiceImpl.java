package com.oimc.aimin.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oimc.aimin.admin.mapper.RolePermissionMapper;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.mapper.RoleMapper;
import com.oimc.aimin.admin.model.request.RoleRequest;
import com.oimc.aimin.admin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;

    public final static String CACHE_NAME = "role";


    @Override
    public boolean checkNameAndCodeUnique(RoleRequest req) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();

        // 检查是否为更新操作
        if (req.getId() != null) {
            // 如果是更新，排除自身ID
            queryWrapper.ne(Role::getId, req.getId());
        }

        // 检查角色名称或角色编码是否已存在；and(...) 收口，避免 OR 优先级把 ne 条件吃掉
        queryWrapper.and(wrapper -> wrapper.eq(Role::getRoleName, req.getRoleName())
                .or()
                .eq(Role::getRoleCode, req.getRoleCode()));

        // 返回true表示唯一（不存在重复），false表示不唯一（存在重复）
        return count(queryWrapper) == 0;
    }


    @Override
    public boolean isExists(String roleName, String roleCode) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq(Role::getRoleName, roleName)
                .or()
                .eq(Role::getRoleCode, roleCode));
        return count(queryWrapper) > 0;
    }



    // 以下是BaseService接口方法的实现
    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Role> getAll() {
        return list();
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Role> deepGetAll() {
        return listDeep(new LambdaQueryWrapper<Role>());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Role> getByIds(Collection<Integer> ids) {
        return listByIds(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Role> deepGetByIds(Collection<Integer> ids) {
        return listByIdsDeep(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Role getById(Integer id) {
        return super.getById(id);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Role deepGetById(Integer id) {
        return getByIdDeep(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Role role) {
        save(role);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Collection<Role> roles) {
        saveBatch(roles);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void update(Role role) {
        updateById(role);
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
        return count(new LambdaQueryWrapper<Role>().eq(Role::getId, id)) > 0;
    }
}