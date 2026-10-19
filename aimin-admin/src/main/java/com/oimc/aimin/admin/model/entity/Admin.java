package com.oimc.aimin.admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.github.yulichang.annotation.EntityMapping;
import com.github.yulichang.annotation.FieldMapping;
import lombok.Data;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @author root
 * @since 2025/02/26
 */
@Data
@TableName("t_admin")
public class Admin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名（唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 加密后的密码（BCrypt）
     */
    @TableField("password")
    private String password;

    /**
     * 性别
     */
    @TableField("gender")
    private boolean gender;

    /**
     * 手机号（唯一）
     */
    @TableField("phone")
    private String phone;

    /**
     * 账号状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Boolean status;

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

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Integer deptId;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 部门信息
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 权限列表
     */
    @TableField(exist = false)
    private List<Permission> permissions;



    @TableField(exist = false)
    @FieldMapping(tag = AdminRole.class, thisField = "id", joinField = "adminId", select = "roleId")
    private List<Integer> roleIds;

    /**
     * 部门信息
     */
    @TableField(exist = false)
    @EntityMapping(thisField = "deptId", joinField = "id")
    private Department department;

    @TableField(exist = false)
    private List<Role> roles;
}