package com.oimc.aimin.admin.service;

import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.request.RoleRequest;


/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
public interface RoleService extends BaseService<Role> {


    boolean isExists(String roleName, String roleCode);

    boolean checkNameAndCodeUnique(RoleRequest req);
}