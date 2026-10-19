package com.oimc.aimin.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.oimc.aimin.admin.model.entity.AdminRole;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.mapper.AdminRoleMapper;
import com.oimc.aimin.admin.mapper.RoleMapper;
import com.oimc.aimin.admin.service.AdminRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {

    private final RoleMapper roleMapper;

    private final static String CACHE_NAME = "admin_role";

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<Role> getRoleListByAid(Integer adminId) {
        MPJLambdaWrapper<Role> wrapper = new MPJLambdaWrapper<Role>()
                .selectAll(Role.class)
                .leftJoin(AdminRole.class, AdminRole::getRoleId, Role::getId)
                .eq(AdminRole::getAdminId, adminId);
        return roleMapper.selectJoinList(Role.class, wrapper);
    }

    
    // 以下是BaseService接口方法的实现
    
    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<AdminRole> getAll() {
        return list();
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<AdminRole> deepGetAll() {
        return listDeep(new LambdaQueryWrapper<AdminRole>());
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<AdminRole> getByIds(Collection<Integer> ids) {
        return listByIds(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public List<AdminRole> deepGetByIds(Collection<Integer> ids) {
        return listByIdsDeep(ids);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public AdminRole getById(Integer id) {
        return super.getById(id);
    }

    @Override
    @Cacheable(value = CACHE_NAME, keyGenerator = "keyGen")
    public AdminRole deepGetById(Integer id) {
        return getByIdDeep(id);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(AdminRole adminRole) {
        save(adminRole);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void insert(Collection<AdminRole> adminRoles) {
        saveBatch(adminRoles);
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void update(AdminRole adminRole) {
        updateById(adminRole);
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
        return count(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getId, id)) > 0;
    }
}
