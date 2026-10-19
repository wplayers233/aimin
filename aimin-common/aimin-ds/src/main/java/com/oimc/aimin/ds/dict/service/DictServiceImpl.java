package com.oimc.aimin.ds.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oimc.aimin.ds.dict.entity.Dict;
import com.oimc.aimin.ds.dict.mapper.DictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统通用字典表 服务实现类
 * </p>
 *
 * @author root
 * @since 2024/12/30
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    private final DictMapper dictMapper;

    @Override
    public Dict getByTypeAndCode(String dictType, String value) {
        LambdaQueryWrapper<Dict> eq = new LambdaQueryWrapper<Dict>()
                .eq(Dict::getTypeCode, dictType)
                .eq(Dict::getDictCode, value);
        return dictMapper.selectOne(eq);
    }
}
