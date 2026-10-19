package com.oimc.aimin.admin.service.pipeline.delete.handler;

import com.oimc.aimin.admin.service.pipeline.delete.AdminDeleteHandler;
import com.oimc.aimin.admin.service.pipeline.delete.context.AdminDelContext;
import com.oimc.aimin.base.exception.BusinessException;
import com.oimc.aimin.satoken.admin.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/*
 *
 * @author 渣哥
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class PreCheckHandler implements AdminDeleteHandler {
    @Override
    public void handle(AdminDelContext context) {
        Set<Integer> set = context.getAdminIds();
        Integer loginId = StpAdminUtil.getLoginId();
        if(set.contains(loginId)){
            throw BusinessException.of("不能删除自己");
        }
    }
}
