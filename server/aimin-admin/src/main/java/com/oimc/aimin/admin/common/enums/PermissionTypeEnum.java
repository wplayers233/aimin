package com.oimc.aimin.admin.common.enums;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionTypeEnum implements IEnum<Byte> {
    /**
     * 启用
     */
    CATALOGUE((byte) 1, "目录"),

    /**
     * 禁用
     */
    MENU((byte) 2, "菜单"),

    /**
     * 按钮
     */
    BUTTON((byte) 3, "按钮");

    private final Byte value;

    @JsonValue
    private final String desc;


    @Override
    public Byte getValue() {
        return this.value;
    }
}
