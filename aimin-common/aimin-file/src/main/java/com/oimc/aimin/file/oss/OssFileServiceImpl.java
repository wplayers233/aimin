package com.oimc.aimin.file.oss;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.oimc.aimin.base.response.FileUploadResult;
import com.oimc.aimin.file.base.FileService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云OSS文件服务实现
 * @author 渣哥
 */
@Slf4j
@Setter
public class OssFileServiceImpl implements FileService {

    private OssProperties ossProperties;

    @Override
    public boolean upload(String path, InputStream fileStream){
        OSS ossClient = createOssClient();
        boolean result = false;
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), path, fileStream);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

            if (StrUtil.isNotBlank(putObjectResult.getRequestId())) {
                result = true;
                log.info("文件上传成功，路径: {}, 请求ID: {}", path, putObjectResult.getRequestId());
            }
        } catch (OSSException oe) {
            log.error("OSS服务异常: ErrorMessage: {}, ErrorCode: {}, RequestId: {}, HostId: {}", 
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
        } finally {
            ossClient.shutdown();
        }
        return result;
    }

    @Override
    public InputStream download(String path)  {
        OSS ossClient = createOssClient();
        try {
            // 判断文件是否存在
            boolean exists = ossClient.doesObjectExist(ossProperties.getBucketName(), path);
            if (!exists) {
                log.warn("文件不存在: {}", path);
                return null;
            }
            
            // 获取文件
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), path);
            log.info("文件下载成功: {}", path);
            
            // 创建ByteArrayInputStream，不直接返回ossObject.getObjectContent()是为了避免连接未关闭的问题
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream objectContent = ossObject.getObjectContent();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = objectContent.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            objectContent.close();
            
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (OSSException oe) {
            log.error("OSS服务异常: ErrorMessage: {}, ErrorCode: {}, RequestId: {}, HostId: {}", 
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        throw new RuntimeException("文件下载失败");
    }

    @Override
    public boolean delete(String path)  {
        OSS ossClient = createOssClient();
        try {
            // 判断文件是否存在
            boolean exists = ossClient.doesObjectExist(ossProperties.getBucketName(), path);
            if (!exists) {
                log.warn("要删除的文件不存在: {}", path);
                return false;
            }
            
            // 删除文件
            ossClient.deleteObject(ossProperties.getBucketName(), path);
            log.info("文件删除成功: {}", path);
            return true;
        } catch (OSSException oe) {
            log.error("OSS服务异常: ErrorMessage: {}, ErrorCode: {}, RequestId: {}, HostId: {}", 
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
        }  finally {
            ossClient.shutdown();
        }
        return true;
    }

    private OSS createOssClient() {
        System.out.println(ossProperties.getAccessKeyId());
        System.out.println(ossProperties.getSecretAccessKey());

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(ossProperties.getAccessKeyId(),
                ossProperties.getSecretAccessKey());

        // 创建 OSSClient 实例
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 显式声明使用 V4 签名算法
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(ossProperties.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .region(ossProperties.getRegion())
                .build();
    }
    @Override
    public FileUploadResult uploadImage(MultipartFile file, String moduleName) {
        if (file == null || file.isEmpty()) {
            log.error("上传的文件为空");
            return null;
        }

        if (!OssUtil.isImage(file)) {
            log.error("上传的文件不是图片");
            return null;
        }

        try {
            // 生成OSS路径
            String ossPath = OssUtil.generateOssPath(moduleName, file);
            String name = OssUtil.generateName(file);
            String path = ossPath + "/" + name;
            // 上传到OSS
            boolean uploadResult = upload(path, file.getInputStream());

            if (!uploadResult) {
                log.error("图片上传到OSS失败");
                return null;
            }

            // 构建上传结果
            return FileUploadResult.builder()
                    .currentName(name)
                    .originalName(file.getOriginalFilename())
                    .path(path)
                    .fileSize(OssUtil.getFileSizeInKb(file))
                    .fileType(OssUtil.getFileExtension(file))
                    .build();
        } catch (Exception e) {
            log.error("图片上传异常: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<FileUploadResult> batchUploadImages(List<MultipartFile> files, String moduleName) {
        if (CollUtil.isEmpty(files)) {
            log.error("批量上传的文件列表为空");
            return new ArrayList<>();
        }

        return files.stream()
                .map(file -> uploadImage(file, moduleName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override

    public String externalPath(String path){
        return ossProperties.getBucketDomain() + path;
    }
}
