package com.oimc.aimin.admin.controller.system;

import com.oimc.aimin.admin.model.request.RoleRequest;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.facade.RoleFacadeService;
import com.oimc.aimin.admin.service.RoleService;
import com.oimc.aimin.base.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 角色管理
 * 提供角色的增删改查及权限分配功能
 */
@Tag(name = "角色管理", description = "角色的增删改查及权限分配")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class RoleController {

    private final RoleFacadeService roleFacadeService;

    private final RoleService roleService;

    /**
     * 创建角色
     *
     * @param req 创建角色请求
     * @return 新创建的角色ID
     */
    @Operation(summary = "添加角色", description = "添加新角色，返回新角色的ID")
    @PostMapping
    public Result<?> add(@Valid @RequestBody RoleRequest req) {
        Integer roleId = roleFacadeService.createRole(req);
        return Result.success(roleId);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色", description = "根据ID删除角色，同时会删除角色与权限的关联关系")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") @NotNull Integer id) {
        roleFacadeService.deleteRole(id);
        return Result.success();
    }

    /**
     * 更新角色
     *
     * @param request 更新角色请求
     * @return 操作结果
     */
    @Operation(summary = "更新角色", description = "更新角色信息，包括角色名称、编码、描述和状态")
    @PutMapping
    public Result<?> update(@Validated @RequestBody RoleRequest request) {
        roleFacadeService.updateRole(request);
        return Result.success();
    }

    /**
     * 获取所有角色列表
     *
     * @return 系统中的所有角色列表
     */
    @Operation(summary = "获取所有角色", description = "获取系统中所有角色的完整列表，不包含角色关联的权限信息")
    @GetMapping
    public Result<?> list() {
        Collection<Role> all = roleService.getAll();
        return Result.success(all);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "分配权限", description = "为指定角色分配一组权限，会替换原有权限设置")
    @PutMapping("/{roleId}/permissions")
    public Result<?> assignPermissions(@PathVariable("roleId") @NotNull Integer roleId, @RequestBody @NotEmpty List<Integer> permissionIds) {
        roleFacadeService.assignPermissions(roleId, permissionIds);
        return Result.success();
    }

    /**
     * 获取管理员的角色列表
     *
     * @param aid 管理员ID
     * @return 角色列表
     */
    @Operation(summary = "获取管理员角色", description = "获取指定管理员拥有的所有角色")
    @GetMapping("/admin/{aid}")
    public Result<?> getAdminRoles(@PathVariable("aid") @NotNull Integer aid) {
        List<Role> roleList = roleFacadeService.getAdminRoles(aid);
        return Result.success(roleList);
    }
}