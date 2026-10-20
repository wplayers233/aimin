package com.oimc.aimin.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.util.SaResult;
import com.oimc.aimin.auth.exception.BuildURLException;
import com.oimc.aimin.auth.wx.pojo.AccessTokenResult;
import com.oimc.aimin.auth.wx.pojo.Jscode2sessionResult;

public interface WxService {

    SaTokenInfo wxLogin(String code) throws BuildURLException;

    Jscode2sessionResult Code2Session(String jsCode);

    AccessTokenResult getAccessToken() throws BuildURLException;

}
