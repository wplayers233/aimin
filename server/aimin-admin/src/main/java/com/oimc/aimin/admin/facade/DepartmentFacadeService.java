package com.oimc.aimin.admin.facade;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.oimc.aimin.admin.model.entity.Department;
import com.oimc.aimin.admin.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门管理门面服务
 * 处理部门相关的业务逻辑，统一缓存管理
 */
@Service
@RequiredArgsConstructor
public class DepartmentFacadeService {

    private final DepartmentService departmentService;

    /**
     * 获取部门树结构
     *
     * @param id 部门ID，如果为null则获取全部部门树
     * @return 部门树结构列表
     */
    public List<Tree<Integer>> getDeptTree(Integer id) {
        if (null == id){
            id = 0;
        }
        List<Department> list = departmentService.getAll();
        return buildTree(list, id);
    }

    private List<Tree<Integer>> buildTree(List<Department> list, Integer id) {
        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 都有默认值的
        treeNodeConfig.setWeightKey("sort");
        // 最大递归深度
        treeNodeConfig.setDeep(5);

        return TreeUtil.build(list, id,treeNodeConfig, (node, tree) -> {
            tree.setId(node.getId());
            tree.setParentId(node.getParentId());
            tree.setWeight(node.getSort());
            tree.setName(node.getName());
        });
    }

} 