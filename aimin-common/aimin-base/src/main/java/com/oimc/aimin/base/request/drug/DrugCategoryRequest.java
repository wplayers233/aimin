package com.oimc.aimin.base.request.drug;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/*
 *
 * @author 渣哥
 */
@Data
public class DrugCategoryRequest {

    private Integer categoryId;

    /**
     * 分类名称，例如“抗生素”、“维生素”
     */
    private String name;

    /**
     * 父级分类ID，用于支持多级分类
     */
    private Integer parentId;

    /**
     * 分类描述，详细说明该分类的用途或特点
     */
    private String description;

    /**
     * 分类创建时间，默认当前时间
     */
    private LocalDateTime createAt;

    /**
     * 分类更新时间，自动更新为当前时间
     */
    private LocalDateTime updateAt;
}
