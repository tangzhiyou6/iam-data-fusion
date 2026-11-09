package com.hd.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * 超大文件 POST + Range 多线程分片下载工具（生产级）。
 * <p>
 * 能力：固定分片、断点续传、分片重试、鉴权按时刷新、磁盘空间校验、失败汇总。
 * 取大小仅探测 Range bytes=0-0，不会触发整文件下载。
 * </p>
 */
@Slf4j
public final class BigFilePostDownloadTestUtil {

    private static final MediaType JSON_TYPE =
            MediaType.parse("application/json; charset=utf-8");
    private static final Pattern CONTENT_RANGE_TOTAL =
            Pattern.compile("bytes\\s+\\d+-\\d+/(\\d+|\\*)", Pattern.CASE_INSENSITIVE);
    private static final int DEFAULT_BUFFER_SIZE = 256 * 1024;
    private static final long DEFAULT_CHUNK_SIZE = 64L * 1024 * 1024;
    private static final int DEFAULT_THREAD_COUNT = 8;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final long DEFAULT_RETRY_BASE_MS = 1000L;
    private static final String META_SUFFIX = ".download.meta";
    private static final String TMP_SUFFIX = ".download.tmp";

    private BigFilePostDownloadTestUtil() {
    }

    /**
     * 兼容旧入口。凭证必须通过系统属性注入，禁止硬编码：
     * <ul>
     *   <li>bigfile.download.url</li>
     *   <li>bigfile.download.username</li>
     *   <li>bigfile.download.password</li>
     *   <li>bigfile.download.secret</li>
     * </ul>
     */
    public static void download(String fileId, String savePath, int threadCount) throws Exception {
        DownloadConfig config = DownloadConfig.builder()
                .downloadUrl(requireSysProp("bigfile.download.url"))
                .username(requireSysProp("bigfile.download.username"))
                .password(requireSysProp("bigfile.download.password"))
                .secret(requireSysProp("bigfile.download.secret"))
                .threadCount(threadCount)
                .build();
        download(fileId, savePath, config);
    }

    public static DownloadResult download(String fileId, String savePath, DownloadConfig config)
            throws Exception {
        Objects.requireNonNull(fileId, "fileId");
        Objects.requireNonNull(savePath, "savePath");
        Objects.requireNonNull(config, "config");
        config.validate();

        Path targetPath = Paths.get(savePath).toAbsolutePath().normalize();
        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmpPath = Paths.get(targetPath + TMP_SUFFIX);
        Path metaPath = Paths.get(targetPath + META_SUFFIX);

        OkHttpClient client = buildClient(config);
        long totalSize = probeFileSize(client, fileId, config);
        if (totalSize <= 0) {
            throw new IOException("无法获取有效文件大小，服务端可能不支持 Range 或 Content-Range");
        }
        ensureDiskSpace(tmpPath, totalSize);

        long chunkSize = config.getChunkSize();
        long chunkCountLong = (totalSize + chunkSize - 1) / chunkSize;
        if (chunkCountLong > Integer.MAX_VALUE) {
            throw new IOException("分片数量过大，请增大 chunkSize: chunks=" + chunkCountLong);
        }
        int chunkCount = (int) chunkCountLong;
        BitSet completed = loadOrInitMeta(metaPath, fileId, totalSize, chunkSize, chunkCount);

        AtomicLong downloaded = new AtomicLong(countCompletedBytes(completed, totalSize, chunkSize));
        ProgressReporter progress = new ProgressReporter(totalSize, downloaded);
        long startMs = System.currentTimeMillis();
        progress.logImmediate("开始下载");

        if (!Files.exists(tmpPath)) {
            // 不预分配整文件，按写入偏移自动扩展，避免 500G+ 预分配耗时/失败
            Files.createFile(tmpPath);
        }

        ExecutorService pool = Executors.newFixedThreadPool(
                Math.min(config.getThreadCount(), Math.max(1, chunkCount)),
                namedThreads("big-file-dl"));
        CompletionService<Void> completion = new ExecutorCompletionService<>(pool);
        List<Future<Void>> futures = new ArrayList<>();
        AtomicInteger failCount = new AtomicInteger(0);
        ReentrantLock metaLock = new ReentrantLock();

        try {
            for (int i = 0; i < chunkCount; i++) {
                if (completed.get(i)) {
                    continue;
                }
                final int chunkIndex = i;
                long start = (long) chunkIndex * chunkSize;
                long end = Math.min(start + chunkSize, totalSize) - 1;
                futures.add(completion.submit(() -> {
                    downloadChunkWithRetry(client, fileId, config, tmpPath, start, end,
                            downloaded, progress, chunkIndex);
                    markCompleted(metaPath, completed, chunkIndex, metaLock);
                    return null;
                }));
            }

            int pending = futures.size();
            for (int i = 0; i < pending; i++) {
                try {
                    completion.take().get();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("分片任务失败: {}", rootMessage(e), e);
                }
            }
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }

        if (failCount.get() > 0 || completed.cardinality() != chunkCount) {
            throw new IOException(String.format(
                    "下载未完成: 失败任务=%d, 已完成分片=%d/%d, 临时文件=%s",
                    failCount.get(), completed.cardinality(), chunkCount, tmpPath));
        }

        long actualSize = Files.size(tmpPath);
        if (actualSize != totalSize) {
            throw new IOException("文件大小校验失败: expect=" + totalSize + ", actual=" + actualSize);
        }

        Path finalPath = moveToTarget(tmpPath, targetPath);
        Files.deleteIfExists(metaPath);
        long costMs = System.currentTimeMillis() - startMs;
        progress.logImmediate("下载完成");
        log.info("下载完成: path={}, size={}, cost={}s, avgSpeed={}/s",
                finalPath, HdFileUtils.formatSize(totalSize), costMs / 1000.0,
                HdFileUtils.formatSize(totalSize * 1000L / Math.max(costMs, 1)));

        return new DownloadResult(finalPath, totalSize, costMs);
    }

    private static void downloadChunkWithRetry(OkHttpClient client, String fileId,
                                               DownloadConfig config, Path tmpPath,
                                               long start, long end, AtomicLong downloaded,
                                               ProgressReporter progress, int chunkIndex)
            throws IOException {
        long expected = end - start + 1;
        IOException last = null;
        for (int attempt = 1; attempt <= config.getMaxRetries(); attempt++) {
            try {
                downloadChunkOnce(client, fileId, config, tmpPath, start, end,
                        expected, downloaded, progress);
                return;
            } catch (IOException e) {
                last = e;
                log.warn("分片[{}] {}-{} 第{}/{}次失败: {}",
                        chunkIndex, start, end, attempt, config.getMaxRetries(), e.getMessage());
                if (attempt < config.getMaxRetries()) {
                    sleepQuietly(config.getRetryBaseMs() * (1L << (attempt - 1)));
                }
            }
        }
        throw new IOException("分片[" + chunkIndex + "]重试耗尽: bytes=" + start + "-" + end, last);
    }

    private static void downloadChunkOnce(OkHttpClient client, String fileId,
                                          DownloadConfig config, Path tmpPath,
                                          long start, long end, long expected,
                                          AtomicLong downloaded, ProgressReporter progress)
            throws IOException {
        // 每次请求刷新 timestamp/sign，避免超长下载过程中鉴权过期
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = buildSign(fileId, timestamp, config.getSecret());
        String body = buildBody(fileId, timestamp, sign, config.getUsername(), config.getPassword());

        Request request = new Request.Builder()
                .url(config.getDownloadUrl())
                .header("Range", "bytes=" + start + "-" + end)
                .header("Accept", "application/octet-stream,*/*")
                .post(RequestBody.create(body, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("响应体为空, HTTP=" + code);
            }

            String contentType = response.header("Content-Type", "");
            if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                String err = peekBodyAsText(responseBody, 2048);
                throw new IOException("服务端返回 JSON 错误: HTTP=" + code + ", body=" + err);
            }

            if (code == 200) {
                // 服务端忽略 Range 会返回整文件，超大文件场景必须拒绝
                throw new IOException("服务端未按 Range 返回(HTTP 200)，拒绝继续以免写坏分片");
            }
            if (code != 206) {
                String err = peekBodyAsText(responseBody, 2048);
                throw new IOException("分片下载失败 HTTP=" + code + ", body=" + err);
            }

            validateContentRange(response.header("Content-Range"), start, end);

            try (RandomAccessFile raf = new RandomAccessFile(tmpPath.toFile(), "rw");
                 BufferedSource source = responseBody.source()) {
                raf.seek(start);
                byte[] buffer = new byte[config.getBufferSize()];
                long written = 0;
                try {
                    int len;
                    while ((len = source.read(buffer)) != -1) {
                        if (written + len > expected) {
                            throw new IOException("分片数据超出预期: written=" + (written + len)
                                    + ", expected=" + expected);
                        }
                        raf.write(buffer, 0, len);
                        written += len;
                        downloaded.addAndGet(len);
                        progress.maybeLog();
                    }
                    if (written != expected) {
                        throw new IOException("分片字节数不完整: written=" + written + ", expected=" + expected);
                    }
                } catch (IOException e) {
                    if (written > 0) {
                        downloaded.addAndGet(-written);
                    }
                    throw e;
                }
            }
        }
    }

    private static long probeFileSize(OkHttpClient client, String fileId, DownloadConfig config)
            throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = buildSign(fileId, timestamp, config.getSecret());
        String body = buildBody(fileId, timestamp, sign, config.getUsername(), config.getPassword());

        Request request = new Request.Builder()
                .url(config.getDownloadUrl())
                .header("Range", "bytes=0-0")
                .post(RequestBody.create(body, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                // 立刻消费/关闭，避免连接泄漏；探测请求不应读入大 body
                responseBody.close();
            }
            if (code == 206) {
                Long total = parseTotalFromContentRange(response.header("Content-Range"));
                if (total != null && total > 0) {
                    return total;
                }
            }
            String contentLength = response.header("Content-Length");
            if (code == 200 && contentLength != null) {
                long len = Long.parseLong(contentLength.trim());
                if (len > 1) {
                    // 200 + Content-Length 通常表示整文件，仅作弱兜底并告警
                    log.warn("探测大小得到 HTTP 200，服务端可能不支持 Range，total={}", len);
                    return len;
                }
            }
            throw new IOException("探测文件大小失败: HTTP=" + code
                    + ", Content-Range=" + response.header("Content-Range")
                    + ", Content-Length=" + contentLength);
        }
    }

    private static void validateContentRange(String contentRange, long start, long end)
            throws IOException {
        if (contentRange == null || contentRange.isEmpty()) {
            log.warn("响应缺少 Content-Range，无法严格校验分片范围");
            return;
        }
        Matcher m = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/", Pattern.CASE_INSENSITIVE)
                .matcher(contentRange);
        if (!m.find()) {
            throw new IOException("无法解析 Content-Range: " + contentRange);
        }
        long respStart = Long.parseLong(m.group(1));
        long respEnd = Long.parseLong(m.group(2));
        if (respStart != start || respEnd != end) {
            throw new IOException(String.format(
                    "Content-Range 与请求不一致: expect=%d-%d, actual=%d-%d",
                    start, end, respStart, respEnd));
        }
    }

    private static Long parseTotalFromContentRange(String contentRange) {
        if (contentRange == null) {
            return null;
        }
        Matcher m = CONTENT_RANGE_TOTAL.matcher(contentRange.trim());
        if (!m.find()) {
            return null;
        }
        String total = m.group(1);
        if ("*".equals(total)) {
            return null;
        }
        return Long.parseLong(total);
    }

    private static BitSet loadOrInitMeta(Path metaPath, String fileId, long totalSize,
                                         long chunkSize, int chunkCount) throws IOException {
        BitSet completed = new BitSet(chunkCount);
        if (!Files.exists(metaPath)) {
            saveMeta(metaPath, fileId, totalSize, chunkSize, completed);
            return completed;
        }
        List<String> lines = Files.readAllLines(metaPath);
        MetaHeader header = MetaHeader.parse(lines);
        if (!Objects.equals(header.fileId, fileId)
                || header.totalSize != totalSize
                || header.chunkSize != chunkSize) {
            log.warn("元数据与当前任务不匹配，将重新下载: meta={}, current fileId={}, size={}, chunk={}",
                    metaPath, fileId, totalSize, chunkSize);
            completed.clear();
            saveMeta(metaPath, fileId, totalSize, chunkSize, completed);
            return completed;
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.contains("=")) {
                continue;
            }
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < chunkCount) {
                completed.set(idx);
            }
        }
        log.info("断点续传: 已完成分片 {}/{}", completed.cardinality(), chunkCount);
        return completed;
    }

    private static void markCompleted(Path metaPath, BitSet completed, int chunkIndex,
                                      ReentrantLock lock) throws IOException {
        lock.lock();
        try {
            completed.set(chunkIndex);
            // 增量追加已完成分片索引；文件头在 init 时写入
            Files.write(metaPath, (chunkIndex + System.lineSeparator()).getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } finally {
            lock.unlock();
        }
    }

    private static void saveMeta(Path metaPath, String fileId, long totalSize,
                                 long chunkSize, BitSet completed) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("fileId=").append(fileId).append('\n');
        sb.append("totalSize=").append(totalSize).append('\n');
        sb.append("chunkSize=").append(chunkSize).append('\n');
        sb.append("completed=").append('\n');
        for (int i = completed.nextSetBit(0); i >= 0; i = completed.nextSetBit(i + 1)) {
            sb.append(i).append('\n');
        }
        Files.write(metaPath, sb.toString().getBytes());
    }

    private static long countCompletedBytes(BitSet completed, long totalSize, long chunkSize) {
        long sum = 0;
        for (int i = completed.nextSetBit(0); i >= 0; i = completed.nextSetBit(i + 1)) {
            long start = (long) i * chunkSize;
            long end = Math.min(start + chunkSize, totalSize) - 1;
            sum += (end - start + 1);
        }
        return sum;
    }

    private static void ensureDiskSpace(Path tmpPath, long totalSize) throws IOException {
        Path dir = tmpPath.getParent() != null ? tmpPath.getParent() : Paths.get(".");
        long usable = Files.getFileStore(dir).getUsableSpace();
        // 预留 1% 或至少 1GB
        long reserve = Math.max(1L << 30, totalSize / 100);
        if (usable < totalSize + reserve) {
            throw new IOException(String.format(
                    "磁盘空间不足: need≈%s, usable=%s, dir=%s",
                    HdFileUtils.formatSize(totalSize + reserve), HdFileUtils.formatSize(usable), dir));
        }
    }

    private static Path moveToTarget(Path tmpPath, Path targetPath) throws IOException {
        Files.deleteIfExists(targetPath);
        try {
            return Files.move(tmpPath, targetPath);
        } catch (IOException e) {
            Files.copy(tmpPath, targetPath);
            Files.deleteIfExists(tmpPath);
            return targetPath;
        }
    }

    private static OkHttpClient buildClient(DownloadConfig config) {
        Dispatcher dispatcher = new Dispatcher();
        int max = Math.max(config.getThreadCount() * 2, 32);
        dispatcher.setMaxRequests(max);
        dispatcher.setMaxRequestsPerHost(Math.max(config.getThreadCount(), 16));
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutSec(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeoutSec(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeoutSec(), TimeUnit.SECONDS)
                .callTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(
                        Math.max(config.getThreadCount(), 16), 5, TimeUnit.MINUTES))
                .build();
    }

    private static String buildBody(String fileId, String timestamp, String sign,
                                    String username, String password) {
        java.util.Map<String, String> body = new java.util.LinkedHashMap<>(8);
        body.put("fileId", fileId);
        body.put("timestamp", timestamp);
        body.put("sign", sign);
        body.put("username", username);
        body.put("password", password);
        return JsonUtils.toJSON(body);
    }

    public static String buildSign(String fileId, String timestamp, String secret) {
        return Md5Utils.md5(fileId + timestamp + secret);
    }

    private static String requireSysProp(String key) {
        String v = System.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalStateException("缺少系统属性: " + key
                    + "（请用 -D" + key + "=... 传入，勿在代码中硬编码凭证）");
        }
        return v.trim();
    }

    private static String peekBodyAsText(ResponseBody body, int max) {
        try {
            byte[] bytes = body.bytes();
            int n = Math.min(bytes.length, max);
            return new String(bytes, 0, n, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "<unreadable>";
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(Math.min(ms, 60_000L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String rootMessage(Throwable e) {
        Throwable cur = e;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage();
    }

    private static ThreadFactory namedThreads(String prefix) {
        AtomicInteger seq = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.getAndIncrement());
            t.setDaemon(false);
            return t;
        };
    }

    private static final class MetaHeader {
        private final String fileId;
        private final long totalSize;
        private final long chunkSize;

        private MetaHeader(String fileId, long totalSize, long chunkSize) {
            this.fileId = fileId;
            this.totalSize = totalSize;
            this.chunkSize = chunkSize;
        }

        private static MetaHeader parse(List<String> lines) {
            String fileId = null;
            long totalSize = -1;
            long chunkSize = -1;
            for (String line : lines) {
                if (line.startsWith("fileId=")) {
                    fileId = line.substring("fileId=".length());
                } else if (line.startsWith("totalSize=")) {
                    totalSize = Long.parseLong(line.substring("totalSize=".length()).trim());
                } else if (line.startsWith("chunkSize=")) {
                    chunkSize = Long.parseLong(line.substring("chunkSize=".length()).trim());
                } else if (line.startsWith("completed=")) {
                    break;
                }
            }
            return new MetaHeader(fileId, totalSize, chunkSize);
        }
    }

    private static final class ProgressReporter {
        private final long totalSize;
        private final AtomicLong downloaded;
        private final AtomicLong lastPrintMs = new AtomicLong(0);
        private final long startMs = System.currentTimeMillis();

        private ProgressReporter(long totalSize, AtomicLong downloaded) {
            this.totalSize = totalSize;
            this.downloaded = downloaded;
        }

        private void maybeLog() {
            long now = System.currentTimeMillis();
            long prev = lastPrintMs.get();
            if (now - prev < 1000 || !lastPrintMs.compareAndSet(prev, now)) {
                return;
            }
            logProgress(now);
        }

        private void logImmediate(String tag) {
            lastPrintMs.set(System.currentTimeMillis());
            logProgress(System.currentTimeMillis());
            log.info("[{}] progress snapshot", tag);
        }

        private void logProgress(long now) {
            long done = downloaded.get();
            long cost = Math.max(now - startMs, 1);
            double pct = totalSize > 0 ? done * 100.0 / totalSize : 0;
            long speed = done * 1000L / cost;
            long remain = Math.max(totalSize - done, 0);
            long etaSec = speed > 0 ? remain / speed : -1;
            log.info("进度: {}/{} ({}%) | 速度: {}/s | ETA: {}",
                    HdFileUtils.formatSize(done), HdFileUtils.formatSize(totalSize), String.format("%.2f", pct),
                    HdFileUtils.formatSize(speed), etaSec < 0 ? "-" : DateUtils.formatHMS(etaSec) + "s");
        }
    }

    @Getter
    @Builder
    public static class DownloadConfig {
        private final String downloadUrl;
        private final String username;
        private final String password;
        private final String secret;
        @Builder.Default
        private final int threadCount = DEFAULT_THREAD_COUNT;
        @Builder.Default
        private final long chunkSize = DEFAULT_CHUNK_SIZE;
        @Builder.Default
        private final int maxRetries = DEFAULT_MAX_RETRIES;
        @Builder.Default
        private final long retryBaseMs = DEFAULT_RETRY_BASE_MS;
        @Builder.Default
        private final int bufferSize = DEFAULT_BUFFER_SIZE;
        @Builder.Default
        private final int connectTimeoutSec = 30;
        @Builder.Default
        private final int readTimeoutSec = 300;
        @Builder.Default
        private final int writeTimeoutSec = 60;

        private void validate() {
            if (isBlank(downloadUrl) || isBlank(username) || isBlank(password) || isBlank(secret)) {
                throw new IllegalArgumentException("downloadUrl/username/password/secret 不能为空");
            }
            if (threadCount < 1 || threadCount > 256) {
                throw new IllegalArgumentException("threadCount 需在 1~256");
            }
            if (chunkSize < 1024 * 1024 || chunkSize > (1L << 30)) {
                throw new IllegalArgumentException("chunkSize 需在 1MB~1GB");
            }
            if (maxRetries < 1 || maxRetries > 20) {
                throw new IllegalArgumentException("maxRetries 需在 1~20");
            }
            if (bufferSize < 8 * 1024 || bufferSize > 4 * 1024 * 1024) {
                throw new IllegalArgumentException("bufferSize 需在 8KB~4MB");
            }
        }

        private static boolean isBlank(String s) {
            return s == null || s.trim().isEmpty();
        }
    }

    @Getter
    public static final class DownloadResult {
        private final Path path;
        private final long size;
        private final long costMs;

        public DownloadResult(Path path, long size, long costMs) {
            this.path = path;
            this.size = size;
            this.costMs = costMs;
        }
    }
}
