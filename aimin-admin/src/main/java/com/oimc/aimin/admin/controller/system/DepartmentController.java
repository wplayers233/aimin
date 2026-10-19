package com.oimc.aimin.admin.controller.system;

import cn.hutool.core.lang.tree.Tree;
import com.oimc.aimin.admin.facade.DepartmentFacadeService;
import com.oimc.aimin.base.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "部门管理", description = "部门的查询和树形结构获取")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class DepartmentController {
    private final DepartmentFacadeService departmentFacadeService;
    
    /**
     * 获取部门树结构
     * 
     * @param id 部门ID，若为空则获取全部部门树
     * @return 部门树结构列表
     */
    @Operation(summary = "获取部门树", description = "获取部门的树形结构，可指定根节点ID")
    @GetMapping("/tree")
    public Result<List<Tree<Integer>>> tree(Integer id){
        List<Tree<Integer>> tree = departmentFacadeService.getDeptTree(id);
        return Result.success(tree);
    }
}
