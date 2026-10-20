package com.oimc.aimin.admin.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.oimc.aimin.admin.common.constant.SysConstants;
import com.oimc.aimin.admin.facade.PermissionFacadeService;
import com.oimc.aimin.admin.facade.RoleFacadeService;
import com.oimc.aimin.admin.service.PermissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Sa-Token 权限认证接口实现
 *
 * @author 渣哥
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionService permissionService;

    private final RoleFacadeService roleFacadeService;

    private final PermissionFacadeService permissionFacadeService;


    /**
     * 获取用户权限列表
     * 
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 权限代码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Integer adminId = Integer.parseInt(loginId.toString());
        Set<String> roleCodeSet = roleFacadeService.getRoleCodesByAdminId(adminId);
        if(roleCodeSet.contains(SysConstants.SUPER_ROLE_CODE)){
            return CollUtil.newArrayList(SysConstants.ALL_PERMISSION);
        }
        // Set<String> permCodes = permissionService.getPermCodesByAdminId(adminId);
        Set<String> permCodes = permissionFacadeService.getPermCodeByAdminId(adminId);
        return CollUtil.newArrayList(permCodes);
    }

    /**
     * 获取用户角色列表
     * 根据登录ID获取用户的角色代码列表，用于权限验证
     * 
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 角色代码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Integer adminId = Integer.parseInt(loginId.toString());
        Set<String> roles = roleFacadeService.getRoleCodesByAdminId(adminId);
        return CollUtil.newArrayList(roles);
    }
}
