package com.oimc.aimin.admin.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.oimc.aimin.admin.model.entity.Admin;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 管理员表 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2025/02/26
 */
public interface AdminMapper extends MPJBaseMapper<Admin> {
    
    /**
     * 获取管理员完整信息，包括角色和权限
     * @param adminId 管理员ID
     * @return 管理员信息，包含角色和权限
     */
    Admin getAdminWithRolesAndPermissions(@Param("adminId") Integer adminId);
}
