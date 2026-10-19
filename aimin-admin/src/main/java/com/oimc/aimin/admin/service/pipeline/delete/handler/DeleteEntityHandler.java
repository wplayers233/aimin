package com.oimc.aimin.admin.service.pipeline.delete.handler;

import com.oimc.aimin.admin.mapper.AdminMapper;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.pipeline.delete.AdminDeleteHandler;
import com.oimc.aimin.admin.service.pipeline.delete.context.AdminDelContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/*
 *
 * @author 渣哥
 */

@Component
@Order(999)
@RequiredArgsConstructor
public class DeleteEntityHandler implements AdminDeleteHandler {

    private final AdminService adminService;

    @Override
    public void handle(AdminDelContext context) {
        Set<Integer> adminIds = context.getAdminIds();
        adminService.delete(adminIds);
    }
}