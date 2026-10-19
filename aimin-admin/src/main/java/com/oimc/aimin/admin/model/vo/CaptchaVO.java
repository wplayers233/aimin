package com.oimc.aimin.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码下发对象。
 * 只下发题面和一次性 ID，答案保存在服务端。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码")
public class CaptchaVO {

    @Schema(description = "验证码ID，登录时原样回传")
    private String captchaId;

    @Schema(description = "验证码题目，例如 3 + 5 = ?")
    private String question;
}
