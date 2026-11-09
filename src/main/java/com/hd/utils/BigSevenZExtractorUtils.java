package com.hd.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

/**
 * 企业级 7z 压缩包提取工具（超大文件优化版）
 *
 * <p>适用场景：大体积 7z 归档文件提取（纯 Java 实现）。</p>
 * <ul>
 *   <li>零全量缓冲：{@link SeekableByteChannel} 顺序读取，不将整个归档加载到内存</li>
 *   <li>流式写入：每个文件边读边写磁盘，不做二次内存缓冲</li>
 *   <li>顺序解压：使用 {@link SevenZFile#read(byte[])}，避免 {@code getInputStream} 触发随机访问导致 Solid 归档反复解压</li>
 *   <li>进度可感知：{@link ExtractProgress} 回调，按字节节流上报</li>
 *   <li>CRC 保护：读完当前 entry 后由 Commons Compress 自动校验 CRC</li>
 *   <li>断点续提：{@code force=false} 时跳过已存在的文件（仍需顺序解码丢弃内容）</li>
 *   <li>密码支持：支持加密 7z 归档</li>
 *   <li>目录安全：防 Zip Slip（拒绝 {@code ..}），自动创建中间目录</li>
 * </ul>
 *
 * <p><b>性能说明：</b>纯 Java LZMA/LZMA2 通常只有数 MB/s～十余 MB/s。
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * BigSevenZExtractorUtils extractor = new BigSevenZExtractorUtils();
 *
 * List<SevenZEntryInfo> results = extractor.extract(
 *     Paths.get("/data/huge.7z"), Paths.get("/output/huge"), null, null, false, 1);
 *
 * extractor.extract(Paths.get("/data/huge.7z"), Paths.get("/output/huge"),
 *     progress -> System.out.printf("[%d%%] %s%n", progress.getPercent(), progress.getCurrentFile()),
 *     null, false, 1);
 * }</pre>
 */
@Slf4j
public class BigSevenZExtractorUtils {

    /** 读写缓冲：64KB，显著减少 syscall / 编解码往返次数（原 8KB 过小） */
    private static final int BUFFER_SIZE = 64 * 1024;

    /** 进度回调最小时间间隔 */
    private static final long PROGRESS_INTERVAL_MS = 500L;

    /** 进度回调最小字节间隔（约 2MB），与时间间隔二者满足其一即上报 */
    private static final long PROGRESS_INTERVAL_BYTES = 2L * 1024 * 1024;

    /** 心跳间隔：read() 卡在 Solid 解码时仍持续上报 */
    private static final long PROGRESS_HEARTBEAT_MS = 1000L;

    // ==================== 公开 API ====================

    /**
     * 从 File 提取 7z（同步，无进度回调）
     *
     * @param archiveFile 7z 压缩包文件
     * @param outputDir   输出目录（不存在时自动创建）
     */
    public List<SevenZEntryInfo> extract(File archiveFile, File outputDir) {
        return extract(archiveFile.toPath(), outputDir.toPath(), null, null, false);
    }

    /** 从 File 提取 7z，带进度回调 */
    public List<SevenZEntryInfo> extract(File archiveFile, File outputDir, ExtractProgress progress) {
        return extract(archiveFile.toPath(), outputDir.toPath(), progress, null, false);
    }

    /** 从 File 提取 7z（完整参数） */
    public List<SevenZEntryInfo> extract(File archiveFile, File outputDir,
                                         ExtractProgress progress, String password,
                                         boolean force) {
        return extract(archiveFile.toPath(), outputDir.toPath(), progress, password, force);
    }

    /** 从 Path 提取 7z（完整参数） */
    public List<SevenZEntryInfo> extract(Path archivePath, Path outputDir,
                                         ExtractProgress progress, String password,
                                         boolean force) {
        return doExtractPath(archivePath, outputDir, progress, password, force);
    }

    /**
     * 从 byte[] 提取 7z（注意：byte[] 会全部加载到内存，仅适用于 &lt;2GB 的归档）
     */
    public List<SevenZEntryInfo> extract(byte[] bytes, Path outputDir,
                                         ExtractProgress progress, String password,
                                         boolean force) {
        try {
            SeekableInMemoryByteChannel ch = new SeekableInMemoryByteChannel(bytes);
            try (SevenZFile szf = openSevenZ(ch, password)) {
                return doExtract(szf, outputDir, progress, force, "byte[] input");
            }
        } catch (IOException e) {
            log.error("[SevenZExtractor] byte[] 提取失败", e);
            return Collections.emptyList();
        }
    }

    // ==================== 预览 & 工具 ====================

    /** 列举归档内所有条目（不提取，用于预览内容结构） */
    public List<SevenZEntryInfo> listEntries(Path archivePath) {
        return listEntries(archivePath, null);
    }

    public List<SevenZEntryInfo> listEntries(Path archivePath, String password) {
        List<SevenZEntryInfo> entries = new ArrayList<>();
        try (SeekableByteChannel ch = Files.newByteChannel(archivePath, StandardOpenOption.READ);
             SevenZFile szf = openSevenZ(ch, password)) {
            for (SevenZArchiveEntry entry : szf.getEntries()) {
                if (entry.isDirectory() || !entry.hasStream()) continue;
                entries.add(toEntryInfo(entry));
            }
        } catch (Exception e) {
            log.error("[SevenZExtractor] 列举条目失败: {}", archivePath, e);
        }
        return entries;
    }

    /** 快速判断文件是否为有效的 7z 格式（通过魔数检测） */
    public boolean isValid7z(Path path) {
        try (SeekableByteChannel ch = Files.newByteChannel(path, StandardOpenOption.READ)) {
            byte[] header = new byte[6];
            int len = ch.read(ByteBuffer.wrap(header));
            if (len < 6) return false;
            return SevenZFile.matches(header, len);
        } catch (IOException e) {
            return false;
        }
    }

    /** 获取归档内所有文件的总解压大小（解压前原始大小） */
    public long getTotalUnpackedSize(Path archivePath) {
        return getTotalUnpackedSize(archivePath, null);
    }

    public long getTotalUnpackedSize(Path archivePath, String password) {
        try (SeekableByteChannel ch = Files.newByteChannel(archivePath, StandardOpenOption.READ);
             SevenZFile szf = openSevenZ(ch, password)) {
            long total = 0;
            for (SevenZArchiveEntry e : szf.getEntries()) {
                if (!e.isDirectory() && e.hasStream()) total += e.getSize();
            }
            return total;
        } catch (IOException e) {
            log.error("[SevenZExtractor] 计算总大小失败: {}", archivePath, e);
            return -1;
        }
    }

    // ==================== 核心提取逻辑 ====================

    private List<SevenZEntryInfo> doExtractPath(
            Path archivePath, Path outputDir,
            ExtractProgress progress, String password, boolean force) {

        validatePath(archivePath, outputDir);
        try (SeekableByteChannel ch = Files.newByteChannel(archivePath, StandardOpenOption.READ);
             SevenZFile szf = openSevenZ(ch, password)) {
            // 只打开一次：元数据来自已解析的 header，无需二遍扫描/重建 channel
            return doExtract(szf, outputDir, progress, force, archivePath.toString());
        } catch (IOException e) {
            log.error("[SevenZExtractor] 提取过程异常: {}", archivePath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 统一顺序提取：{@code getNextEntry()} + {@link SevenZFile#read(byte[])}。
     *
     * <p><b>切勿</b>在顺序遍历时调用 {@code getInputStream(entry)}：
     * 它会走随机访问路径（{@code buildDecodingStream(..., true)}），
     * 在 Solid 归档上会导致每个文件都从 folder 头部重新解压，表现为
     * 「解压一两个文件后卡住 / 速度骤降」（参见 COMPRESS-702）。</p>
     */
    private List<SevenZEntryInfo> doExtract(
            SevenZFile szf, Path outputDir,
            ExtractProgress progress, boolean overwrite, String archiveName) throws IOException {

        long totalBytes = 0;
        int totalFiles = 0;
        for (SevenZArchiveEntry e : szf.getEntries()) {
            if (!e.isDirectory() && e.hasStream()) {
                totalBytes += e.getSize();
                totalFiles++;
            }
        }

        List<SevenZEntryInfo> allEntries = new ArrayList<>(Math.max(16, totalFiles));
        long startTime = System.currentTimeMillis();
        ProgressReporter reporter = new ProgressReporter(progress, totalFiles, totalBytes, startTime);
        reporter.startHeartbeat();

        int fileIndex = 0;
        try {
            Files.createDirectories(outputDir);

            SevenZArchiveEntry entry;
            while ((entry = szf.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.hasStream()) continue;

                String entryName = cleanEntryName(entry.getName());
                if (entryName.isEmpty()) continue;

                Path targetPath;
                try {
                    targetPath = resolveSafePath(outputDir, entryName);
                } catch (SecurityException se) {
                    log.warn("[SevenZExtractor] 拒绝危险路径: {}", entryName);
                    allEntries.add(SevenZEntryInfo.builder()
                            .fileName(getFileName(entryName))
                            .path(entryName)
                            .extracted(false)
                            .extractError(se.getMessage())
                            .content(null)
                            .build());
                    reporter.beginFile(entryName, fileIndex);
                    discardEntry(szf, reporter);
                    continue;
                }

                fileIndex++;
                reporter.beginFile(entryName, fileIndex);

                if (!overwrite && Files.exists(targetPath)) {
                    // Solid 下仍必须顺序读完当前 entry，否则后续 entry 解码会堆积/错位
                    long before = reporter.getProcessedBytes();
                    discardEntry(szf, reporter);
                    // 按声明大小对齐本文件贡献，避免实际读出量与声明不一致时进度漂移
                    reporter.alignProcessed(before + Math.max(0, entry.getSize()));
                    long size = entry.getSize();
                    try {
                        size = Files.size(targetPath);
                    } catch (IOException ignored) { /* 用声明大小 */ }
                    allEntries.add(SevenZEntryInfo.builder()
                            .fileName(getFileName(entryName))
                            .path(entryName)
                            .extracted(true).skipped(true)
                            .content(null)
                            .sizeBytes(size)
                            .build());
                    reporter.emit(true);
                    continue;
                }

                try {
                    SevenZEntryInfo info = extractEntry(szf, entry, targetPath, reporter);
                    allEntries.add(info);
                    reporter.emit(true);
                } catch (Exception ex) {
                    log.warn("[SevenZExtractor] 文件提取失败: {} — {}", entryName, ex.getMessage());
                    allEntries.add(SevenZEntryInfo.builder()
                            .fileName(getFileName(entryName))
                            .path(entryName)
                            .extracted(false)
                            .extractError(ex.getClass().getSimpleName() + ": " + ex.getMessage())
                            .content(null)
                            .build());
                    reporter.emit(true);
                }
            }
        } finally {
            reporter.stopHeartbeat();
        }

        long costMs = System.currentTimeMillis() - startTime;
        int ok = 0, fail = 0, skip = 0;
        for (SevenZEntryInfo i : allEntries) {
            if (i.isSkipped()) skip++;
            else if (i.isExtracted()) ok++;
            else fail++;
        }
        double speedMBps = costMs > 0
                ? reporter.getProcessedBytes() / 1024.0 / 1024.0 / (costMs / 1000.0) : 0;
        log.info("[SevenZExtractor] 完成 {}，共 {} 文件（成功:{}, 失败:{}, 跳过:{}），耗时: {} ms，均速: {} MB/s",
                archiveName, allEntries.size(), ok, fail, skip, costMs,
                String.format("%.2f", speedMBps));
        return allEntries;
    }

    // ==================== 单文件提取（顺序读）====================

    /**
     * 顺序流式提取单个文件。
     * 使用 {@link SevenZFile#read(byte[])}，保持 Solid 块的顺序解码，不做全量内存缓冲。
     */
    private SevenZEntryInfo extractEntry(
            SevenZFile szf, SevenZArchiveEntry entry, Path targetPath,
            ProgressReporter reporter) throws IOException {

        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        long written = 0;
        long entryStartProcessed = reporter.getProcessedBytes();
        byte[] buf = new byte[BUFFER_SIZE];
        try (OutputStream os = new BufferedOutputStream(
                Files.newOutputStream(targetPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE), BUFFER_SIZE)) {

            int len;
            while ((len = szf.read(buf)) != -1) {
                os.write(buf, 0, len);
                written += len;
                reporter.addBytes(len);
            }
        }

        long declared = entry.getSize();
        if (written != declared && declared > 0) {
            log.debug("[SevenZExtractor] 大小不一致（声明:{} vs 实际:{}）: {}",
                    declared, written, targetPath.getFileName());
            // 按声明大小对齐总进度，避免 percent 被个别异常 entry 拉歪
            reporter.alignProcessed(entryStartProcessed + Math.max(written, declared));
        }

        Date modDate = entry.getLastModifiedDate();
        if (modDate != null) {
            try {
                Files.setLastModifiedTime(targetPath,
                        java.nio.file.attribute.FileTime.fromMillis(modDate.getTime()));
            } catch (Exception ignored) { /* 文件系统不支持则忽略 */ }
        }

        return SevenZEntryInfo.builder()
                .fileName(getFileName(entry.getName()))
                .path(entry.getName())
                .extracted(true).skipped(false)
                .content(null)
                .sizeBytes(written)
                .compressedSizeBytes(null)
                .build();
    }

    /** 丢弃当前 entry 剩余内容，并计入进度（断点续提 / 危险路径） */
    private void discardEntry(SevenZFile szf, ProgressReporter reporter) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = szf.read(buf)) != -1) {
            if (len > 0) {
                reporter.addBytes(len);
            }
        }
    }

    /**
     * 进度上报器：时间/字节双节流 + 心跳，避免 {@code read()} 阻塞在 Solid 解码时进度“假死”。
     */
    private static final class ProgressReporter {
        private final ExtractProgress callback;
        private final int totalFiles;
        private final long totalBytes;
        private final long startTime;
        private final AtomicLong processedBytes = new AtomicLong(0);
        private final AtomicInteger fileIndex = new AtomicInteger(0);
        private final AtomicReference<String> currentFile = new AtomicReference<>("");
        private final AtomicLong lastEmitAt = new AtomicLong(0);
        private final AtomicLong lastEmitBytes = new AtomicLong(0);
        private ScheduledExecutorService heartbeat;

        ProgressReporter(ExtractProgress callback, int totalFiles, long totalBytes, long startTime) {
            this.callback = callback;
            this.totalFiles = totalFiles;
            this.totalBytes = totalBytes;
            this.startTime = startTime;
        }

        void startHeartbeat() {
            if (callback == null) return;
            heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "7z-progress-heartbeat");
                t.setDaemon(true);
                return t;
            });
            heartbeat.scheduleAtFixedRate(() -> emit(false),
                    PROGRESS_HEARTBEAT_MS, PROGRESS_HEARTBEAT_MS, TimeUnit.MILLISECONDS);
        }

        void stopHeartbeat() {
            if (heartbeat != null) {
                heartbeat.shutdownNow();
                heartbeat = null;
            }
            emit(true);
        }

        void beginFile(String name, int index) {
            currentFile.set(name);
            fileIndex.set(index);
            emit(true);
        }

        void addBytes(long n) {
            if (n <= 0) return;
            processedBytes.addAndGet(n);
            emit(false);
        }

        /** 将已处理字节对齐到至少 target（用于跳过/大小不一致） */
        void alignProcessed(long absoluteTarget) {
            processedBytes.updateAndGet(cur -> Math.max(cur, absoluteTarget));
        }

        long getProcessedBytes() {
            return processedBytes.get();
        }

        void emit(boolean force) {
            if (callback == null) return;
            long now = System.currentTimeMillis();
            long bytes = processedBytes.get();
            if (!force) {
                boolean timeOk = now - lastEmitAt.get() >= PROGRESS_INTERVAL_MS;
                boolean bytesOk = bytes - lastEmitBytes.get() >= PROGRESS_INTERVAL_BYTES;
                if (!timeOk && !bytesOk) return;
            }
            lastEmitAt.set(now);
            lastEmitBytes.set(bytes);

            SevenZProgressInfo pi = new SevenZProgressInfo();
            pi.setCurrentFile(currentFile.get());
            pi.setCurrentFileIndex(fileIndex.get());
            pi.setTotalFiles(totalFiles);
            pi.setProcessedBytes(bytes);
            pi.setTotalBytes(totalBytes);
            pi.setElapsedMs(now - startTime);
            try {
                callback.onProgress(pi);
            } catch (Exception ex) {
                log.warn("[SevenZExtractor] 进度回调异常（已忽略，不中断解压）: {}", ex.getMessage());
            }
        }
    }

    // ==================== 工具方法 ====================

    private SevenZFile openSevenZ(SeekableByteChannel ch, String password) throws IOException {
        if (password != null && !password.isEmpty()) {
            return new SevenZFile(ch, password.toCharArray());
        }
        return new SevenZFile(ch);
    }

    private void validatePath(Path archivePath, Path outputDir) {
        if (archivePath == null || !Files.exists(archivePath)) {
            throw new IllegalArgumentException("归档文件不存在: " + archivePath);
        }
        if (!isValid7z(archivePath)) {
            throw new IllegalArgumentException("文件不是有效的 7z 格式: " + archivePath);
        }
        if (outputDir == null) {
            throw new IllegalArgumentException("输出目录不能为 null");
        }
    }

    /** 清理 7z 条目名的首尾斜杠和反斜杠 */
    private String cleanEntryName(String name) {
        if (name == null) return "";
        String s = name.replace('\\', '/');
        while (s.startsWith("/")) s = s.substring(1);
        while (s.endsWith("/"))   s = s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * 解析安全目标路径：拒绝 {@code ..}、绝对路径，防止 Zip Slip。
     */
    private Path resolveSafePath(Path outputDir, String entryName) throws IOException {
        Path outputRoot = outputDir.toAbsolutePath().normalize();
        String sanitized = sanitizePath(entryName);
        Path target = outputRoot.resolve(sanitized).normalize();
        if (!target.startsWith(outputRoot)) {
            throw new SecurityException("Zip Slip 危险路径: " + entryName);
        }
        return target;
    }

    /**
     * 安全化路径：去除 null/控制字符，拒绝 {@code ..}，限制每个路径组件长度。
     */
    private String sanitizePath(String path) {
        if (path == null) return "";
        String s = path.replace('\0', '_');
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[/\\\\]")) {
            if (part.isEmpty() || ".".equals(part)) continue;
            if ("..".equals(part)) {
                throw new SecurityException("路径包含 '..': " + path);
            }
            StringBuilder cleaned = new StringBuilder(part.length());
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                cleaned.append(c < 32 ? '_' : c);
            }
            part = cleaned.toString();
            if (part.length() > 200) part = part.substring(0, 200);
            if (sb.length() > 0) sb.append(File.separatorChar);
            sb.append(part);
        }
        return sb.toString();
    }

    private String getFileName(String path) {
        if (path == null || path.isEmpty()) return path;
        int idx = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    private SevenZEntryInfo toEntryInfo(SevenZArchiveEntry entry) {
        return SevenZEntryInfo.builder()
                .fileName(getFileName(entry.getName()))
                .path(entry.getName())
                .extracted(false).skipped(false)
                .content(null)
                .sizeBytes(entry.getSize())
                .compressedSizeBytes(null)
                .build();
    }

    // ==================== 进度回调 ====================

    /** 提取进度回调接口（@FunctionalInterface，支持 lambda） */
    @FunctionalInterface
    public interface ExtractProgress {
        void onProgress(SevenZProgressInfo info);
    }

    /** 进度详情 */
    public static class SevenZProgressInfo {
        private String currentFile;
        private int    currentFileIndex;
        private int    totalFiles;
        private long   processedBytes;
        private long   totalBytes;
        private long   elapsedMs;

        /** 完成百分比（0~99，全部完成时由调用方视为 100） */
        public int getPercent() {
            if (totalBytes <= 0) return 0;
            return (int) Math.min(99, processedBytes * 100 / totalBytes);
        }

        /** 当前速度 MB/s */
        public double getSpeedMBps() {
            if (elapsedMs <= 0) return 0;
            return processedBytes / 1024.0 / 1024.0 / (elapsedMs / 1000.0);
        }

        /** 预估剩余时间 */
        public String getEta() {
            if (processedBytes <= 0 || totalBytes <= 0) return "计算中...";
            double ratio = processedBytes * 1.0 / totalBytes;
            if (ratio <= 0) return "计算中...";
            long etaMs = (long) (elapsedMs / ratio * (1.0 - ratio));
            if (etaMs <= 0) return "即将完成";
            long s = etaMs / 1000;
            if (s < 60)   return s + " 秒";
            if (s < 3600) return (s / 60) + " 分 " + (s % 60) + " 秒";
            return (s / 3600) + " 小时 " + ((s % 3600) / 60) + " 分";
        }

        public String getCurrentFile()       { return currentFile; }
        public void   setCurrentFile(String v)       { this.currentFile = v; }
        public int    getCurrentFileIndex()  { return currentFileIndex; }
        public void   setCurrentFileIndex(int v)     { this.currentFileIndex = v; }
        public int    getTotalFiles()        { return totalFiles; }
        public void   setTotalFiles(int v)           { this.totalFiles = v; }
        public long   getProcessedBytes()    { return processedBytes; }
        public void   setProcessedBytes(long v)      { this.processedBytes = v; }
        public long   getTotalBytes()        { return totalBytes; }
        public void   setTotalBytes(long v)          { this.totalBytes = v; }
        public long   getElapsedMs()         { return elapsedMs; }
        public void   setElapsedMs(long v)           { this.elapsedMs = v; }
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SevenZEntryInfo {

        /** 文件名（不含路径） */
        private String fileName;

        /** 文件在 7z 包内的完整路径（含目录层级） */
        private String path;

        /** 是否提取成功（文件写入磁盘成功） */
        private boolean extracted;

        /** 是否被跳过（断点续提场景，文件已存在） */
        @Builder.Default
        private boolean skipped = false;

        /** 提取失败时的错误信息 */
        private String extractError;

        /**
         * 文件内容（byte[]，超大文件场景建议从磁盘读取，null 表示未缓存）
         */
        private byte[] content;

        /** 解压后文件大小（字节） */
        private Long sizeBytes;

        /** 压缩后大小（字节）；无法取得时可能为 -1 */
        private Long compressedSizeBytes;

        public boolean isExtractFailed() {
            return extractError != null;
        }

        public void loadContent(java.nio.file.Path diskPath) throws java.io.IOException {
            if (this.content == null) {
                this.content = java.nio.file.Files.readAllBytes(diskPath);
            }
        }

        /**
         * 压缩率 = 压缩后 / 解压后（越小压得越狠）
         *
         * @return 0.0 ~ 1.0+，无法计算时返回 -1
         */
        public double getCompressionRatio() {
            if (sizeBytes == null || compressedSizeBytes == null
                    || sizeBytes <= 0 || compressedSizeBytes < 0) return -1;
            return (double) compressedSizeBytes / sizeBytes;
        }

        public String getReadableSize() {
            if (sizeBytes == null) return "unknown";
            if (sizeBytes < 1024) return sizeBytes + " B";
            if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
            if (sizeBytes < 1024L * 1024 * 1024) return String.format("%.1f MB", sizeBytes / 1024.0 / 1024.0);
            return String.format("%.2f GB", sizeBytes / 1024.0 / 1024.0 / 1024.0);
        }
    }
}
