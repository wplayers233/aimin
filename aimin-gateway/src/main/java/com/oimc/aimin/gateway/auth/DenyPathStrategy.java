package com.oimc.aimin.gateway.auth;

import cn.dev33.satoken.exception.NotPermissionException;

/**
 * 默认拒绝策略：没有显式配置鉴权规则的路径一律不允许访问。
 * 之前默认返回 PublicPathStrategy，导致新增服务（例如 /aimin-agent/**）天然裸奔。
 */
public class DenyPathStrategy implements LoginCheckStrategy {

    @Override
    public void checkAuth() {
        throw new NotPermissionException("该接口未配置鉴权策略，已拒绝访问");
    }
}
