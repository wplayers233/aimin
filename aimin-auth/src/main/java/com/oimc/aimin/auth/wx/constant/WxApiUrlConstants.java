package com.oimc.aimin.auth.wx.constant;

public class WxApiUrlConstants {

    public interface Miniprogram{
        /**
         * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html">...</a>
         */
        String DO_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
        /**
         * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/checkSessionKey.html">...</a>
         */
        String CHECK_SESSION_URL = "https://api.weixin.qq.com/wxa/checksession";
        /**
         * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-access-token/getAccessToken.html">...</a>
         */
        String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

        /**
         * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html">...</a>
         */
        String GET_USER_PHONE_NUMBER = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    }
}
