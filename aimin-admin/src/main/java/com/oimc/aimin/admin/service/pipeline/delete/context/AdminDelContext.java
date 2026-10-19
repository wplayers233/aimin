package com.oimc.aimin.admin.service.pipeline.delete.context;

import com.oimc.aimin.admin.model.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/*
 *
 * @author 渣哥
 */
@Data
@AllArgsConstructor
public class AdminDelContext {
    private Set<Integer> adminIds;
    private List<Admin> adminList;

    public AdminDelContext(Set<Integer> adminIds){
        this.adminIds = adminIds;
        this.adminList = new ArrayList<>();
    }

}
