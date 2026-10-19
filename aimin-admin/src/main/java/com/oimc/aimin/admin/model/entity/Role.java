package com.oimc.aimin.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.github.yulichang.annotation.FieldMapping;
import lombok.Data;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Data
@TableName("t_role")
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 角色名称（唯一）
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色编码（唯一）
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    @TableField("role_desc")
    private String roleDesc;

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
    @FieldMapping(tag = RolePermission.class, thisField = "id", joinField = "roleId", select = "permId")
    private List<Integer> permissionIds;

    @TableField(exist = false)
    private List<Permission> permissions;


    public Role() {

    }
}