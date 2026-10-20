package com.oimc.aimin.file.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 *
 * @author 渣哥
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String region;
    private String bucketDomain;
}
