package com.oimc.aimin.admin.facade;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.oimc.aimin.admin.common.constant.PermissionConstants;
import com.oimc.aimin.admin.common.constant.SysConstants;
import com.oimc.aimin.admin.common.enums.EnableStatusEnum;
import com.oimc.aimin.admin.model.entity.Admin;
import com.oimc.aimin.admin.model.entity.Permission;
import com.oimc.aimin.admin.model.entity.Role;
import com.oimc.aimin.admin.model.entity.RolePermission;
import com.oimc.aimin.admin.model.request.PermissionRequest;
import com.oimc.aimin.admin.model.vo.RouterVO;
import com.oimc.aimin.admin.service.AdminService;
import com.oimc.aimin.admin.service.PermissionService;
import com.oimc.aimin.admin.service.RolePermissionService;
import com.oimc.aimin.admin.service.RoleService;
import com.oimc.aimin.admin.utils.ObjectConvertor;
import com.oimc.aimin.base.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理门面服务
 * 处理权限/菜单相关的业务逻辑，统一缓存管理
 */
@Service
@RequiredArgsConstructor
public class PermissionFacadeService {

    private final RoleService roleService;

    private final PermissionService permissionService;

    private final RolePermissionService rolePermissionService;

    private final AdminService adminService;

    private final ObjectConvertor objectConvertor;


    public List<RouterVO> getRoutersByAdminId(Integer aid) {
        List<Permission> permissions = getPermissionByAdminId(aid);
        return buildRouters(permissions);
    }

    /**
     * 获取菜单树形结构
     * 
     * @return 菜单树
     */
    public List<Tree<Integer>> getMenuTree() {
        List<Permission> permissions = permissionService.getAll();
        List<Permission> menu = permissions.stream()
                .filter(p -> Objects.equals(p.getPermType().getValue(), PermissionConstants.CATALOGUE) ||
                        Objects.equals(p.getPermType().getValue(), PermissionConstants.MENU))
                .sorted(Comparator.comparingInt(Permission::getSort))
                .toList();
        return buildMenuTree(menu);
    }

    /**
     * 获取权限树形结构
     *
     * @return 权限树
     */
    public List<Tree<Integer>> getTree() {
        List<Permission> permissions = permissionService.getAll();
        return buildTree(permissions);
    }

    /**
     * 删除权限
     * 
     * @param id 权限ID
     */
    @Transactional
    public void deletePermission(Integer id) {
        boolean exists = permissionService.isExists(id);
        if (!exists) {
            throw new RuntimeException("权限不存在");
        }
        List<Permission> children = permissionService.getChildren(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子级权限，无法删除");
        }
        List<RolePermission> rolePermissions = rolePermissionService.getByPermId(id);
        if (!rolePermissions.isEmpty()) {
            throw new RuntimeException("存在关联角色，无法删除");
        }
        permissionService.delete(id);
    }


    /**
     * 创建或更新权限
     * 
     * @param req 权限请求对象
     * @return 权限ID
     */
    @Transactional
    public Integer updatePermission(PermissionRequest req) {
        if(req.getId() != null){
            Permission permission = objectConvertor.toPermission(req);
            String[] split = permission.getPermKey().split(":");
            StringBuilder name = new StringBuilder();
            for(String s : split){
                if(StringUtils.isNotBlank(s)){
                    name.append(StringUtils.capitalize(s));
                }
            }
            permission.setName(name.toString());
            permissionService.update(permission);
            return permission.getId();
        }
        throw new BusinessException("权限ID不能为空");
    }

    public Integer createPermission(PermissionRequest req) {
        boolean existsByPermKey = permissionService.isExistsByPermKey(req.getPermKey());
        if(existsByPermKey){
            throw new BusinessException("权限标识已存在");
        }
        Integer parentId = req.getParentId();
        if(null != parentId){
            boolean parentExists = permissionService.isExists(req.getParentId());
            if(!parentExists){
                throw new BusinessException("父权限不存在");
            }
        }

        Permission permission = objectConvertor.toPermission(req);
        permission.setParentId(req.getParentId() != null ? req.getParentId() : 0);
        permission.setStatus(EnableStatusEnum.ENABLE);
        String[] split = req.getPermKey().split(":");
        StringBuilder name = new StringBuilder();
        for(String s : split){
            if(StringUtils.isNotBlank(s)){
                name.append(StringUtils.capitalize(s));
            }
        }
        permission.setName(name.toString());
        permissionService.insert(permission);
        return permission.getId();
    }



    public Set<String> getPermCodeByAdminId(Integer adminId){
        List<Permission> permissionList = getPermissionByAdminId(adminId);
        return permissionList.stream()
                .map(Permission::getPermKey)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    private List<Permission> getPermissionByAdminId(Integer adminId){
        Admin admin = adminService.deepGetById(adminId);
        List<Integer> roleIds = admin.getRoleIds();
        List<Role> roles = roleService.deepGetByIds(roleIds);
        Set<Integer> set = new HashSet<>();
        for(Role role : roles){
            List<Integer> permissionIds = role.getPermissionIds();
            set.addAll(permissionIds);
        }
        return permissionService.getByIds(set);
    }



    private List<Tree<Integer>> buildTree(List<Permission> permissions){
        TreeNodeConfig treeNodeConfig = buildTreeNodeConfig();

        return TreeUtil.build(permissions, 0, treeNodeConfig, (permission, tree) -> {
            tree.setId(permission.getId());
            tree.setParentId(permission.getParentId() != null ? permission.getParentId() : 0);
            tree.setWeight(permission.getSort());
            tree.setName(permission.getTitle());

            // 添加额外属性
            tree.putExtra("permKey", permission.getPermKey());
            tree.putExtra("type", permission.getPermType());
        });

    }

    private List<Tree<Integer>> buildMenuTree(List<Permission> menu){
        TreeNodeConfig treeNodeConfig = buildTreeNodeConfig();

        // 构建菜单树
        return TreeUtil.build(menu, 0, treeNodeConfig, (permission, tree) -> {
            tree.setId(permission.getId());
            tree.setParentId(permission.getParentId() != null ? permission.getParentId() : 0);
            tree.setWeight(permission.getSort());
            tree.setName(permission.getTitle());
            tree.putExtra("path", permission.getPath());
            tree.putExtra("component", permission.getComponent());
            tree.putExtra("icon", permission.getIcon());
            tree.putExtra("hidden", permission.getHidden());
        });
    }

    private TreeNodeConfig buildTreeNodeConfig() {
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        treeNodeConfig.setWeightKey("sort");
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setDeep(3);
        return treeNodeConfig;
    }
    /**
     * 根据角色ID获取权限列表
     * @param roleId
     * @return
     */
    public List<Tree<Integer>> getPermissionTreeWithRole(Integer roleId) {
        Role role = roleService.deepGetById(roleId);
        List<Integer> ids = role.getPermissionIds();
        List<Permission> all = permissionService.getAll();
        List<Permission> permissionsWithRole = permissionService.getByIds(ids);
        return buildPermissionTreeWithRole(all,permissionsWithRole,0,roleId);
    }

    public List<Tree<Integer>> buildPermissionTreeWithRole(List<Permission> allPermissions, List<Permission> permissionsByRoleId, Integer pid,Integer roleId) {
        Set<Integer> checkedIds = permissionsByRoleId.stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
        // 配置树节点
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 设置排序字段
        treeNodeConfig.setWeightKey("sort");
        // 设置ID字段
        treeNodeConfig.setIdKey("id");
        // 最大递归深度
        treeNodeConfig.setDeep(3);

        return TreeUtil.build(allPermissions, pid, treeNodeConfig, (permission, tree) -> {
            tree.setId(permission.getId());
            tree.setParentId(permission.getParentId() != null ? permission.getParentId() : 0);
            tree.setWeight(permission.getSort());
            tree.setName(permission.getTitle());

            // 添加额外属性
            tree.putExtra("permKey", permission.getPermKey());
            tree.putExtra("type", permission.getPermType());
            // 添加checked属性
            if(SysConstants.SUPER_ROLE_ID.equals(roleId)){
                tree.putExtra("checked",true);
            }else{
                tree.putExtra("checked", checkedIds.contains(permission.getId()));
            }
        });
    }

    private List<RouterVO> buildRouters(List<Permission> allPermissions) {
        // 过滤出类型为目录(1)或菜单(2)的权限，并按照排序字段排序
        List<Permission> menuPermissions = allPermissions.stream()
                .filter(p -> Objects.equals(p.getPermType().getValue(), PermissionConstants.CATALOGUE) ||
                        Objects.equals(p.getPermType().getValue(), PermissionConstants.MENU) &&
                                p.getStatus() == EnableStatusEnum.ENABLE)  // 合并过滤条件
                .sorted(Comparator.comparingInt(Permission::getSort))
                .collect(Collectors.toList());

        // 构建权限树
        return buildRouterTree(menuPermissions, 0);
    }

    private List<RouterVO> buildRouterTree(List<Permission> permissions, Integer parentId) {
        List<RouterVO> routers = new ArrayList<>();
        // 获取指定父ID的所有权限
        List<Permission> children = permissions.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .toList();

        if (!children.isEmpty()) {
            for (Permission permission : children) {
                RouterVO router = objectConvertor.toRouterVO(permission);
                // 递归获取子路由
                List<RouterVO> childRouters = buildRouterTree(permissions, permission.getId());
                if (!childRouters.isEmpty()) {
                    router.setChildren(childRouters);
                }
                routers.add(router);
            }
        }
        return routers;
    }
}