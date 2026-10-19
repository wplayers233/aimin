package com.oimc.aimin.satoken.admin;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import com.oimc.aimin.base.exception.BusinessException;
import com.oimc.aimin.satoken.Device;

import java.util.List;

public class StpAdminUtil {
    public static StpLogic stpLogic = new StpLogic("ADMIN");


    /**
     * 获取当前登录用户的ID
     * 此方法用于从stpLogic对象中获取登录ID如果登录ID为空，则抛出异常，
     * 表示当前没有用户登录这是为了确保方法的调用方能够明确地知道登录状态，
     * 并进行相应的处理
     *
     * @return 当前登录用户的ID，如果未登录，则抛出异常
     * @throws BusinessException 如果没有用户登录，将抛出此异常，提示"没有登录"
     */
    public static Integer getLoginId() {
        Object loginId = stpLogic.getLoginId();
        if (loginId == null) {
            throw BusinessException.of("admin未登录");
        }
       return Integer.parseInt(loginId.toString());
    }


    /**
     * 使用PC设备进行登录
     * 此方法专门用于PC端的用户登录，通过设置特定的设备类型和配置，
     * 调用登录逻辑以实现PC端的用户认证
     *
     * @param id 用户ID，用于标识尝试登录的用户
     */
    public static SaTokenInfo loginByPc(Integer id) {
        SaLoginModel config = new SaLoginModel();
        config.setDevice(Device.PC);
        config.setIsWriteHeader(false);
        stpLogic.login(id,config);
        return getTokenInfo();
    }


    /**
     * 获取当前请求的令牌信息
     * 此方法用于从stpLogic对象中提取当前请求的令牌信息SaTokenInfo对象包含了有关令牌的各种详细信息，
     * 如用户ID、令牌过期时间等此方法抽象了底层的令牌获取逻辑，为调用者提供了更高级别的封装，
     * 使其无需关注令牌存储和管理的细节
     *
     * @return 当前请求的令牌信息如果当前请求没有关联的令牌，或者令牌解析失败，将返回null
     */
    public static SaTokenInfo getTokenInfo() {
        return stpLogic.getTokenInfo();
    }

    /**
     * 退出登录
     */
    public static void logout() {
        stpLogic.logout();
    }

    /**
     * 获取当前登录用户的权限列表
     * @return 当前登录用户的权限列表，如果当前用户没有登录，将返回null
     */
    public static List<String> permissions() {
        return stpLogic.getPermissionList();
    }
}
