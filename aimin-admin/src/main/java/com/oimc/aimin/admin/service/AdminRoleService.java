package com.oimc.aimin.admin.service;

import com.oimc.aimin.admin.model.entity.AdminRole;
import com.oimc.aimin.admin.model.entity.Role;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 用户角色关联表 服务类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
public interface AdminRoleService extends BaseService<AdminRole> {

    List<Role> getRoleListByAid(Integer adminId);
}
