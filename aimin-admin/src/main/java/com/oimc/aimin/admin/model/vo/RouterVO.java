package com.oimc.aimin.admin.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RouterVO {
    /**
     * 路由路径
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 路由名称
     */
    private String name;
    
    /**
     * 重定向地址
     */
    private String redirect;
    
    /**
     * 路由元数据
     */
    private Meta meta;

    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 子路由
     */
    private List<RouterVO> children;
    
    @Data
    public static class Meta {
        /**
         * 标题
         */
        private String title;
        
        /**
         * 图标
         */
        private String icon;
        
        /**
         * 是否在菜单中显示
         */
        private Boolean hidden;
        
        /**
         * 其他元数据
         */
        private Map<String, Object> extraProperties;
    }
}
