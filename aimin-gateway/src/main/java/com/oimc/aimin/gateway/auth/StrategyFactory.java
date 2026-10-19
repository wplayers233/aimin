package com.oimc.aimin.gateway.auth;

import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 网关鉴权策略路由表。
 *
 * <p>规则：显式列出的前缀走对应策略，**未匹配的路径默认拒绝**。
 * 新增无鉴权路径必须改这里并说明原因，避免再出现“新服务默认公网可访问”的问题。</p>
 */
public class StrategyFactory {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final Map<String, LoginCheckStrategy> STRATEGY_MAP = new LinkedHashMap<>();

    private static final LoginCheckStrategy DENY = new DenyPathStrategy();

    static {
        // 公开接口：登录、验证码、微信 token 等
        STRATEGY_MAP.put("/aimin-auth/public/**", new PublicPathStrategy());
        STRATEGY_MAP.put("/aimin-admin/public/**", new PublicPathStrategy());
        // 后台管理接口：要求管理员登录
        STRATEGY_MAP.put("/aimin-auth/**", new UserPathStrategy());
        STRATEGY_MAP.put("/aimin-admin/**", new AdminPathStrategy());
        // 诊断工作台与管理端共用管理员会话，必须登录后才能调用
        STRATEGY_MAP.put("/aimin-agent/**", new AdminPathStrategy());
    }

    private StrategyFactory() {
    }

    public static LoginCheckStrategy getStrategy(String requestPath) {
        for (Map.Entry<String, LoginCheckStrategy> entry : STRATEGY_MAP.entrySet()) {
            if (PATH_MATCHER.match(entry.getKey(), requestPath)) {
                return entry.getValue();
            }
        }
        return DENY;
    }
}
