package com.oimc.aimin.admin.model.vo;

import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/*
 *
 * @author 渣哥
 */
@Data
public class RoleVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Integer id;

    /**
     * 角色名称（唯一）
     */
    private String roleName;

    /**
     * 角色编码（唯一）
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    private LocalDateTime updateAt;

}
