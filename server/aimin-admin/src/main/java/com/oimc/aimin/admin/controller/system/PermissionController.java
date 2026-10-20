package com.oimc.aimin.admin.controller.system;

import cn.hutool.core.lang.tree.Tree;
import com.oimc.aimin.admin.model.entity.Permission;
import com.oimc.aimin.admin.model.request.PermissionRequest;
import com.oimc.aimin.admin.facade.PermissionFacadeService;
import com.oimc.aimin.admin.service.PermissionService;
import com.oimc.aimin.base.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限/菜单管理
 * 权限包含目录、菜单和按钮三种类型
 * 其中目录和菜单类型共同构成前端可访问的页面结构
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
@Tag(name = "权限/菜单管理", description = "权限、菜单及按钮的增删改查接口")
public class PermissionController {

    private final PermissionFacadeService permissionFacadeService;

    private final PermissionService permissionService;

    /**
     * 获取菜单树形结构
     * @return 菜单树
     */
    @Operation(summary = "获取菜单树", description = "获取所有目录和菜单类型的权限，以树形结构展示")
    @GetMapping("/menu/tree")
    public Result<?> getMenuTree() {
        List<Tree<Integer>> menuTree = permissionFacadeService.getMenuTree();
        return Result.success(menuTree);
    }

    @Operation(summary = "获取权限列表", description = "根据角色ID获取权限树")
    @GetMapping("/tree/{roleId}")
    public Result<?> getPermissionTreeWithRole(@PathVariable("roleId") Integer roleId){
        List<Tree<Integer>> permissionTreeWithRole = permissionFacadeService.getPermissionTreeWithRole(roleId);
        return Result.success(permissionTreeWithRole);
    }

    /**
     * 根据ID删除权限
     * @param id 权限ID
     * @return 操作结果
     */
    @Operation(summary = "删除权限", description = "根据ID删除权限/菜单/按钮，会同时删除与角色的关联关系")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable("id") Integer id) {
        permissionFacadeService.deletePermission(id);
        return Result.success();
    }

    /**
     * 更新权限
     * @param req 权限创建请求
     * @return 新创建的权限ID
     */
    @Operation(summary = "更新权限", description = "更新权限信息，如ID不存在则创建新权限")
    @PutMapping
    public Result<?> update(@RequestBody @Valid PermissionRequest req) {
        Integer permissionId = permissionFacadeService.updatePermission(req);
        return Result.success(permissionId);
    }

    /**
     * 创建权限
     * @param req 权限创建请求
     * @return 新创建的权限ID
     */
    @Operation(summary = "创建权限", description = "创建权限信息")
    @PostMapping
    public Result<?> create(@RequestBody @Valid PermissionRequest req) {
        Integer permissionId = permissionFacadeService.createPermission(req);
        return Result.success(permissionId);
    }

    /**
     * 获取指定父级下的权限列表
     * @param pid 父权限ID
     * @return 权限列表
     */
    @Operation(summary = "获取子权限列表", description = "获取指定父ID下的所有直接子级权限")
    @GetMapping("/parent/{pid}")
    public Result<?> getChildren(@PathVariable("pid") Integer pid) {
        List<Permission> list = permissionService.getListByPid(pid);
        return Result.success(list);
    }
}
