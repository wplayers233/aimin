package com.oimc.aimin.admin.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import com.oimc.aimin.admin.common.constant.UiConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnableStatusEnum implements IEnum<Integer> {

    /**
     * 启用
     */
    ENABLE(1, "启用", UiConstants.COLOR_SUCCESS),

    /**
     * 禁用
     */
    DISABLE(0, "禁用", UiConstants.COLOR_ERROR);

    private final Integer code;

    @JsonValue
    private final String name;
    private final String color;

    @Override
    public Integer getValue() {
        return this.code;
    }
    public static EnableStatusEnum getEnum(String value){
        for (EnableStatusEnum obj:EnableStatusEnum.values()
        ) {
            if (obj.name.equals(value)){
                return obj;
            }
        }
        return null;
    }
    //重载以兼容两个参数，传code或者value都可
    public static EnableStatusEnum getEnum(Integer code){
        for (EnableStatusEnum obj:EnableStatusEnum.values()
        ) {
            if (obj.code.equals(code)){
                return obj;
            }
        }
        return null;
    }

}
