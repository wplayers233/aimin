package com.oimc.aimin.auth.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author root
 * @since 2024/12/23
 */
@Data
@TableName("t_user_account")
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("openid")
    private String openid;
    @TableField("unionid")
    private String unionid;
    @TableField("nick")
    private String nick;
    @TableField("phone")
    private String phone;
    @TableField("avatar")
    private String avatar;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}