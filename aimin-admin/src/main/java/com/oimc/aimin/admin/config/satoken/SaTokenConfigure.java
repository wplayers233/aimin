package com.oimc.aimin.admin.config.satoken;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import com.oimc.aimin.satoken.admin.StpAdminUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 恢复注解式鉴权：除 /public/** 外，所有接口都要求管理员登录，
 * 并执行方法上的 @SaCheckPermission（type = ADMIN）。
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            if ("OPTIONS".equalsIgnoreCase(SaHolder.getRequest().getMethod())) {
                return;
            }
            SaRouter.match("/**")
                    .notMatch("/public/**", "/error", "/favicon.ico")
                    .check(r -> StpAdminUtil.stpLogic.checkLogin());
        })).addPathPatterns("/**");
    }
}
