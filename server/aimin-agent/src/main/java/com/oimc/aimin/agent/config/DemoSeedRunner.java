package com.oimc.aimin.agent.config;

import com.oimc.aimin.agent.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * 演示知识库自动灌库：启动时若知识库为空，则把 ./data/kb-demo 下的文档切片入库。
 * 只在首次（知识库为空）时执行，避免重复向量化。
 */
@Component
@Order(1)
@ConditionalOnProperty(prefix = "aimin.seed-kb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoSeedRunner.class);

    private final IngestionService ingestionService;
    private final String dir;

    public DemoSeedRunner(IngestionService ingestionService,
                          @Value("${aimin.seed-kb.dir:./data/kb-demo}") String dir) {
        this.ingestionService = ingestionService;
        this.dir = dir;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!ingestionService.listDocs().isEmpty()) {
            log.info("知识库已有 {} 篇文档，跳过演示语料灌库", ingestionService.listDocs().size());
            return;
        }
        Path root = Path.of(dir);
        if (!Files.isDirectory(root)) {
            log.warn("演示语料目录不存在，跳过灌库：{}", root.toAbsolutePath());
            return;
        }
        List<Path> files;
        try (Stream<Path> stream = Files.list(root)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".md") || name.endsWith(".markdown")
                                || name.endsWith(".txt") || name.endsWith(".pdf");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            log.warn("读取演示语料目录失败：{}", e.getMessage());
            return;
        }
        int docs = 0;
        int chunks = 0;
        long start = System.currentTimeMillis();
        for (Path file : files) {
            try {
                IngestionService.DocInfo info = ingestionService.ingest(file);
                docs++;
                chunks += info.chunks();
            } catch (Exception e) {
                log.warn("灌库失败 {}：{}", file.getFileName(), e.getMessage());
            }
        }
        log.info("演示知识库灌库完成：{} 篇 / {} 切片，用时 {} ms", docs, chunks, System.currentTimeMillis() - start);
    }
}
