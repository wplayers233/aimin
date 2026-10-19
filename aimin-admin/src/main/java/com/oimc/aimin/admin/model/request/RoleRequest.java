package com.oimc.aimin.admin.model.request;

import com.oimc.aimin.admin.model.request.groups.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建角色参数")
public class RoleRequest {

    @NotBlank(message = "角色名称不能为空", groups = { Update.class })
    private Integer id;
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码", example = "admin")
    private String roleCode;

    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    @Schema(description = "角色描述", example = "系统管理员角色")
    private String roleDesc;
} 