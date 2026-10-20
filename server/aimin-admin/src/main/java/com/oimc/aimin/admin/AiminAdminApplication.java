package com.oimc.aimin.admin;

import com.oimc.aimin.admin.common.BeanManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.oimc.aimin.admin.mapper")
@EnableCaching
@EnableFeignClients
public class AiminAdminApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(AiminAdminApplication.class, args);
        BeanManager.setAppContext(app);
    }
}
