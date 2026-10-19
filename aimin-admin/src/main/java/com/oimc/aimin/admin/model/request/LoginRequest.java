package com.oimc.aimin.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录参数")
public class LoginRequest {
    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码ID，由 /public/auth/captcha 下发")
    @NotBlank(message = "验证码不能为空")
    private String captchaId;

    @Schema(description = "验证码答案")
    @NotBlank(message = "验证码不能为空")
    private String captchaAnswer;
}
