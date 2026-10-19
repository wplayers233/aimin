package com.oimc.aimin.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oimc.aimin.admin.mapper.RoleMapper;
import com.oimc.aimin.admin.model.entity.AdminRole;
import com.oimc.aimin.admin.model.entity.RolePermission;
import com.oimc.aimin.admin.mapper.RolePermissionMapper;
import com.oimc.aimin.admin.service.RolePermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oimc.aimin.admin.mapper.PermissionMapper;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.entity.Permission;
import com.oimc.aimin.base.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private static final String CACHE_NAME = "rolePer";

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<RolePermission> getAll() {
        return list();
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<RolePermission> deepGetAll() {

        return listDeep(new LambdaQueryWrapper<RolePermission>());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<RolePermission> getByIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return listByIds(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<RolePermission> deepGetByIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return listByIdsDeep(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public RolePermission getById(Integer id) {
        if (id == null) {
            return null;
        }
        return super.getById(id);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public RolePermission deepGetById(Integer id) {
        if (id == null) {
            return null;
        }
        return getByIdDeep(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(RolePermission rolePermission) {
        if (rolePermission != null) {
            save(rolePermission);
        }
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = CACHE_NAME, allEntries = true),
                    @CacheEvict(value = RoleServiceImpl.CACHE_NAME, allEntries = true)
            }
    )
    public void insert(Collection<RolePermission> rolePermissions) {
        if (!CollectionUtils.isEmpty(rolePermissions)) {
            saveBatch(rolePermissions);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void update(RolePermission rolePermission) {
        if (rolePermission != null && rolePermission.getId() != null) {
            updateById(rolePermission);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Integer id) {
        if (id != null) {
            removeById(id);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Collection<Integer> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            removeByIds(ids);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteByRoleId(Integer roleId) {
        if (roleId != null) {
            LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RolePermission::getRoleId, roleId);
            remove(wrapper);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<RolePermission> getByPermId(Integer id) {
        LambdaQueryWrapper<RolePermission> eq = new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermId, id);
        return list(eq);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public boolean isExists(Integer id) {
        return count(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getId, id)) > 0;
    }
}
