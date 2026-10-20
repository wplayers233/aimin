package com.oimc.aimin.auth.wx.service;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.shaded.com.google.common.base.Joiner;
import com.oimc.aimin.auth.exception.BuildURLException;
import com.oimc.aimin.auth.wx.constant.WxApiUrlConstants;
import com.oimc.aimin.auth.wx.pojo.AccessTokenResult;
import com.oimc.aimin.auth.wx.pojo.Jscode2sessionResult;
import com.oimc.aimin.auth.wx.properties.WXProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WxMiniprogramService {

    private final RestTemplate restTemplate;
    private final WXProperties wxProperties;


    public Jscode2sessionResult Code2Session(String jsCode) {
        Map<String, String> params = new HashMap<>(6);
        params.put("appid", wxProperties.getAppid());
        params.put("secret", wxProperties.getSecret());
        params.put("js_code", jsCode);
        params.put("grant_type", "authorization_code");
        String paramsStr = Joiner.on("&").withKeyValueSeparator("=").join(params);
        String url = STR."\{WxApiUrlConstants.Miniprogram.DO_LOGIN_URL}?\{paramsStr}";
        String response = restTemplate.getForObject(url, String.class);
        return JSONObject.parseObject(response, Jscode2sessionResult.class);
    }

    public AccessTokenResult getAccessToken() throws BuildURLException {
        Map<String, String> params = new HashMap<>(8);
        params.put("appid", wxProperties.getAppid());
        params.put("secret", wxProperties.getSecret());
        params.put("grant_type", "client_credential");
        String url = buildURL(WxApiUrlConstants.Miniprogram.GET_ACCESS_TOKEN_URL, params);
        String response = restTemplate.getForObject(url, String.class);
        return JSONObject.parseObject(response, AccessTokenResult.class);
    }

    public String getUserPhoneNumber(String code,String accessToken,String openid) throws BuildURLException {
        Map<String, String> params = new HashMap<>(1);
        params.put("access_token", accessToken);
        String url = buildURL(WxApiUrlConstants.Miniprogram.GET_USER_PHONE_NUMBER, params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestEntity = new HashMap<>(2);
        requestEntity.put("code", code);
        requestEntity.put("openid", openid);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(requestEntity,headers);
        String response = restTemplate.postForObject(url, entity, String.class);

        System.out.println(response);

        return  "";
    }

    private String buildURL(String baseUrl, Map<String, String> params) throws BuildURLException {
        if (params != null && !params.isEmpty()) {
            String join = Joiner.on("&").withKeyValueSeparator("=").join(params);
            baseUrl += "?" + join;
            return baseUrl;

        }
        throw new BuildURLException("拼接参数不能为空");
    }

}
