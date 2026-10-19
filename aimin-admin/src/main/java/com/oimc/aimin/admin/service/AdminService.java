package com.oimc.aimin.admin.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oimc.aimin.admin.model.request.AdminRequest;
import com.oimc.aimin.admin.model.request.LoginRequest;
import com.oimc.aimin.admin.model.entity.Admin;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 管理员表 服务类
 * </p>
 *
 * @author root
 * @since 2025/02/26
 */
public interface AdminService extends BaseService<Admin>{

    SaTokenInfo login(LoginRequest login);

    Page<Admin> searchFuzzyWithDept(String content, Set<Integer> deptIds, Integer currentPage,Integer pageSize);

    boolean isExists(String username, String phone);

    void deleteRelationRoles(Set<Integer> adminId);

    boolean checkPhoneUsernameUnique(AdminRequest req);

    void updateRelationRoles(Integer aid, List<Integer> roleIds);
}