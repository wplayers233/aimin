package com.oimc.aimin.admin.model.request;

import com.oimc.aimin.admin.model.request.groups.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


@Data
@Schema(description = "Admin参数")
public class AdminRequest {

    @Schema(description = "管理员ID")
    @NotNull(message = "管理员ID不能为空", groups = { Update.class })
    private Integer aid;

    // 用户名字段，必须非空且长度在3到50个字符之间
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名必须介于 3 到 50 个字符之间")
    @Schema(description = "用户名")
    private String username;

    // 密码字段：新增必填，更新时留空表示不修改；非空时长度 6-100
    @Pattern(regexp = "^$|^.{6,100}$", message = "密码长度需为 6 到 100 个字符")
    @Schema(description = "密码（新增必填；更新时留空表示不修改）")
    private String password;

    // 昵称字段，长度不得超过50个字符
    @Size(max = 20, message = "昵称最多为20个字符")
    @Schema(description = "昵称")
    private String nickname;

    // 性别字段
    @Schema(description = "性别")
    private boolean gender;

    // 手机号码字段，必须符合中国大陆手机号码格式
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号不符合规范")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "状态")
    private Boolean status;

    @Schema(description = "部门id")
    private Integer deptId;

    @Schema(description = "角色ID列表")
    private List<Integer> roleIds;

    @Schema(description = "描述信息")
    private String description;
}
