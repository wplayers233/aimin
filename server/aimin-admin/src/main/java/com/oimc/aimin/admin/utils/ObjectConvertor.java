package com.oimc.aimin.admin.utils;

import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.request.AdminRequest;
import com.oimc.aimin.admin.model.request.PermissionRequest;
import com.oimc.aimin.admin.model.request.RoleRequest;
import com.oimc.aimin.admin.model.vo.PermissionVO;
import com.oimc.aimin.admin.model.vo.RouterVO;
import com.oimc.aimin.admin.service.pipeline.create.context.AdminCreateContext;
import com.oimc.aimin.admin.model.entity.Permission;
import com.oimc.aimin.admin.model.vo.AdminVO;
import com.oimc.aimin.admin.model.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;

import java.util.List;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ObjectConvertor {

    AdminVO toAdminVO(Admin admin);

    @Mapping(source = "encryptedPassword", target = "password")
    Admin toAdmin(AdminCreateContext context);

    Admin toAdmin(AdminRequest req);

    Role toRole(RoleRequest req);

    Role toRole(AdminRequest req);

    Permission toPermission(PermissionRequest req);

    List<PermissionVO> toPermissionVO(List<Permission> permissions);

    PermissionVO toPermissionVO(Permission permission);

    @Mappings({
            @Mapping(source = "title", target = "meta.title"),
            @Mapping(source = "icon", target = "meta.icon"),
            @Mapping(source = "hidden", target = "meta.hidden"),
    })
    RouterVO toRouterVO(Permission permission);
}
