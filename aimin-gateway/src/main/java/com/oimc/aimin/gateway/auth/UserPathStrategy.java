package com.oimc.aimin.gateway.auth;

import com.oimc.aimin.satoken.user.StpUserUtil;

public class UserPathStrategy implements LoginCheckStrategy{
    @Override
    public void checkAuth() {
        StpUserUtil.stpLogic.checkLogin();
    }
}
