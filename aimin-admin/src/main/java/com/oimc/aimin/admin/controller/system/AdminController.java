package com.oimc.aimin.admin.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.oimc.aimin.admin.facade.AdminFacadeService;
import com.oimc.aimin.admin.service.pipeline.create.context.AdminCreateContext;
import com.oimc.aimin.admin.model.request.AdminRequest;
import com.oimc.aimin.admin.model.request.PageRequest;
import com.oimc.aimin.admin.model.vo.AdminVO;
import com.oimc.aimin.admin.service.pipeline.delete.context.AdminDelContext;
import com.oimc.aimin.base.response.PageResp;
import com.oimc.aimin.base.response.Result;
import com.oimc.aimin.satoken.admin.StpAdminUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 管理员控制器
 * 提供管理员的增删改查等操作
 */
@Tag(name = "管理员管理", description = "管理员的创建、查询、更新和删除")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/admin")
public class AdminController {

    private final AdminFacadeService adminFacadeService;


    /**
     * 创建管理员
     * 创建一个新的管理员账号，包括基本信息和角色分配
     *
     * @param createRequest 创建管理员的请求对象，包含用户名、密码等信息
     * @return 操作结果
     */
    @Operation(summary = "创建管理员", description = "创建一个新的管理员账号，包括设置基本信息和分配角色权限")
    @SaCheckPermission(value = "system:user:add", type = "ADMIN")
    @PostMapping
    public Result<?> createAdmin(@RequestBody @Valid AdminRequest createRequest){
        adminFacadeService.add(new AdminCreateContext(createRequest));
        return Result.success();
    }

    /**
     * 批量删除管理员
     * 根据ID集合批量删除管理员账号
     *
     * @param adminIds 管理员ID集合
     * @return 操作结果
     */
    @Operation(summary = "批量删除管理员", description = "根据ID集合批量删除管理员，同时移除关联的角色分配")
    @SaCheckPermission(value = "system:user:delete", type = "ADMIN")
    @DeleteMapping
    public Result<?> deleteAdmins(@RequestBody @NotEmpty Set<Integer> adminIds){
        adminFacadeService.delete(new AdminDelContext(adminIds));
        return Result.success();
    }

    /**
     * 更新管理员信息
     * 更新管理员的基本信息或角色分配
     *
     * @param request 更新管理员的请求对象，包含ID和需要更新的字段
     * @return 操作结果
     */
    @Operation(summary = "更新管理员", description = "更新管理员信息，可包括基本资料修改和角色重新分配")
    @SaCheckPermission(value = "system:user:edit", type = "ADMIN")
    @PutMapping
    public Result<?> updateAdmin(@RequestBody @Valid AdminRequest request) {
        adminFacadeService.updateAdminInfo(request);
        return Result.success();
    }

    /**
     * 搜索管理员
     * 根据条件分页查询管理员列表
     *
     * @param searchRequest 搜索条件，包含关键字和分页信息
     * @return 分页的管理员列表
     */
    @Operation(summary = "搜索管理员", description = "根据条件分页查询管理员列表，支持关键字搜索和分页")
    @SaCheckPermission(value = "system:user:list", type = "ADMIN")
    @PostMapping("/search")
    public Result<?> searchAdmins(@RequestBody @Valid PageRequest searchRequest) {
        PageResp<AdminVO> adminVOPageResp = adminFacadeService.pageSearchFuzzy(searchRequest);
        return Result.success(adminVOPageResp);
    }

    /**
     *
     * @return
     */
    @Operation(summary = "获取当前管理员详情", description = "获取当前管理员的详细信息，包括基本资料和已分配角色")
    @SaCheckPermission(value = "system:user:info", type = "ADMIN")
    @GetMapping
    public Result<?> getCurrentAdminDetail() {
        Integer aid = StpAdminUtil.getLoginId();
        AdminVO admin = adminFacadeService.getAllInfoByAdminId(aid);
        return Result.success(admin);
    }

    /**
     *
     * @return
     */
    @Operation(summary = "根据管理员id获取详情", description = "根据ID获取单个管理员的详细信息，包括基本资料和已分配角色")
    @SaCheckPermission(value = "system:user:info", type = "ADMIN")
    @GetMapping("/{id}")
    public Result<?> getAdminDetailById(@PathVariable("id") @NotNull Integer id) {
        AdminVO admin = adminFacadeService.getAllInfoByAdminId(id);
        return Result.success(admin);
    }

}