package com.oimc.aimin.generator.config;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.BeetlTemplateEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Generate {

    private final MysqlProperties properties;


    @PostConstruct
    public void exec() {
        FastAutoGenerator.create(properties.getUrl(), properties.getUsername(), properties.getPassword())
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(properties.getUsername())
                            .outputDir(Paths.get(System.getProperty("user.dir")) + File.separator +"generate_output")
                            .commentDate("yyyy/MM/dd");
                })
                // 包配置
                .packageConfig( (scanner, builder) -> {
                    builder.parent(scanner.apply("输入包名"))
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service")
                            .xml("xml");
                })
                // 策略配置
                .strategyConfig((scanner, builder) -> {
                            builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                                    .addTablePrefix("t_")
                                    .entityBuilder()
                                    .enableLombok()
                                    .enableTableFieldAnnotation()
                                    .controllerBuilder()
                                    .disable()
                                    .serviceBuilder()
                                    .formatServiceFileName("%sService")
                                    .build();
                })
                // 使用Beetl引擎模板，默认的是Velocity引擎模板
                .templateEngine(new BeetlTemplateEngine())
                .execute();

    }
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }
}
