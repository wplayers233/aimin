package com.oimc.aimin.generator.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "mysql")
@Component
public class MysqlProperties {
    private String url;
    private String username;
    private String password;
    private String author;
    private String packageParent;
}
