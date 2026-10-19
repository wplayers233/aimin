package com.oimc.aimin.satoken.user;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import com.oimc.aimin.base.exception.BusinessException;
import com.oimc.aimin.satoken.Device;

public class StpUserUtil {

    public static StpLogic stpLogic = new StpLogic("USER");

    /**
     * 获取当前登录用户的ID
     * 此方法用于从stpLogic对象中获取登录ID如果登录ID为空，则抛出异常，
     *
     * @return 当前登录用户的ID，即openId
     * @throws BusinessException 如果没有用户登录，将抛出此异常，提示"没有登录"
     */
    public static String getLoginId() {
        Object loginId = stpLogic.getLoginId();
        if (loginId == null) {
            throw BusinessException.of("用户未登录");
        }
        return loginId.toString();
    }

    /**
     * 使用PC设备进行登录
     * 此方法专门用于PC端的用户登录，通过设置特定的设备类型和配置，
     * 调用登录逻辑以实现PC端的用户认证
     *
     * @param openId 用户ID，用于标识尝试登录的用户
     */
    public static void loginByPc(String openId) {
        SaLoginModel config = new SaLoginModel();
        config.setDevice(Device.PC);
        config.setIsWriteHeader(false);
        stpLogic.login(openId, config);
    }

    public static SaTokenInfo loginByMiniProgram(String openId) {
        SaLoginModel config = new SaLoginModel();
        config.setDevice(Device.MINI_PROGRAM);
        config.setIsWriteHeader(false);
        stpLogic.login(openId, config);
        return getTokenInfo();
    }

    /**
     * 获取当前请求的令牌信息
     *
     * @return 当前请求的令牌信息如果当前请求没有关联的令牌，或者令牌解析失败，将返回null
     */
    public static SaTokenInfo getTokenInfo() {
        return stpLogic.getTokenInfo();
    }

    public static Boolean isLogin(){
        return stpLogic.isLogin();
    }
}
