package com.oimc.aimin.admin.model.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.Data;

import java.util.List;

/**
 * 登录响应数据视图对象
 * 包含登录成功后返回给前端的所有必要信息
 *
 * @author 渣哥
 */
@Data
public class LoginVO {
    
    /**
     * 验证码校验结果
     * true表示验证码验证通过，false表示验证码验证失败
     */
    private Boolean captchaVerifyResult;

    /**
     * 登录成功后返回的Token信息对象
     */
    private String tokenName;

    /**
     * 登录成功后返回的Token信息对象
     */
    private String tokenValue;
    
    /**
     * 用户权限列表
     * 包含当前登录用户拥有的所有权限代码，用于前端权限控制
     * 由PermissionService.getPermCodesByAdminId方法获取
     */
    private List<String> permissions;

    public LoginVO(SaTokenInfo tokenInfo, List<String> permissions) {
        this.tokenName = tokenInfo.getTokenName();
        this.tokenValue = tokenInfo.getTokenValue();
        this.permissions = permissions;
    }

    public void setTokenInfo(SaTokenInfo tokenInfo) {
        this.tokenName = tokenInfo.getTokenName();
        this.tokenValue = tokenInfo.getTokenValue();
    }

    public LoginVO() {

    }
}
