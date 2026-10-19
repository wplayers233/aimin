package com.oimc.aimin.admin.facade;

import com.oimc.aimin.admin.model.entity.Admin;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.entity.RolePermission;
import com.oimc.aimin.admin.model.request.RoleRequest;
import com.oimc.aimin.admin.service.AdminRoleService;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.RolePermissionService;
import com.oimc.aimin.admin.service.RoleService;
import com.oimc.aimin.admin.utils.ObjectConvertor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 
 * @author 渣哥
*/
@Service
@RequiredArgsConstructor
public class RoleFacadeService {

    private final RoleService roleService;
    private final AdminService adminService;
    private final AdminRoleService adminRoleService;
    private final RolePermissionService rolePermissionService;
    private final ObjectConvertor objectConvertor;

    public Set<String> getRoleCodesByAdminId(Integer adminId){
        Admin admin = adminService.getByIdDeep(adminId);
        List<Role> roles = roleService.getByIds(admin.getRoleIds());
        return roles.stream()
                .map(Role::getRoleCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 创建角色
     *
     * @param req 创建角色请求对象
     * @return 新创建的角色ID
     */
    public Integer createRole(RoleRequest req) {
        boolean exists = roleService.isExists(req.getRoleName(), req.getRoleCode());
        if (exists) {
            throw new RuntimeException("角色名称或角色编码已存在");
        }
        Role role = objectConvertor.toRole(req);
        roleService.insert(role);
        return role.getId();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    @Transactional
    public void deleteRole(Integer id) {
        boolean exists = roleService.isExists(id);
        if (!exists) {
            throw new RuntimeException("角色不存在");
        }
        rolePermissionService.deleteByRoleId(id);
        roleService.delete(id);
    }

    /**
     * 更新角色信息
     *
     * @param request 更新角色请求对象
     */
    public void updateRole(RoleRequest request) {
        Role role = roleService.getById(request.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        boolean unique = roleService.checkNameAndCodeUnique(request);
        if (!unique) {
            throw new RuntimeException("角色名称或角色编码已存在");
        }
        Role coverRole = objectConvertor.toRole(request);
        roleService.update(coverRole);
    }


    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    @Transactional
    public void assignPermissions(Integer roleId, List<Integer> permissionIds) {
        rolePermissionService.deleteByRoleId(roleId);
        List<RolePermission> list = permissionIds.stream().map(permissionId -> {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermId(permissionId);
            return rolePermission;
        }).toList();
        rolePermissionService.insert(list);
    }

    /**
     * 获取管理员的角色列表
     *
     * @param adminId 管理员ID
     * @return 角色列表
     */
    public List<Role> getAdminRoles(Integer adminId) {
        return adminRoleService.getRoleListByAid(adminId);
    }
}
