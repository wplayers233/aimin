package com.oimc.aimin.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传结果
 * @author 渣哥
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 现在文件名
     */
    private String currentName;

    /**
     * 原来文件名
     */
    private String originalName;
    
    /**
     * 文件路径
     */
    private String path;
    
    /**
     * 文件大小（KB）
     */
    private Integer fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;


} 