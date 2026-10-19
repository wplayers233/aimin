package com.oimc.aimin.admin.service.pipeline.delete.handler;

import com.oimc.aimin.admin.mapper.AdminRoleMapper;
import com.oimc.aimin.admin.model.entity.Admin;
import com.oimc.aimin.admin.model.entity.AdminRole;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.pipeline.delete.AdminDeleteHandler;
import com.oimc.aimin.admin.service.pipeline.delete.context.AdminDelContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/*
 *
 * @author 渣哥
 */
@Service
@Order(20)
@RequiredArgsConstructor
public class CollectAdminRolesHandler implements AdminDeleteHandler {

    private final AdminService adminService;

    @Override
    public void handle(AdminDelContext context) {
        Set<Integer> adminIds = context.getAdminIds();
        List<Admin> admins = adminService.listByIdsDeep(adminIds);
        if (!admins.isEmpty()) {
            context.getAdminList().addAll(admins);
        }
    }
}
