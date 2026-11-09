package com.hd.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import com.github.junrar.rarfile.FileHeader;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * 大文件压缩包解压工具（流式落地本地，避免 OOM）
 * <p>
 * 支持格式：zip、tar、tar.gz / tgz、rar、7z<br>
 * 与 {@link ArchiveFileUtils} 不同：本类将条目流式写入磁盘，并按目录规则决定解压目标，
 * 递归解压嵌套压缩包后返回解压目录的绝对路径。
 * </p>
 * <p>
 * 7z / RAR5 说明：使用 sevenzipjbinding（自带 native，无需系统安装 7-Zip），
 * 通过 {@link IInArchive#extract} 批量回调解压，避免 Commons Compress 纯 Java LZMA 过慢，
 * 也避免逐条目 {@code extractSlow}。
 * RAR4 仍使用 junrar（基于本地文件，不整包进内存）。
 * </p>
 * <p>
 * 目录规则（{@code extractToRandomDir=false}）：
 * <ul>
 *   <li>若压缩包内有且仅有一个顶层目录，且名称与压缩包名（去扩展名）相同，
 *       则直接解压到压缩包所在文件夹；</li>
 *   <li>否则在压缩包所在目录创建同名新目录再解压。</li>
 * </ul>
 * {@code extractToRandomDir=true} 时：在压缩包所在目录下创建唯一随机文件夹再解压，
 * 递归嵌套包同样遵循该原则，避免解压到已存在目录。
 * </p>
 */
@Slf4j
public final class BigArchiveFileUtils {

    private BigArchiveFileUtils() {
    }

    /** 递归层级上限，防止循环嵌套 */
    private static final int MAX_RECURSION_DEPTH = 12;

    /** 读写缓冲：64KB */
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * ZIP 文件名编码：优先 UTF-8（macOS / 现代工具常未置 EFS 标志），
     * 失败再回退 GBK（Windows 中文环境创建的 zip）。
     * 带 UTF-8 标志位（EFS）或 Unicode Extra Field 的条目仍由 Commons Compress 按 UTF-8 解析。
     */
    private static final Charset ZIP_FILENAME_CHARSET_PRIMARY = java.nio.charset.StandardCharsets.UTF_8;
    private static final Charset ZIP_FILENAME_CHARSET_FALLBACK = Charset.forName("GBK");

    /**
     * 解压压缩包到本地（含嵌套递归），返回解压后目录的绝对路径。
     * <p>等价于 {@link #extractToLocal(File, boolean) extractToLocal(archiveFile, false)}。</p>
     *
     * @param archiveFile 压缩包文件
     * @return 解压后目录的绝对路径
     */
    public static String extractToLocal(File archiveFile) throws IOException {
        return extractToLocal(archiveFile, false);
    }

    /**
     * 解压压缩包到本地（含嵌套递归），返回解压后目录的绝对路径。
     *
     * @param archiveFile         压缩包文件
     * @param extractToRandomDir  为 true 时解压到唯一随机目录；为 false 时按压缩包名称解压到所在目录
     * @return 解压后目录的绝对路径
     */
    public static String extractToLocal(File archiveFile, boolean extractToRandomDir) throws IOException {
        if (archiveFile == null) {
            throw new IllegalArgumentException("archiveFile must not be null");
        }
        return extractToLocal(archiveFile.toPath(), extractToRandomDir).toString();
    }

    /**
     * 解压压缩包到本地（含嵌套递归），返回解压后目录的绝对路径。
     * <p>等价于 {@link #extractToLocal(Path, boolean) extractToLocal(archivePath, false)}。</p>
     *
     * @param archivePath 压缩包路径
     * @return 解压后目录的绝对路径
     */
    public static Path extractToLocal(Path archivePath) throws IOException {
        return extractToLocal(archivePath, false);
    }

    /**
     * 解压压缩包到本地（含嵌套递归），返回解压后目录的绝对路径。
     *
     * @param archivePath         压缩包路径
     * @param extractToRandomDir  为 true 时解压到唯一随机目录；为 false 时按压缩包名称解压到所在目录
     * @return 解压后目录的绝对路径
     */
    public static Path extractToLocal(Path archivePath, boolean extractToRandomDir) throws IOException {
        if (archivePath == null || !Files.isRegularFile(archivePath)) {
            throw new IllegalArgumentException("archive must exist and be a regular file: " + archivePath);
        }
        Path absolute = archivePath.toAbsolutePath().normalize();
        Path resultDir = extractRecursive(absolute, 0, extractToRandomDir);
        log.info("[BigArchive] 解压完成: {} -> {} (randomDir={})", absolute, resultDir, extractToRandomDir);
        return resultDir;
    }

    // ==================== 递归解压 ====================

    private static Path extractRecursive(Path archivePath, int depth, boolean extractToRandomDir)
            throws IOException {
        if (depth > MAX_RECURSION_DEPTH) {
            throw new IOException("Archive recursion depth exceeded (" + MAX_RECURSION_DEPTH
                    + "): " + archivePath);
        }

        ArchiveFormat format = detectFormat(archivePath);
        if (format == null) {
            throw new IOException("Unsupported archive format: " + archivePath.getFileName());
        }

        String baseName = archiveBaseName(archivePath.getFileName().toString());
        Path parentDir = archivePath.getParent();
        if (parentDir == null) {
            parentDir = archivePath.toAbsolutePath().getParent();
        }
        if (parentDir == null) {
            parentDir = Paths.get(".").toAbsolutePath().normalize();
        }

        Path extractRoot;
        Path resultDir;
        if (extractToRandomDir) {
            // 唯一随机目录：避免解压到已存在目录；嵌套包同样走此原则
            extractRoot = createUniqueExtractDir(parentDir);
            resultDir = extractRoot;
            log.info("[BigArchive] 创建随机解压目录: archive={}, dir={}",
                    archivePath.getFileName(), extractRoot);
        } else {
            RootAnalysis root = analyzeRoot(archivePath, format);
            if (root.singleMatchingDir) {
                // 有且仅有一个与压缩包同名的顶层目录 → 直接解压到压缩包所在文件夹
                extractRoot = parentDir;
                resultDir = parentDir.resolve(baseName).normalize();
                log.info("[BigArchive] 单根目录命中，解压到父目录: archive={}, rootDir={}",
                        archivePath.getFileName(), baseName);
            } else {
                // 否则创建与压缩包同名的新目录
                extractRoot = parentDir.resolve(baseName).normalize();
                resultDir = extractRoot;
                Files.createDirectories(extractRoot);
                log.info("[BigArchive] 创建解压目录: {}", extractRoot);
            }
        }

        extractArchive(archivePath, format, extractRoot);

        // 递归解压嵌套压缩包（沿用同一 extractToRandomDir 原则）
        List<Path> nestedArchives = findNestedArchives(resultDir);
        for (Path nested : nestedArchives) {
            if (Files.isRegularFile(nested)) {
                log.info("[BigArchive] 递归解压嵌套包 (depth={}, randomDir={}): {}",
                        depth + 1, extractToRandomDir, nested);
                extractRecursive(nested, depth + 1, extractToRandomDir);
            }
        }

        return resultDir.toAbsolutePath().normalize();
    }

    /**
     * 在 parentDir 下原子创建唯一随机解压目录。
     */
    private static Path createUniqueExtractDir(Path parentDir) throws IOException {
        Files.createDirectories(parentDir);
        return Files.createTempDirectory(parentDir, "extract_").toAbsolutePath().normalize();
    }

    // ==================== 根目录分析 ====================

    /**
     * 分析压缩包顶层结构：是否有且仅有一个目录，且名称等于压缩包名（去扩展名）。
     */
    private static RootAnalysis analyzeRoot(Path archivePath, ArchiveFormat format) throws IOException {
        String baseName = archiveBaseName(archivePath.getFileName().toString());
        Set<String> topComponents = new LinkedHashSet<>();
        Set<String> topDirs = new HashSet<>();

        for (EntryMeta meta : listEntryMetas(archivePath, format)) {
            String name = normalizeEntryName(meta.name);
            if (name.isEmpty()) {
                continue;
            }
            boolean dirHint = meta.directory || name.endsWith("/");
            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
            if (name.isEmpty()) {
                continue;
            }
            int slash = name.indexOf('/');
            String top = slash >= 0 ? name.substring(0, slash) : name;
            topComponents.add(top);
            if (dirHint || slash >= 0) {
                topDirs.add(top);
            }
        }

        if (topComponents.size() == 1) {
            String only = topComponents.iterator().next();
            if (topDirs.contains(only) && only.equals(baseName)) {
                return new RootAnalysis(true);
            }
        }
        return new RootAnalysis(false);
    }

    private static List<EntryMeta> listEntryMetas(Path archivePath, ArchiveFormat format)
            throws IOException {
        switch (format) {
            case ZIP:
                return listZipMetas(archivePath);
            case TAR:
                return listTarMetas(archivePath, false);
            case TAR_GZ:
                return listTarMetas(archivePath, true);
            case SEVEN_Z:
                return list7zMetas(archivePath);
            case RAR:
                return listRarMetas(archivePath);
            default:
                throw new IOException("Unsupported format: " + format);
        }
    }

    private static List<EntryMeta> listZipMetas(Path archivePath) throws IOException {
        try {
            return listZipMetasWithCharset(archivePath, ZIP_FILENAME_CHARSET_PRIMARY);
        } catch (IOException e) {
            if (!isCharsetDecodeFailure(e)) {
                throw e;
            }
            log.warn("[BigArchive] ZIP filename decode failed with {}, retry {}: {}",
                    ZIP_FILENAME_CHARSET_PRIMARY.name(), ZIP_FILENAME_CHARSET_FALLBACK.name(),
                    e.toString());
            return listZipMetasWithCharset(archivePath, ZIP_FILENAME_CHARSET_FALLBACK);
        }
    }

    private static List<EntryMeta> listZipMetasWithCharset(Path archivePath, Charset charset)
            throws IOException {
        List<EntryMeta> metas = new ArrayList<>();
        try (ZipFile zipFile = ZipFile.builder()
                .setPath(archivePath)
                .setCharset(charset)
                .get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                metas.add(new EntryMeta(entry.getName(), entry.isDirectory()));
            }
        }
        return metas;
    }

    private static List<EntryMeta> listTarMetas(Path archivePath, boolean gzip) throws IOException {
        List<EntryMeta> metas = new ArrayList<>();
        try (InputStream fis = new BufferedInputStream(Files.newInputStream(archivePath));
             InputStream input = gzip ? new GzipCompressorInputStream(fis) : fis;
             TarArchiveInputStream tis = new TarArchiveInputStream(input)) {
            TarArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                metas.add(new EntryMeta(entry.getName(), entry.isDirectory()));
                // 跳过内容，仅读元数据
                long size = entry.getSize();
                if (!entry.isDirectory() && size > 0) {
                    skipFully(tis, size);
                }
            }
        }
        return metas;
    }

    private static List<EntryMeta> list7zMetas(Path archivePath) throws IOException {
        return listMetasWithSevenZip(archivePath);
    }

    private static List<EntryMeta> listRarMetas(Path archivePath) throws IOException {
        if (isRarV5(archivePath)) {
            return listMetasWithSevenZip(archivePath);
        }
        try {
            return listRarMetasWithJunrar(archivePath);
        } catch (UnsupportedRarV5Exception e) {
            log.info("[BigArchive] RAR v5 detected via junrar, fallback to sevenzip: {}",
                    archivePath.getFileName());
            return listMetasWithSevenZip(archivePath);
        } catch (RarException e) {
            throw new IOException("Failed to list RAR entries: " + archivePath, e);
        }
    }

    private static List<EntryMeta> listRarMetasWithJunrar(Path archivePath)
            throws IOException, RarException {
        List<EntryMeta> metas = new ArrayList<>();
        try (Archive archive = new Archive(archivePath.toFile())) {
            for (FileHeader header : archive.getFileHeaders()) {
                metas.add(new EntryMeta(resolveRarFileName(header), header.isDirectory()));
            }
        }
        return metas;
    }

    /**
     * 使用 sevenzipjbinding 列举条目（7z / RAR5 等），仅读元数据不落地。
     */
    private static List<EntryMeta> listMetasWithSevenZip(Path archivePath) throws IOException {
        RandomAccessFile raf = null;
        RandomAccessFileInStream inStream = null;
        IInArchive archive = null;
        try {
            raf = new RandomAccessFile(archivePath.toFile(), "r");
            inStream = new RandomAccessFileInStream(raf);
            archive = SevenZip.openInArchive(null, inStream);
            int n = archive.getNumberOfItems();
            List<EntryMeta> metas = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                Object pathObj = archive.getProperty(i, PropID.PATH);
                String path = pathObj == null ? "" : String.valueOf(pathObj);
                Boolean isFolder = (Boolean) archive.getProperty(i, PropID.IS_FOLDER);
                metas.add(new EntryMeta(path, Boolean.TRUE.equals(isFolder)));
            }
            return metas;
        } catch (SevenZipException e) {
            throw new IOException("Failed to list archive entries via sevenzip: " + archivePath, e);
        } finally {
            closeQuietly(archive);
            closeQuietly(inStream);
            // inStream.close() 会关闭 raf；此处兜底避免 open 失败时泄漏
            closeQuietly(raf);
        }
    }

    // ==================== 流式解压落地 ====================

    private static void extractArchive(Path archivePath, ArchiveFormat format, Path extractRoot)
            throws IOException {
        Files.createDirectories(extractRoot);
        switch (format) {
            case ZIP:
                extractZip(archivePath, extractRoot);
                break;
            case TAR:
                extractTar(archivePath, extractRoot, false);
                break;
            case TAR_GZ:
                extractTar(archivePath, extractRoot, true);
                break;
            case SEVEN_Z:
                extract7z(archivePath, extractRoot);
                break;
            case RAR:
                extractRar(archivePath, extractRoot);
                break;
            default:
                throw new IOException("Unsupported format: " + format);
        }
    }

    private static void extractZip(Path archivePath, Path extractRoot) throws IOException {
        try {
            extractZipWithCharset(archivePath, extractRoot, ZIP_FILENAME_CHARSET_PRIMARY);
        } catch (IOException e) {
            if (!isCharsetDecodeFailure(e)) {
                throw e;
            }
            log.warn("[BigArchive] ZIP filename decode failed with {}, retry {}: {}",
                    ZIP_FILENAME_CHARSET_PRIMARY.name(), ZIP_FILENAME_CHARSET_FALLBACK.name(),
                    e.toString());
            extractZipWithCharset(archivePath, extractRoot, ZIP_FILENAME_CHARSET_FALLBACK);
        }
    }

    private static void extractZipWithCharset(Path archivePath, Path extractRoot, Charset charset)
            throws IOException {
        try (ZipFile zipFile = ZipFile.builder()
                .setPath(archivePath)
                .setCharset(charset)
                .get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = normalizeEntryName(entry.getName());
                if (entryName.isEmpty()) {
                    continue;
                }
                if (StringGarbledUtil.isGarbled(entryName)) {
                    throw new UnmappableCharacterException(500);
                }
                Path target = resolveSafePath(extractRoot, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (InputStream in = zipFile.getInputStream(entry);
                     OutputStream out = newBufferedOutput(target)) {
                    copy(in, out);
                }
                if (entry.getLastModifiedDate() != null) {
                    setLastModifiedQuietly(target, entry.getLastModifiedDate());
                }
            }
        }
    }

    private static boolean isCharsetDecodeFailure(Throwable t) {
        while (t != null) {
            if (t instanceof java.nio.charset.MalformedInputException
                    || t instanceof java.nio.charset.UnmappableCharacterException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private static void extractTar(Path archivePath, Path extractRoot, boolean gzip)
            throws IOException {
        try (InputStream fis = new BufferedInputStream(Files.newInputStream(archivePath));
             InputStream input = gzip ? new GzipCompressorInputStream(fis) : fis;
             TarArchiveInputStream tis = new TarArchiveInputStream(input)) {
            TarArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                String entryName = normalizeEntryName(entry.getName());
                if (entryName.isEmpty()) {
                    continue;
                }
                Path target = resolveSafePath(extractRoot, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (OutputStream out = newBufferedOutput(target)) {
                    copy(tis, out);
                }
                if (entry.getModTime() != null) {
                    setLastModifiedQuietly(target, entry.getModTime());
                }
            }
        }
    }

    /**
     * 7z 解压：sevenzipjbinding native LZMA，批量 {@link IInArchive#extract} 回调写入磁盘。
     */
    private static void extract7z(Path archivePath, Path extractRoot) throws IOException {
        extractWithSevenZip(archivePath, extractRoot);
    }

    /**
     * RAR 流式解压：RAR4 走 junrar（File 随机访问）；RAR5 走 sevenzipjbinding。
     * 条目内容直接写入磁盘，避免整包/整文件缓冲。
     */
    private static void extractRar(Path archivePath, Path extractRoot) throws IOException {
        if (isRarV5(archivePath)) {
            extractWithSevenZip(archivePath, extractRoot);
            return;
        }
        try {
            extractRarWithJunrar(archivePath, extractRoot);
        } catch (UnsupportedRarV5Exception e) {
            log.info("[BigArchive] RAR v5 detected via junrar, fallback to sevenzip: {}",
                    archivePath.getFileName());
            extractWithSevenZip(archivePath, extractRoot);
        } catch (RarException e) {
            throw new IOException("RAR extract failed: " + archivePath, e);
        }
    }

    private static void extractRarWithJunrar(Path archivePath, Path extractRoot)
            throws IOException, RarException {
        try (Archive archive = new Archive(archivePath.toFile())) {
            for (FileHeader header : archive.getFileHeaders()) {
                String entryName = normalizeEntryName(resolveRarFileName(header));
                if (entryName.isEmpty()) {
                    continue;
                }
                Path target = resolveSafePath(extractRoot, entryName);
                if (header.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (OutputStream out = newBufferedOutput(target)) {
                    archive.extractFile(header, out);
                }
                if (header.getMTime() != null) {
                    setLastModifiedQuietly(target, header.getMTime());
                }
            }
        }
    }

    /**
     * sevenzipjbinding 批量解压（7z / RAR5 共用）：一次 extract 回调写盘，
     * 对 solid 归档远快于 Commons Compress / 逐条 extractSlow。
     */
    private static void extractWithSevenZip(Path archivePath, Path extractRoot) throws IOException {
        RandomAccessFile raf = null;
        RandomAccessFileInStream inStream = null;
        IInArchive inArchive = null;
        DiskExtractCallback callback = null;
        try {
            raf = new RandomAccessFile(archivePath.toFile(), "r");
            inStream = new RandomAccessFileInStream(raf);
            inArchive = SevenZip.openInArchive(null, inStream);
            callback = new DiskExtractCallback(inArchive, extractRoot);
            // indices=null 表示解压全部条目；testMode=false 表示真正写出
            inArchive.extract(null, false, callback);
            callback.finish();
        } catch (SevenZipException e) {
            throw new IOException("Archive extract failed via sevenzip: " + archivePath, e);
        } finally {
            if (callback != null) {
                callback.closeQuietlyCurrent();
            }
            closeQuietly(inArchive);
            closeQuietly(inStream);
            closeQuietly(raf);
        }
    }

    /**
     * sevenzipjbinding 解压回调：按条目打开文件输出流，块写入后立即关闭。
     */
    private static final class DiskExtractCallback implements IArchiveExtractCallback {
        private final IInArchive inArchive;
        private final Path extractRoot;
        private OutputStream currentOut;
        private Path currentTarget;
        private Date currentModified;
        private volatile IOException error;

        private DiskExtractCallback(IInArchive inArchive, Path extractRoot) {
            this.inArchive = inArchive;
            this.extractRoot = extractRoot;
        }

        @Override
        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode)
                throws SevenZipException {
            closeQuietlyCurrent();
            currentTarget = null;
            currentModified = null;
            if (extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }
            try {
                Object pathObj = inArchive.getProperty(index, PropID.PATH);
                String entryName = normalizeEntryName(pathObj == null ? "" : String.valueOf(pathObj));
                if (entryName.isEmpty()) {
                    return null;
                }
                Path target = resolveSafePath(extractRoot, entryName);
                Boolean isFolder = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
                if (Boolean.TRUE.equals(isFolder) || entryName.endsWith("/")) {
                    Files.createDirectories(target);
                    return null;
                }
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Object mod = inArchive.getProperty(index, PropID.LAST_MODIFICATION_TIME);
                if (mod instanceof Date) {
                    currentModified = (Date) mod;
                }
                currentTarget = target;
                currentOut = newBufferedOutput(target);
                return new ISequentialOutStream() {
                    @Override
                    public int write(byte[] data) throws SevenZipException {
                        try {
                            currentOut.write(data);
                            return data.length;
                        } catch (IOException e) {
                            error = e;
                            throw new SevenZipException("Write entry failed: " + entryName, e);
                        }
                    }
                };
            } catch (IOException e) {
                error = e;
                throw new SevenZipException("Prepare extract stream failed, index=" + index, e);
            }
        }

        @Override
        public void prepareOperation(ExtractAskMode extractAskMode) {
            // no-op
        }

        @Override
        public void setOperationResult(ExtractOperationResult extractOperationResult)
                throws SevenZipException {
            try {
                closeQuietlyCurrent();
                if (extractOperationResult != ExtractOperationResult.OK) {
                    throw new SevenZipException("Extract entry failed, result="
                            + extractOperationResult
                            + (currentTarget == null ? "" : ", target=" + currentTarget));
                }
                if (currentTarget != null && currentModified != null) {
                    setLastModifiedQuietly(currentTarget, currentModified);
                }
            } finally {
                currentTarget = null;
                currentModified = null;
            }
        }

        @Override
        public void setTotal(long total) {
            // no-op
        }

        @Override
        public void setCompleted(long complete) {
            // no-op
        }

        private void finish() throws IOException {
            closeQuietlyCurrent();
            if (error != null) {
                throw error;
            }
        }

        private void closeQuietlyCurrent() {
            if (currentOut != null) {
                try {
                    currentOut.close();
                } catch (IOException e) {
                    if (error == null) {
                        error = e;
                    }
                } finally {
                    currentOut = null;
                }
            }
        }
    }

    private static void closeQuietly(IInArchive archive) {
        if (archive == null) {
            return;
        }
        try {
            archive.close();
        } catch (Exception ignored) {
            /* ignore */
        }
    }

    private static void closeQuietly(RandomAccessFileInStream inStream) {
        if (inStream == null) {
            return;
        }
        try {
            inStream.close();
        } catch (Exception ignored) {
            /* ignore */
        }
    }

    private static void closeQuietly(RandomAccessFile raf) {
        if (raf == null) {
            return;
        }
        try {
            raf.close();
        } catch (Exception ignored) {
            /* ignore */
        }
    }

    // ==================== 嵌套压缩包扫描 ====================

    private static List<Path> findNestedArchives(Path rootDir) throws IOException {
        List<Path> archives = new ArrayList<>();
        if (rootDir == null || !Files.isDirectory(rootDir)) {
            return archives;
        }
        Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String name = file.getFileName().toString();
                if (isArchiveFileName(name)) {
                    archives.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return archives;
    }

    // ==================== 格式检测 ====================

    private static ArchiveFormat detectFormat(Path archivePath) throws IOException {
        String fileName = archivePath.getFileName().toString();
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz")) {
            return ArchiveFormat.TAR_GZ;
        }
        if (lower.endsWith(".tar")) {
            return ArchiveFormat.TAR;
        }
        if (lower.endsWith(".zip")) {
            return ArchiveFormat.ZIP;
        }
        if (lower.endsWith(".rar")) {
            return ArchiveFormat.RAR;
        }
        if (lower.endsWith(".7z")) {
            return ArchiveFormat.SEVEN_Z;
        }
        return detectFormatByMagic(archivePath);
    }

    private static ArchiveFormat detectFormatByMagic(Path archivePath) throws IOException {
        byte[] header = new byte[270];
        try (InputStream in = Files.newInputStream(archivePath)) {
            int n = readFully(in, header);
            if (n < 4) {
                return null;
            }
            if (header[0] == 'P' && header[1] == 'K') {
                return ArchiveFormat.ZIP;
            }
            if ((header[0] & 0xFF) == 0x1F && (header[1] & 0xFF) == 0x8B) {
                return ArchiveFormat.TAR_GZ;
            }
            if (n >= 6
                    && header[0] == 'R' && header[1] == 'a' && header[2] == 'r' && header[3] == '!') {
                return ArchiveFormat.RAR;
            }
            if (n >= 6
                    && header[0] == 0x37 && header[1] == 0x7A
                    && header[2] == (byte) 0xBC && header[3] == (byte) 0xAF) {
                return ArchiveFormat.SEVEN_Z;
            }
            if (n > 262) {
                String magic = new String(header, 257, 5, java.nio.charset.StandardCharsets.US_ASCII);
                if ("ustar".equals(magic)) {
                    return ArchiveFormat.TAR;
                }
            }
        }
        return null;
    }

    /** RAR5 魔数：Rar!\x1A\x07\x01（末字节 0x01；RAR4 为 0x00） */
    private static boolean isRarV5(Path archivePath) throws IOException {
        byte[] header = new byte[7];
        try (InputStream in = Files.newInputStream(archivePath)) {
            int n = readFully(in, header);
            return n >= 7
                    && header[0] == 0x52
                    && header[1] == 0x61
                    && header[2] == 0x72
                    && header[3] == 0x21
                    && header[4] == 0x1A
                    && header[5] == 0x07
                    && header[6] == 0x01;
        }
    }

    private static String resolveRarFileName(FileHeader header) {
        if (StringUtils.isNotEmpty(header.getFileNameW())) {
            return header.getFileNameW();
        }
        return header.getFileNameString();
    }

    // ==================== 路径 & IO 工具 ====================

    private static String archiveBaseName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return fileName;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".tar.gz")) {
            return fileName.substring(0, fileName.length() - 7);
        }
        if (lower.endsWith(".tgz")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static boolean isArchiveFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip")
                || lower.endsWith(".tar")
                || lower.endsWith(".tar.gz")
                || lower.endsWith(".tgz")
                || lower.endsWith(".rar")
                || lower.endsWith(".7z");
    }

    private static String normalizeEntryName(String name) {
        if (name == null) {
            return "";
        }
        return name.replace('\\', '/').replaceAll("^/+", "");
    }

    /**
     * 解析安全目标路径：拒绝 {@code ..}、绝对路径，防止 Zip Slip。
     */
    private static Path resolveSafePath(Path outputDir, String entryName) throws IOException {
        Path outputRoot = outputDir.toAbsolutePath().normalize();
        String sanitized = sanitizePath(entryName);
        if (sanitized.isEmpty()) {
            throw new SecurityException("Empty archive entry path");
        }
        Path target = outputRoot.resolve(sanitized).normalize();
        if (!target.startsWith(outputRoot)) {
            throw new SecurityException("Zip Slip 危险路径: " + entryName);
        }
        return target;
    }

    private static String sanitizePath(String path) {
        if (path == null) {
            return "";
        }
        String s = path.replace('\0', '_').replace('\\', '/');
        boolean trailingSlash = s.endsWith("/");
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("/")) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                throw new SecurityException("路径包含 '..': " + path);
            }
            StringBuilder cleaned = new StringBuilder(part.length());
            for (int i = 0; i < part.length(); i++) {
                char c = part.charAt(i);
                cleaned.append(c < 32 ? '_' : c);
            }
            part = cleaned.toString();
            if (part.length() > 200) {
                part = part.substring(0, 200);
            }
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(part);
        }
        if (trailingSlash && sb.length() > 0) {
            sb.append('/');
        }
        return sb.toString();
    }

    private static OutputStream newBufferedOutput(Path target) throws IOException {
        return new BufferedOutputStream(
                Files.newOutputStream(target,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE),
                BUFFER_SIZE);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    private static void skipFully(InputStream in, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped > 0) {
                remaining -= skipped;
                continue;
            }
            if (in.read() == -1) {
                break;
            }
            remaining--;
        }
    }

    private static int readFully(InputStream in, byte[] buf) throws IOException {
        int off = 0;
        while (off < buf.length) {
            int n = in.read(buf, off, buf.length - off);
            if (n < 0) {
                break;
            }
            off += n;
        }
        return off;
    }

    private static void setLastModifiedQuietly(Path target, Date date) {
        try {
            Files.setLastModifiedTime(target, FileTime.fromMillis(date.getTime()));
        } catch (Exception ignored) {
            /* 文件系统不支持则忽略 */
        }
    }

    // ==================== 内部类型 ====================

    private enum ArchiveFormat {
        ZIP, TAR, TAR_GZ, RAR, SEVEN_Z
    }

    private static final class RootAnalysis {
        private final boolean singleMatchingDir;

        private RootAnalysis(boolean singleMatchingDir) {
            this.singleMatchingDir = singleMatchingDir;
        }
    }

    private static final class EntryMeta {
        private final String name;
        private final boolean directory;

        private EntryMeta(String name, boolean directory) {
            this.name = name;
            this.directory = directory;
        }
    }
}
