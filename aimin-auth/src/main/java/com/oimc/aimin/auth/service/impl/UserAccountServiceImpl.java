package com.oimc.aimin.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.oimc.aimin.auth.model.entity.UserAccount;
import com.oimc.aimin.auth.mapper.UserAccountMapper;
import com.oimc.aimin.auth.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oimc.aimin.satoken.user.StpUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author root
 * @since 2024/12/23
 */
@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    private final UserAccountMapper userAccountMapper;

    @Override
    public boolean updateByOpenId(UserAccount userAccount) {
        // 创建 LambdaUpdateWrapper 实例
        LambdaUpdateWrapper<UserAccount> updateWrapper = new LambdaUpdateWrapper<>();

        // 设置更新条件：匹配指定的 openid
        updateWrapper.eq(UserAccount::getOpenid, userAccount.getOpenid());

        // 设置需要更新的字段
        updateWrapper
                .set(UserAccount::getNick, userAccount.getNick())
                .set(UserAccount::getPhone, userAccount.getPhone())
                .set(UserAccount::getAvatar, userAccount.getAvatar());

        // 执行更新操作
        return userAccountMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean updateAvatar(String avatarUrl) {
        String loginId = StpUserUtil.getLoginId();
        UserAccount userAccount = new UserAccount();
        userAccount.setOpenid(loginId);
        userAccount.setAvatar(avatarUrl);
        return updateByOpenId(userAccount);
    }

    @Override
    public boolean updateNick(String nickName) {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String loginId = tokenInfo.getLoginId().toString();
        UserAccount userAccount = new UserAccount();
        userAccount.setOpenid(loginId);
        userAccount.setNick(nickName);
        return updateByOpenId(userAccount);
    }
}
