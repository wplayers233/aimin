package com.oimc.aimin.admin.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oimc.aimin.admin.model.entity.Admin;
import com.oimc.aimin.admin.model.entity.Department;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.request.AdminRequest;
import com.oimc.aimin.admin.model.request.PageRequest;
import com.oimc.aimin.admin.model.vo.AdminVO;
import com.oimc.aimin.admin.service.AdminRoleService;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.DepartmentService;
import com.oimc.aimin.admin.service.RoleService;
import com.oimc.aimin.admin.service.pipeline.create.AdminCreateHandler;
import com.oimc.aimin.admin.service.pipeline.create.context.AdminCreateContext;
import com.oimc.aimin.admin.service.pipeline.delete.AdminDeleteHandler;
import com.oimc.aimin.admin.service.pipeline.delete.context.AdminDelContext;
import com.oimc.aimin.admin.utils.ObjectConvertor;
import com.oimc.aimin.admin.utils.PwdUtils;
import com.oimc.aimin.base.exception.BusinessException;
import com.oimc.aimin.base.response.PageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 *
 * @author 渣哥
 */
@Service
@RequiredArgsConstructor
public class AdminFacadeService {

    private final AdminService adminService;

    private final AdminRoleService adminRoleService;

    private final DepartmentService departmentService;

    private final RoleService roleService;
    
    private final ObjectConvertor objectConvertor;

    private final List<AdminCreateHandler> createHandlers;

    private final List<AdminDeleteHandler> deleteHandlers;

    private final com.oimc.aimin.admin.service.AdminAuditService auditService;

    /**
     * 新增管理员
     * 通过责任链模式处理创建管理员的逻辑
     *
     * @param context 管理员创建上下文，包含创建管理员所需的全部信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AdminCreateContext context) {
        if (context.getPassword() == null || context.getPassword().isBlank()) {
            throw BusinessException.of("密码不能为空");
        }
        try {
            createHandlers.forEach(handler -> handler.handle(context));
            // 创建时一并保存角色关系：原实现丢了 roleIds，新建管理员只能事后在别处补角色
            if (context.getAdminId() != null && context.getRoleIds() != null) {
                adminService.updateRelationRoles(context.getAdminId(), context.getRoleIds());
            }
            auditService.audit("admin-console", "ADMIN_CREATE", "admin:" + context.getAdminId(), context.getUsername());
        } catch (DuplicateKeyException e) {
            // 并发下 isExists 先查后写会漏，靠 t_admin.uk_username/uk_phone 兜底
            throw BusinessException.of("用户名或手机号已存在");
        }
    }

    /**
     * 删除管理员
     * 通过责任链模式处理删除管理员的逻辑，包括删除关联的角色关系
     *
     * @param context 管理员删除上下文，包含需要删除的管理员ID等信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(AdminDelContext context) {
        deleteHandlers.forEach(handler -> handler.handle(context));
        auditService.audit("admin-console", "ADMIN_DELETE", "admin", "批量删除管理员");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAdminInfo(AdminRequest req){
        Integer adminId = req.getAid();
        Admin byId = adminService.getById(adminId);
        if (byId == null) {
            throw BusinessException.of("管理员不存在");
        }
        boolean unique = adminService.checkPhoneUsernameUnique(req);
        if(!unique){
            throw BusinessException.of("用户名或手机号已被占用");
        }
        Admin admin = objectConvertor.toAdmin(req);
        // toAdmin 不映射主键，必须显式回填，否则 updateById(null) 静默影响 0 行
        admin.setId(adminId);
        // 密码只在显式传入时更新；不传就保持原值，避免明文或空串覆盖
        admin.setPassword(null);
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            admin.setPassword(PwdUtils.bcryptPasswordEncoder(req.getPassword()));
        }
        try {
            adminService.update(admin);
            // 传 null 表示不调整角色；传空数组表示清空角色
            if (req.getRoleIds() != null) {
                adminService.updateRelationRoles(adminId, req.getRoleIds());
            }
            auditService.audit("admin-console", "ADMIN_UPDATE", "admin:" + adminId, req.getUsername());
        } catch (DuplicateKeyException e) {
            throw BusinessException.of("用户名或手机号已存在");
        }
    }

    /**
     *
     * @param req SearchReq
     * @return PageResp<AdminVO>
     */
    public PageResp<AdminVO> pageSearchFuzzy(PageRequest req){
        Set<Integer> childrenIds = extractChildrenIds(req.getDeptId());
        Page<Admin> adminPage = adminService.searchFuzzyWithDept(req.getContent(), childrenIds,req.getCurrentPage(), req.getPageSize());
        return PageResp.build(adminPage, AdminVO.class);
    }

    private Set<Integer> extractChildrenIds(Integer deptId) {
        Set<Integer> idSet = new HashSet<>();
        if (deptId == null) {
            return idSet;
        }
        
        // 添加当前部门ID
        idSet.add(deptId);
        
        // 获取所有部门列表
        List<Department> departments = departmentService.getAll();
        
        // 递归查找子部门
        findChildren(deptId, departments, idSet);
        
        return idSet;
    }


    /**
     *
     * @param id
     * @return
     */
    public AdminVO getAllInfoByAdminId(Integer id) {
        Admin admin = adminService.deepGetById(id);
        List<Role> roles = roleService.getByIds(admin.getRoleIds());
        admin.setRoles(roles);
        return objectConvertor.toAdminVO(admin);
    }

    /**
     * 递归查找所有子部门ID
     *
     * @param parentId 父部门ID
     * @param departments 所有部门列表
     * @param result 结果集合
     */
    private void findChildren(Integer parentId, List<Department> departments, Set<Integer> result) {
        for (Department dept : departments) {
            // 检查ancestors字段是否包含当前部门ID
            if (dept.getAncestors() != null && dept.getAncestors().contains(parentId.toString())) {
                // 通过检查是否部门的ancestors字段中包含父ID来判断是否是子部门
                String[] ancestors = dept.getAncestors().split(",");
                for (String ancestor : ancestors) {
                    if (parentId.equals(Integer.parseInt(ancestor))) {
                        result.add(dept.getId());
                        break;
                    }
                }
            }
        }
    }


}
