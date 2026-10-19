package com.oimc.aimin.admin.model.request;

import com.oimc.aimin.admin.common.enums.PermissionTypeEnum;
import com.oimc.aimin.admin.model.request.groups.Save;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建权限参数")
public class PermissionRequest {


    @Schema(description = "id", example = "1")
    private Integer id;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空", groups = { Save.class })
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    @Schema(description = "权限名称", example = "用户管理")
    private String title;

    /**
     * 权限类型（1-目录，2-菜单，3-按钮）
     */
    @NotNull(message = "权限类型不能为空",groups = { Save.class })
    @Schema(description = "权限类型（1-目录，2-菜单，3-按钮）", example = "按钮")
    private PermissionTypeEnum permType;

    /**
     * 权限唯一标识（如:user:add）
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    @Schema(description = "权限唯一标识", example = "system:user:list")
    private String permKey;

    /**
     * 路由地址
     */
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    @Schema(description = "路由地址", example = "/system/user")
    private String path;

    /**
     * 路由名称
     */
    @Size(max = 50, message = "路由名称长度不能超过50个字符")
    @Schema(description = "路由名称", example = "SystemUser")
    private String routerName;

    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID，如果是顶级权限，则为0", example = "0")
    private Integer parentId ;

    /**
     * 图标
     */
    @Size(max = 50, message = "图标长度不能超过50个字符")
    @Schema(description = "图标", example = "User")
    private String icon;

    /**
     * 组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    /**
     * 重定向地址
     */
    @Size(max = 200, message = "重定向地址长度不能超过200个字符")
    @Schema(description = "重定向地址", example = "/system/user/list")
    private String redirect;

    /**
     * 是否隐藏（0-否，1-是）
     */
    @Schema(description = "是否隐藏", example = "false")
    private Boolean isHidden = false;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer sort = 0;
} 