package com.oimc.aimin.admin.model.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.Data;

@Data
public class TokenInfoVO {
    private String tokenName;
    private String tokenValue;

    public TokenInfoVO(SaTokenInfo tokenInfo) {
        this.tokenName = tokenInfo.getTokenName();
        this.tokenValue = tokenInfo.getTokenValue();
    }
}
