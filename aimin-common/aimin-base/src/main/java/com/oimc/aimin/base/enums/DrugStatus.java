package com.oimc.aimin.base.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DrugStatus implements IEnum<Integer> {
    /**
     * 启用
     */
    ENABLE(1, "启用"),

    /**
     * 禁用
     */
    DISABLE(0, "禁用");

    private final Integer code;

    @JsonValue
    private final String name;


    @Override
    public Integer getValue() {
        return this.code;
    }
    public static DrugStatus getEnum(String value){
        for (DrugStatus obj:DrugStatus.values()
        ) {
            if (obj.name.equals(value)){
                return obj;
            }
        }
        return null;
    }
    //重载以兼容两个参数，传code或者value都可
    public static DrugStatus getEnum(Integer code){
        for (DrugStatus obj:DrugStatus.values()
        ) {
            if (obj.code.equals(code)){
                return obj;
            }
        }
        return null;
    }
}
