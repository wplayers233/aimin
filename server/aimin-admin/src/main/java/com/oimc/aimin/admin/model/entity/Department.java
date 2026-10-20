package com.oimc.aimin.admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author root
 * @since 2025/03/14
 */
@Data
@TableName("t_department")
public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 上级部门ID
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 祖级列表
     */
    @TableField("ancestors")
    private String ancestors;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态（1：启用；2：禁用）
     */
    @TableField("status")
    private Byte status;

    /**
     * 是否为系统内置数据
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 创建人
     */
    @TableField("create_user")
    private Integer createUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField("update_user")
    private Integer updateUser;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    private Boolean deleted;
}