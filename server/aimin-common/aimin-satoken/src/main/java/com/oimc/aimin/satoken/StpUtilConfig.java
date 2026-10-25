package com.oimc.aimin.satoken;

import cn.dev33.satoken.config.SaTokenConfig;
import com.oimc.aimin.satoken.admin.StpAdminUtil;
import com.oimc.aimin.satoken.user.StpUserUtil;

public class StpUtilConfig {
    public void init(){
        SaTokenConfig adminConfig = new SaTokenConfig();
        adminConfig.setTokenName("aimin-admin-token");
        adminConfig.setTimeout(2592000);
        adminConfig.setActiveTimeout(-1L);
        // 同账号多端共享同一 token：演示工作台常被浏览器 + 脚本同时登录，
        // isConcurrent=false 会互相顶下线（"token 已被顶下线"），表现为页面刷新后莫名 401
        adminConfig.setIsConcurrent(true);
        adminConfig.setIsShare(true);
        adminConfig.setIsLog(true);
        adminConfig.setTokenStyle("random-64");
        StpAdminUtil.stpLogic.setConfig(adminConfig);

        SaTokenConfig userConfig = new SaTokenConfig();
        userConfig.setTokenName("aimin-auth-token");
        userConfig.setTimeout(2592000);
        userConfig.setActiveTimeout(-1);
        userConfig.setIsConcurrent(false);
        userConfig.setIsShare(true);
        userConfig.setIsLog(true);
        userConfig.setTokenStyle("random-64");
        StpUserUtil.stpLogic.setConfig(userConfig);
    }
}
