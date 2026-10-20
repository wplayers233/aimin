package com.oimc.aimin.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.github.yulichang.annotation.EntityMapping;
import com.oimc.aimin.admin.common.enums.EnableStatusEnum;
import com.oimc.aimin.admin.common.enums.PermissionTypeEnum;
import lombok.Data;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Data
@TableName("t_permission")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 权限名称
     */
    @TableField("title")
    private String title;

    /**
     * 权限类型（1-目录，2-菜单，3-按钮）
     */
    @TableField("perm_type")
    private PermissionTypeEnum permType;

    /**
     * 权限唯一标识（如:user:add）
     */
    @TableField("perm_key")
    private String permKey;

    /**
     * 路由地址
     */
    @TableField("path")
    private String path;

    /**
     * 路由名称
     */
    @TableField("name")
    private String name;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 重定向地址
     */
    @TableField("redirect")
    private String redirect;

    /**
     * 是否隐藏（0-否，1-是）
     */
    @TableField("hidden")
    private Boolean hidden;


    /**
     * 显示顺序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private EnableStatusEnum status;

    /**
     * 创建时间
     */
    @TableField("create_at")
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    @TableField("update_at")
    private LocalDateTime updateAt;

    @TableField(exist = false)
    private boolean hasChildren;


    @TableField(exist = false)
    @EntityMapping(thisField = "id", joinField = "parentId")
    private List<Permission> children;

}