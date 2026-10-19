package com.oimc.aimin.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oimc.aimin.agent.retrieval.Bm25Index;
import com.oimc.aimin.agent.retrieval.KnowledgeChunk;
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

    /** 超长块二次切分时的重叠字符数，避免答案恰好落在切口 */
    private static final int CHUNK_OVERLAP = 80;

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
    private final Bm25Index bm25Index;
    private final ObjectMapper objectMapper;
    private final Path vectorFile;
    private final Path docsFile;
    private final Path uploadDir;

    private final List<DocInfo> registry = new ArrayList<>();
    private final List<KnowledgeChunk> chunkRegistry = new ArrayList<>();

    /** 文档注册表文件：文档元数据 + 切片文本，重启后 BM25 与文档列表可恢复 */
    public record RegistryFile(List<DocInfo> docs, List<KnowledgeChunk> chunks) {
    }

    public IngestionService(EmbeddingModel embeddingModel,
                            EmbeddingStore<TextSegment> store,
                            Bm25Index bm25Index,
                            ObjectMapper objectMapper,
                            @Value("${aimin.agent.vector-file}") String vectorFile,
                            @Value("${aimin.agent.docs-file:./data/agent-docs.json}") String docsFile,
                            @Value("${aimin.agent.upload-dir}") String uploadDir) throws IOException {
        this.embeddingModel = embeddingModel;
        this.store = store;
        this.bm25Index = bm25Index;
        this.objectMapper = objectMapper;
        this.vectorFile = Path.of(vectorFile);
        this.docsFile = Path.of(docsFile);
        this.uploadDir = Path.of(uploadDir);
        Files.createDirectories(this.vectorFile.toAbsolutePath().getParent());
        Files.createDirectories(this.uploadDir);
        loadRegistry();
    }

    private void loadRegistry() {
        if (!Files.isRegularFile(docsFile)) {
            return;
        }
        try {
            RegistryFile loaded = objectMapper.readValue(docsFile.toFile(), RegistryFile.class);
            if (loaded != null) {
                if (loaded.docs() != null) {
                    registry.addAll(loaded.docs());
                }
                if (loaded.chunks() != null) {
                    chunkRegistry.addAll(loaded.chunks());
                    bm25Index.addAll(loaded.chunks());
                }
            }
        } catch (Exception e) {
            // 注册表损坏则从空开始，向量库仍可独立工作
        }
    }

    public DocInfo ingest(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空，请重新选择");
        }
        return ingestBytes(sanitizeFileName(file.getOriginalFilename()), file.getBytes(), true);
    }

    /** 本地目录灌库（演示语料 / 运维导入），不复制原文件 */
    public DocInfo ingest(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IllegalArgumentException("文件不存在：" + source);
        }
        return ingestBytes(sanitizeFileName(source.getFileName().toString()), Files.readAllBytes(source), false);
    }

    private DocInfo ingestBytes(String fileName, byte[] bytes, boolean keepCopy) throws IOException {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("文件为空，请重新选择");
        }
        String lower = fileName.toLowerCase();
        String ext = lower.contains(".") ? lower.substring(lower.lastIndexOf('.') + 1) : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("仅支持 PDF、Markdown（.md）、纯文本（.txt）文档");
        }
        // 留档：文件名已剥离目录并过滤非法字符，且再校验一次解析结果必须落在 uploadDir 内
        if (keepCopy) {
            Path target = uploadDir.resolve(UUID.randomUUID() + "-" + fileName).normalize().toAbsolutePath();
            if (!target.startsWith(uploadDir.toAbsolutePath().normalize())) {
                throw new IllegalArgumentException("文件名不合法");
            }
            Files.write(target, bytes);
        }

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

        String docId = UUID.randomUUID().toString();
        List<TextSegment> segments = new ArrayList<>();
        List<KnowledgeChunk> newChunks = new ArrayList<>();
        int chunkIndex = 0;
        for (PageText page : pages) {
            for (String chunk : chunkByHeading(page.text())) {
                if (chunk.isBlank()) continue;
                var meta = new dev.langchain4j.data.document.Metadata().put("title", fileName);
                if (page.page() != null) {
                    meta = meta.put("page", page.page());
                }
                segments.add(TextSegment.from(chunk, meta));
                newChunks.add(new KnowledgeChunk(docId + "#" + chunkIndex, fileName, chunkIndex, null, chunk));
                chunkIndex++;
            }
        }
        if (segments.isEmpty()) {
            throw new IOException("未能从文档中提取到文本内容");
        }

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);

        DocInfo info = new DocInfo(docId, fileName, segments.size(), bytes.length, System.currentTimeMillis());
        registry.add(0, info);
        chunkRegistry.addAll(newChunks);
        bm25Index.addAll(newChunks);
        persist();
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
        if (s.length() <= MAX_CHUNK_CHARS) {
            out.add(s);
            return;
        }
        // 兜底：超长块二次切分，相邻块保留 CHUNK_OVERLAP 字符重叠
        int step = MAX_CHUNK_CHARS - CHUNK_OVERLAP;
        for (int i = 0; i < s.length(); i += step) {
            String part = s.substring(i, Math.min(i + MAX_CHUNK_CHARS, s.length())).strip();
            if (!part.isEmpty()) out.add(part);
            if (i + MAX_CHUNK_CHARS >= s.length()) break;
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
        try {
            Files.createDirectories(docsFile.toAbsolutePath().getParent());
            RegistryFile data = new RegistryFile(List.copyOf(registry), List.copyOf(chunkRegistry));
            Files.writeString(docsFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
                    StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // 注册表写失败不影响本次入库
        }
    }
}
