package com.oimc.aimin.auth.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.oimc.aimin.auth.model.vo.MiniProgramLoginVO;
import com.oimc.aimin.auth.exception.BuildURLException;
import com.oimc.aimin.auth.service.UserAccountService;
import com.oimc.aimin.auth.utils.ObjectConvertor;
import com.oimc.aimin.auth.service.WxService;
import com.oimc.aimin.base.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/wx")
@RequiredArgsConstructor
public class WxController {

    private final WxService wxService;
    private final ObjectConvertor objectConvertor;
    private final UserAccountService userAccountService;

    @GetMapping("/token")
    public Result<?> token(String code) throws BuildURLException {
        SaTokenInfo saTokenInfo = wxService.wxLogin(code);
        MiniProgramLoginVO vo = objectConvertor.toMiniProgramLoginVO(saTokenInfo);
        return Result.success(vo);
    }

    @GetMapping("/updateAvatar")
    public Result<?> updateAvatar(String avatarUrl) {
        userAccountService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @GetMapping("/updateNickName")
    public Result<?> updateNickName(String NickName) {
        userAccountService.updateNick(NickName);
        return Result.success();
    }

}
