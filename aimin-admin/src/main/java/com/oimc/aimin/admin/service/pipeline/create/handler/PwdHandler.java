package com.oimc.aimin.admin.service.pipeline.create.handler;

import com.oimc.aimin.admin.service.pipeline.create.AdminCreateHandler;
import com.oimc.aimin.admin.service.pipeline.create.context.AdminCreateContext;
import com.oimc.aimin.admin.utils.PwdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@RequiredArgsConstructor
public class PwdHandler implements AdminCreateHandler {

    @Override
    public void handle(AdminCreateContext context) {
        String passwordEncoder = PwdUtils.bcryptPasswordEncoder(context.getPassword());
        context.setEncryptedPassword(passwordEncoder);
    }
}
