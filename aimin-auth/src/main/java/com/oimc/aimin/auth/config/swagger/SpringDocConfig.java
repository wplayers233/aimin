package com.oimc.aimin.auth.config.swagger;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI restfulOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Aimin-Admin-Server-API")
                        .description("RESTful风格接口")
                        .version("V1.0.0")
                        .license(new License().name("Aimin-Admin-Server")))
                .externalDocs(new ExternalDocumentation()
                        .description(""));
    }

}
