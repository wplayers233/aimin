package com.oimc.aimin.ds.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oimc.aimin.ds.dict.entity.Dict;

/**
 * <p>
 * 系统通用字典表 服务类
 * </p>
 *
 * @author root
 * @since 2024/12/30
 */
public interface DictService extends IService<Dict> {

    Dict getByTypeAndCode(String dictType, String value);
}
