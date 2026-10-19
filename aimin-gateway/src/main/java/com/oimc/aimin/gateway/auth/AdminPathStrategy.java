package com.oimc.aimin.gateway.auth;

import com.oimc.aimin.satoken.admin.StpAdminUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminPathStrategy implements LoginCheckStrategy{
    @Override
    public void checkAuth() {
        log.info("admin path check auth");
        StpAdminUtil.stpLogic.checkLogin();
    }
}
