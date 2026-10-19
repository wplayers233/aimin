package com.oimc.aimin.auth.service;

import com.oimc.aimin.auth.model.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author root
 * @since 2024/12/23
 */
public interface UserAccountService extends IService<UserAccount> {

    boolean updateByOpenId(UserAccount userAccount);

    boolean updateAvatar(String avatarUrl);

    boolean updateNick(String nickName);
}
