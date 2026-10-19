package com.oimc.aimin.file.oss;

import com.oimc.aimin.file.base.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 *
 * @author 渣哥
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "model", havingValue = "oss")
@EnableConfigurationProperties({OssProperties.class})
public class OssConfiguration {

    private final OssProperties ossProperties;

    @Bean
    public FileService fileService(){
        OssFileServiceImpl ossFileService = new OssFileServiceImpl();
        ossFileService.setOssProperties(ossProperties);
        return ossFileService;
    }
}
