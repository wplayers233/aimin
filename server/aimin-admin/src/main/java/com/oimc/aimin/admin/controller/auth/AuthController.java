package com.oimc.aimin.admin.controller.auth;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.oimc.aimin.admin.model.request.LoginRequest;
import com.oimc.aimin.admin.model.vo.CaptchaVO;
import com.oimc.aimin.admin.model.vo.LoginVO;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.impl.CaptchaService;
import com.oimc.aimin.base.response.Result;
import com.oimc.aimin.satoken.admin.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/auth")
public class AuthController {

    private final AdminService adminService;

    private final CaptchaService captchaService;

    /**
     * 下发一次性验证码。
     * 前端先取题面，登录时回传 captchaId + 答案。
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.success(captchaService.create());
    }

    @Operation(summary = "登录")
    @PostMapping("/token")
    public Result<?> token(@Valid @RequestBody LoginRequest login) {
        LoginVO loginVO = new LoginVO();
        if (!captchaService.verify(login.getCaptchaId(), login.getCaptchaAnswer())) {
            loginVO.setCaptchaVerifyResult(false);
            return Result.success(loginVO);
        }
        SaTokenInfo tokenInfo = adminService.login(login);
        List<String> permissions = StpAdminUtil.permissions();
        loginVO.setCaptchaVerifyResult(true);
        loginVO.setTokenInfo(tokenInfo);
        loginVO.setPermissions(permissions);
        return Result.success(loginVO);
    }

    // 登出接口
    @Operation(summary = "退出登录")
    @GetMapping("/logout")
    public Result<?> logout() {
        StpAdminUtil.logout();
        return Result.success();
    }
}
