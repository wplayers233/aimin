package com.oimc.aimin.admin.model.vo;

import com.oimc.aimin.admin.common.enums.PermissionTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class PermissionVO {

    private Integer id;

    private PermissionTypeEnum permType;

    private String title;

    private String permKey;

    private Boolean checked;

    private Integer parentId;

    private Integer sort;

    private List<PermissionVO> children;

}
