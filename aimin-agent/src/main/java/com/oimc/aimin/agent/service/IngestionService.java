package com.oimc.aimin.agent.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 医疗知识入库管线：
 * PDF/MD/TXT → 提文本（PDF 按页）→ 章节标题切片 + 512 字符兜底 → bge-small-zh embedding → 向量库
 */
@Service
public class IngestionService {

    private static final int MAX_CHUNK_CHARS = 512;

    /** 仅允许的文档扩展名 */
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of("pdf", "md", "markdown", "txt");

    /** Windows/Linux 都非法或容易出问题的字符 */
    private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");

    private static final Pattern HEADING = Pattern.compile(
            "^\\s*(#{1,6}\\s+.*|(第[一二三四五六七八九十百千]+[章节篇部])|([一二三四五六七八九十]+、)|([（(][一二三四五六七八九十]+[)）])|(\\d+(\\.\\d+)*\\s+\\S)).*$");

    public record DocInfo(String docId, String fileName, int chunks, long bytes, long ingestedAt) {
    }

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;
    private final Path vectorFile;
    private final Path uploadDir;

    private final List<DocInfo> registry = new ArrayList<>();

    public IngestionService(EmbeddingModel embeddingModel,
                            EmbeddingStore<TextSegment> store,
                            @Value("${aimin.agent.vector-file}") String vectorFile,
                            @Value("${aimin.agent.upload-dir}") String uploadDir) throws IOException {
        this.embeddingModel = embeddingModel;
        this.store = store;
        this.vectorFile = Path.of(vectorFile);
        this.uploadDir = Path.of(uploadDir);
        Files.createDirectories(this.vectorFile.toAbsolutePath().getParent());
        Files.createDirectories(this.uploadDir);
    }

    public DocInfo ingest(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空，请重新选择");
        }
        String fileName = sanitizeFileName(file.getOriginalFilename());
        String lower = fileName.toLowerCase();
        String ext = lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("仅支持 PDF、Markdown（.md）、纯文本（.txt）文档");
        }
        byte[] bytes = file.getBytes();
        // 留档：文件名已剥离目录并过滤非法字符，且再校验一次解析结果必须落在 uploadDir 内
        Path target = uploadDir.resolve(UUID.randomUUID() + "-" + fileName).normalize().toAbsolutePath();
        if (!target.startsWith(uploadDir.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("文件名不合法");
        }
        Files.write(target, bytes);

        List<PageText> pages = new ArrayList<>();
        if (lower.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                for (int p = 1; p <= doc.getNumberOfPages(); p++) {
                    stripper.setStartPage(p);
                    stripper.setEndPage(p);
                    pages.add(new PageText(p, stripper.getText(doc)));
                }
            }
        } else {
            // md / txt 及其他文本
            pages.add(new PageText(null, new String(bytes, StandardCharsets.UTF_8)));
        }

        List<TextSegment> segments = new ArrayList<>();
        for (PageText page : pages) {
            for (String chunk : chunkByHeading(page.text())) {
                if (chunk.isBlank()) continue;
                var meta = new dev.langchain4j.data.document.Metadata().put("title", fileName);
                if (page.page() != null) {
                    meta = meta.put("page", page.page());
                }
                segments.add(TextSegment.from(chunk, meta));
            }
        }
        if (segments.isEmpty()) {
            throw new IOException("未能从文档中提取到文本内容");
        }

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);
        persist();

        DocInfo info = new DocInfo(UUID.randomUUID().toString(), fileName, segments.size(), bytes.length, System.currentTimeMillis());
        registry.add(0, info);
        return info;
    }

    public List<DocInfo> listDocs() {
        return List.copyOf(registry);
    }

    // ---------- 内部实现 ----------

    /** 剥离目录部分（防 ../ 穿越）、过滤非法字符、限制长度 */
    private String sanitizeFileName(String original) {
        String name = original == null ? "" : original;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("_").strip();
        if (name.isEmpty() || name.equals(".") || name.equals("..")) {
            name = "未命名";
        }
        if (name.length() > 120) {
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
            name = name.substring(0, Math.max(1, 120 - ext.length())) + ext;
        }
        return name;
    }

    private record PageText(Integer page, String text) {
    }

    /** 章节标题优先切片，超长块按 MAX_CHUNK_CHARS 兜底 */
    private List<String> chunkByHeading(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;
        String[] lines = text.replace("\r\n", "\n").split("\n");
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            boolean isHeading = HEADING.matcher(line).matches() && line.length() < 60;
            if (isHeading && current.length() > 0) {
                flushChunk(current, chunks);
                current = new StringBuilder();
            }
            current.append(line.strip()).append('\n');
            if (current.length() >= MAX_CHUNK_CHARS) {
                flushChunk(current, chunks);
                current = new StringBuilder();
            }
        }
        flushChunk(current, chunks);
        return chunks;
    }

    private void flushChunk(StringBuilder sb, List<String> out) {
        String s = sb.toString().strip();
        if (s.isEmpty()) return;
        // 兜底：超长块二次切分
        for (int i = 0; i < s.length(); i += MAX_CHUNK_CHARS) {
            String part = s.substring(i, Math.min(i + MAX_CHUNK_CHARS, s.length())).strip();
            if (!part.isEmpty()) out.add(part);
        }
    }

    private void persist() {
        try {
            if (store instanceof InMemoryEmbeddingStore<TextSegment> mem) {
                Files.writeString(vectorFile, mem.serializeToJson(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            // 持久化失败不阻塞入库
        }
    }
}
