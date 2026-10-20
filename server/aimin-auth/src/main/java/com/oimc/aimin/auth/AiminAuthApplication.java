package com.oimc.aimin.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.oimc.aimin.auth.**.mapper")
public class AiminAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiminAuthApplication.class, args);
    }
}
