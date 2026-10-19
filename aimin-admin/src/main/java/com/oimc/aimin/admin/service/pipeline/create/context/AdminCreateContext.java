package com.oimc.aimin.admin.service.pipeline.create.context;

import com.oimc.aimin.admin.model.request.AdminRequest;
import lombok.Data;

import java.util.List;


@Data
public class AdminCreateContext {
    private String username;
    private String phone;
    private String password;
    private String encryptedPassword;
    private String nickname;
    private boolean gender;
    private Integer deptId;
    private Boolean status;
    private List<Integer> roleIds;

    /**
     * 新增成功后由 SaveUserHandler 回填，供后续保存角色关系使用
     */
    private Integer adminId;

    // createAdminModel 入参的构造函数
    public AdminCreateContext(AdminRequest model) {
        this.username = model.getUsername();
        this.phone = model.getPhone();
        this.password = model.getPassword();
        this.nickname = model.getNickname();
        this.gender = model.isGender();
        this.deptId = model.getDeptId();
        this.status = model.getStatus();
        this.roleIds = model.getRoleIds();
    }

}
