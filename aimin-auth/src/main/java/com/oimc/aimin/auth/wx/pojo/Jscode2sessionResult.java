package com.oimc.aimin.auth.wx.pojo;


import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

@Data
public class Jscode2sessionResult implements Serializable {

    @JSONField(name = "session_key")
    private String sessionKey;
    private String unionid;
    private String errmsg;
    private String openid;
    private int errcode;
}
