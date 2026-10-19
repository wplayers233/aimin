package com.oimc.aimin.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oimc.aimin.gateway")
public class AiminGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiminGatewayApplication.class, args);
    }

}
