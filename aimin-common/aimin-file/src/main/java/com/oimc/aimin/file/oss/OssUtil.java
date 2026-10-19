package com.oimc.aimin.file.oss;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * 文件工具类
 * @author 渣哥
 */
@Slf4j
@Setter
public class OssUtil {

    /**
     * 生成OSS存储路径
     *
     * @param moduleName 模块名
     * @param file 文件
     * @return OSS存储路径
     */
    public static String generateOssPath(String moduleName, MultipartFile file) {

        // 生成日期路径, 按照年月日组织
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");

        // 组合完整路径: 模块名/年/月/日/uuid.扩展名
        return StrUtil.format("{}/{}", moduleName, datePath);
    }

    public static String generateName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extName = FileUtil.extName(originalFilename);

        return IdUtil.fastSimpleUUID() + "." + extName;
    }
    
    /**
     * 获取文件大小，单位KB
     *
     * @param file 文件
     * @return 文件大小
     */
    public static Integer getFileSizeInKb(MultipartFile file) {
        long sizeInBytes = file.getSize();
        return (int) (sizeInBytes / 1024);
    }
    
    /**
     * 获取文件扩展名
     *
     * @param file 文件
     * @return 文件扩展名
     */
    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return cn.hutool.core.io.FileUtil.extName(originalFilename);
    }
    
    /**
     * 获取文件名称（不包含路径和扩展名）
     *
     * @param file 文件
     * @return 文件名称
     */
    public static String getFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            return IdUtil.fastSimpleUUID();
        }
        return cn.hutool.core.io.FileUtil.mainName(originalFilename);
    }
    
    /**
     * 检查文件类型是否为图片
     *
     * @param file 文件
     * @return 是否为图片
     */
    public static boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }


} 