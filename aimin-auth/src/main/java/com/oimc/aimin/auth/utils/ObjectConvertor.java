package com.oimc.aimin.auth.utils;


import cn.dev33.satoken.stp.SaTokenInfo;
import com.oimc.aimin.auth.model.vo.MiniProgramLoginVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObjectConvertor {

    MiniProgramLoginVO toMiniProgramLoginVO(SaTokenInfo saTokenInfo);
}
