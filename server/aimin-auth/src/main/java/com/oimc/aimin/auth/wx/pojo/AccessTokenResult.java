package com.oimc.aimin.auth.wx.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class AccessTokenResult {
    @JSONField(name = "access_token")
    private String accessToken;
    @JSONField(name = "expires_in")
    private String refreshIn;
}
