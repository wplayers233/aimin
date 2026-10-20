package com.oimc.aimin.admin.model.vo;


import lombok.Data;

import java.io.Serial;

/*
 *
 * @author 渣哥
 */
@Data
public class DepartmentVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */

    private Integer id;

    /**
     * 名称
     */

    private String name;

    /**
     * 上级部门ID
     */

    private Integer parentId;

    /**
     * 祖级列表
     */

    private String ancestors;

    /**
     * 描述
     */

    private String description;

    /**
     * 排序
     */

    private Integer sort;

    /**
     * 状态（1：启用；2：禁用）
     */

    private Byte status;

    /**
     * 是否为系统内置数据
     */

    private Boolean isSystem;







}
