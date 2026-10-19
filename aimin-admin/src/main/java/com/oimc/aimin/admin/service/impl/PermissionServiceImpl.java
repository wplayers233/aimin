package com.oimc.aimin.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.oimc.aimin.admin.common.enums.EnableStatusEnum;
import com.oimc.aimin.admin.model.entity.*;
import com.oimc.aimin.admin.mapper.PermissionMapper;
import com.oimc.aimin.admin.service.PermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oimc.aimin.admin.utils.ObjectConvertor;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;

    public static final String CACHE_NAME = "perm";

    private final ObjectConvertor objectConvertor;


    /**
     * 根据父ID获取权限列表
     * @param pid 父权限ID
     * @return 权限列表
     */
    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> getListByPid(Integer pid) {
        // 一次取全量后在内存分组，替代原来“每条子权限再 count 一次”的 N+1 查询
        List<Permission> all = list();
        Map<Integer, Long> childCount = all.stream()
                .filter(permission -> permission.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId, Collectors.counting()));
        List<Permission> list = all.stream()
                .filter(permission -> Objects.equals(permission.getParentId(), pid))
                .collect(Collectors.toList());
        list.forEach(permission -> permission.setHasChildren(childCount.getOrDefault(permission.getId(), 0L) > 0));
        return list;
    }

    /**
     * 根据管理员ID获取权限代码列表
     * 
     * @param adminId 管理员ID
     * @return 权限代码列表
     */
    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Set<String> getPermCodesByAdminId(Integer adminId) {
        // 使用MPJLambdaWrapper进行多表关联查询，获取用户的所有权限
        MPJLambdaWrapper<Permission> wrapper = new MPJLambdaWrapper<>();
        wrapper.select(Permission::getPermKey)  // 只查询权限代码字段
               .innerJoin(RolePermission.class, RolePermission::getPermId, Permission::getId)
               .innerJoin(AdminRole.class, AdminRole::getRoleId, RolePermission::getRoleId)
               .eq(AdminRole::getAdminId, adminId)
               .eq(Permission::getStatus, EnableStatusEnum.ENABLE)  // 只获取启用状态的权限
               .groupBy(Permission::getPermKey);  // 去重
        List<Permission> permissions = permissionMapper.selectJoinList(Permission.class, wrapper);
        // 提取permKey字段并返回
        return permissions.stream()
                .map(Permission::getPermKey)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> getChildren(Integer id) {
        LambdaQueryWrapper<Permission> childrenQuery = new LambdaQueryWrapper<>();
        childrenQuery.eq(Permission::getParentId, id);
        return list(childrenQuery);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public boolean isExistsByPermKey(String permKey) {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPermKey, permKey);
        return count(queryWrapper) > 0;
    }


    // 以下是BaseService接口方法的实现

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> getAll() {
        return list();
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> deepGetAll() {
        return listDeep(new LambdaQueryWrapper<Permission>());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> getByIds(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Permission> deepGetByIds(Collection<Integer> ids) {
        return listByIdsDeep(ids);
    }

    @Override
    public Permission getById(Integer id) {
        return null;
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public Permission deepGetById(Integer id) {
        return getByIdDeep(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Permission permission) {
        save(permission);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Collection<Permission> permissions) {
        saveBatch(permissions);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void update(Permission permission) {
        updateById(permission);
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
        return count(new LambdaQueryWrapper<Permission>().eq(Permission::getId, id)) > 0;
    }
}
