package com.oimc.aimin.admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 用户部门关联表
 * </p>
 *
 * @author root
 * @since 2025/02/27
 */
@Data
@TableName("t_admin_department")
public class AdminDepartment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField("admin_id")
    private Integer adminId;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Integer deptId;

    /**
     * 是否主部门（0-否，1-是）
     */
    @TableField("is_primary")
    private Byte isPrimary;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableLogic
    private Boolean deleted;
}