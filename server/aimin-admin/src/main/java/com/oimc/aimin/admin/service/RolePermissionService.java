package com.oimc.aimin.admin.service;

import com.oimc.aimin.admin.model.entity.RolePermission;
import com.github.yulichang.extension.mapping.base.MPJDeepService;

import java.util.List;

/**
 * <p>
 * 角色权限关联表 服务类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
public interface RolePermissionService extends BaseService<RolePermission> {

    void deleteByRoleId(Integer roleId);

    List<RolePermission> getByPermId(Integer id);
}
