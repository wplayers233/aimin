package com.oimc.aimin.admin.service.pipeline.create.handler;


import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.pipeline.create.AdminCreateHandler;
import com.oimc.aimin.admin.service.pipeline.create.context.AdminCreateContext;
import com.oimc.aimin.base.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class UsernamePhoneValidationHandler implements AdminCreateHandler {

    private final AdminService adminService;

    @Override
    public void handle(AdminCreateContext context) {
        boolean exists = adminService.isExists(context.getUsername(), context.getPhone());
        if (exists) {
            throw new BusinessException("用户名或手机号已存在");
        }
    }
}
