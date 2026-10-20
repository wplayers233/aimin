package com.oimc.aimin.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "图形验证参数")
public class CaptchaVerifyRequest {

    /**
     * 用于存储验证码验证所需参数的字符串变量
     */
    @Schema(description = "图形验证码")
    @NotBlank(message = "图形验证码不能为空")
    private String captchaVerifyParam;
}
