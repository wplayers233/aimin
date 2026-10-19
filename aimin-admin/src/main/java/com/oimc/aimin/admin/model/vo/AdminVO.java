package com.oimc.aimin.admin.model.vo;

import com.oimc.aimin.admin.model.entity.Department;
import com.oimc.aimin.admin.model.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminVO {
    private int id;
    private String username;
    private String phone;
    private String nickname;
    private Boolean status;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private boolean gender;
    private List<RoleVO> roles;
    private DepartmentVO department;
}
