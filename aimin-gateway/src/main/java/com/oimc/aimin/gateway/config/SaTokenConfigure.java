package com.oimc.aimin.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.util.SaResult;
import com.oimc.aimin.gateway.auth.StrategyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class SaTokenConfigure {

    private static final Logger logger = Logger.getLogger(SaTokenConfigure.class.getName());

    /**
     * 允许跨域的来源白名单，逗号分隔；默认只放行本地 Vite 开发端口。
     * 生产部署时通过 AIMIN_CORS_ORIGINS 覆盖，不要退化成 "*"（带凭证时浏览器也会拒绝）。
     */
    @Value("${aimin.gateway.cors.allowed-origins:http://localhost:5174,http://127.0.0.1:5174}")
    private String allowedOrigins;

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有请求；公开路径由 StrategyFactory 里的 /public/** 规则放行
                .addInclude("/**")
                .setAuth(obj -> {
                    String path = SaHolder.getRequest().getRequestPath();
                    StrategyFactory.getStrategy(path).checkAuth();
                })
                .setError(e -> {
                    int code = 500;
                    if (e instanceof NotLoginException) {
                        code = 401;
                    } else if (e instanceof NotPermissionException) {
                        code = 403;
                    } else {
                        logger.log(Level.WARNING, "sa全局异常", e);
                    }
                    return SaResult.error(e.getMessage()).setCode(code);
                })
                .setBeforeAuth(obj -> {
                    String origin = SaHolder.getRequest().getHeader("Origin");
                    if (origin != null && isAllowedOrigin(origin)) {
                        SaHolder.getResponse()
                                .setHeader("Access-Control-Allow-Origin", origin)
                                .setHeader("Access-Control-Allow-Credentials", "true")
                                .setHeader("Vary", "Origin")
                                .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, HEAD")
                                .setHeader("Access-Control-Max-Age", "3600")
                                .setHeader("Access-Control-Allow-Headers",
                                        "aimin-admin-token, aimin-auth-token, Content-Type, Authorization");
                    }
                    SaRouter.match(SaHttpMethod.OPTIONS)
                            .free(r -> {
                            })
                            .back();
                });
    }

    private boolean isAllowedOrigin(String origin) {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .anyMatch(origin::equals);
    }
}
