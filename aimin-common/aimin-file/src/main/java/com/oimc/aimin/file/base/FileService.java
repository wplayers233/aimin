package com.oimc.aimin.file.base;

import com.aliyuncs.exceptions.ClientException;
import com.oimc.aimin.base.response.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/*
 *
 * @author 渣哥
 */
public interface FileService {

    /**
     * 上传文件到OSS
     * @param path OSS中的文件路径
     * @param fileStream 文件输入流
     * @return 是否上传成功
     * @throws ClientException 客户端异常
     */
    boolean upload(String path, InputStream fileStream) throws ClientException;
    
    /**
     * 从OSS下载文件
     * @param path OSS中的文件路径
     * @return 文件输入流
     * @throws ClientException 客户端异常
     * @throws IOException IO异常
     */
    InputStream download(String path) throws ClientException, IOException;
    
    /**
     * 删除OSS中的文件
     * @param path OSS中的文件路径
     * @return 是否删除成功
     * @throws ClientException 客户端异常
     */
    boolean delete(String path) throws ClientException;


    /**
     * 上传单张图片
     *
     * @param file 图片文件
     * @param moduleName 模块名称
     * @return 上传结果
     */
    FileUploadResult uploadImage(MultipartFile file, String moduleName);

    /**
     * 批量上传图片
     *
     * @param files 图片文件列表
     * @param moduleName 模块名称
     * @return 上传结果列表
     */
    List<FileUploadResult> batchUploadImages(List<MultipartFile> files, String moduleName);

    String externalPath(String path);

}
