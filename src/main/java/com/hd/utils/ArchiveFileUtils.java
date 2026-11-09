package com.hd.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipException;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import com.github.junrar.rarfile.FileHeader;
import com.google.common.collect.Sets;
import com.hd.model.vo.ArchiveExtractorVo;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 企业级压缩包提取工具类
 * <p>
 * 支持格式：zip, tar, tar.gz, rar, 7z
 * 自动递归解压嵌套压缩包，所有文件路径打平输出到 List<ArchiveExtractorVo>
 * </p>
 * <p>
 * 嵌套归档说明：<br>
 * 压缩包内的压缩包（如 a.zip 包含 b.tar.gz）会被递归提取，
 * 内层文件的 path 以前缀形式保留，如 "parent_folder/nested_archive/file.txt"
 * </p>
 */
@Slf4j
public final class ArchiveFileUtils {

    private ArchiveFileUtils() {
    }

    /** 递归层级上限，防止循环引用 / 超大嵌套 */
    private static final int MAX_RECURSION_DEPTH = 12;

    private static final String RAR_V5_UNSUPPORTED = "RAR_V5_UNSUPPORTED";
    private static final String ZIP_MALFORMED = "ZIP_MALFORMED";
    private static final String ARCHIVE_CORRUPT = "ARCHIVE_CORRUPT";
    private static final String RECURSION_EXCEEDED = "RECURSION_EXCEEDED";

    private static final Set<String> ARCHIVE_FILE_TYPES = Sets.newHashSet("zip", "tar", "tar.gz", "rar", "7z");

    /**
     * ZIP 文件名编码：优先 UTF-8（macOS / 现代工具常未置 EFS 标志），
     * 失败再回退 GBK（Windows 中文环境创建的 zip）。
     * 带 UTF-8 标志位（EFS）或 Unicode Extra Field 的条目仍由 Commons Compress 按 UTF-8 解析。
     */
    private static final Charset ZIP_FILENAME_CHARSET_PRIMARY = StandardCharsets.UTF_8;
    private static final Charset ZIP_FILENAME_CHARSET_FALLBACK = Charset.forName("GBK");

    public static boolean isArchiveFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotEmpty(extension)) {
            extension = extension.trim().toLowerCase();
            return ARCHIVE_FILE_TYPES.contains(extension);
        }
        return false;
    }

    /**
     * 从本地文件提取压缩包内容（含嵌套归档递归打平）
     */
    public static List<ArchiveExtractorVo> extract(File file) {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("file must exist and be a regular file");
        }
        try {
            byte[] content = HdFileUtils.readFileWithChannel(file);
            return extract(content, file.getName(), "", 0);
        } catch (Exception e) {
            log.error("Failed to read archive file: {}", file.getAbsolutePath(), e);
            return Collections.singletonList(buildFailed(null, file.getName(), "", ARCHIVE_CORRUPT));
        }
    }

    /**
     * 从二进制内容提取压缩包（含嵌套归档递归打平）
     */
    public static List<ArchiveExtractorVo> extract(byte[] content, String archiveFileName) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("archive content must not be empty");
        }
        if (StringUtils.isBlank(archiveFileName)) {
            throw new IllegalArgumentException("archiveFileName must not be blank");
        }
        return extract(content, archiveFileName, "", 0);
    }

    private static List<ArchiveExtractorVo> extract(byte[] content, String archiveFileName, String pathPrefix, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return Collections.singletonList(
                    buildFailed(content, archiveFileName, pathPrefix, RECURSION_EXCEEDED));
        }

        ArchiveFormat format = detectFormat(archiveFileName, content);
        if (format == null) {
            log.warn("Unsupported archive format: {}", archiveFileName);
            return Collections.singletonList(
                    buildFailed(content, archiveFileName, pathPrefix, ARCHIVE_CORRUPT));
        }

        List<ArchiveEntry> entries = null;
        try {
            entries = readArchive(content, format);
        } catch (UnsupportedRarV5Exception e) {
            log.warn("RAR v5 detected, fallback to sevenzip: {}",
                    pathPrefixOrName(pathPrefix, archiveFileName));
            try {
                entries = parseRarWithSevenZip(content);
            } catch (IOException ex) {
                log.warn("RAR v5 extract failed: {}", pathPrefixOrName(pathPrefix, archiveFileName), ex);
                return Collections.singletonList(
                        buildFailed(content, archiveFileName, pathPrefix, RAR_V5_UNSUPPORTED));
            }
        } catch (ZipException e) {
            log.warn("Malformed ZIP archive: {}", pathPrefixOrName(pathPrefix, archiveFileName), e);
            return Collections.singletonList(
                    buildFailed(content, archiveFileName, pathPrefix, ZIP_MALFORMED));
        } catch (SecurityException e) {
            log.warn("Unsafe archive entry path: {}", pathPrefixOrName(pathPrefix, archiveFileName), e);
            return Collections.singletonList(
                    buildFailed(content, archiveFileName, pathPrefix, ARCHIVE_CORRUPT));
        } catch (RarException | ArchiveException | IOException e) {
            log.warn("Corrupt archive: {}", pathPrefixOrName(pathPrefix, archiveFileName), e);
            return Collections.singletonList(
                    buildFailed(content, archiveFileName, pathPrefix, ARCHIVE_CORRUPT));
        }

        List<ArchiveExtractorVo> results = new ArrayList<>();
        for (ArchiveEntry entry : entries) {
            String entryPath = joinPath(pathPrefix, entry.name);
            String entryFileName = getFileName(entry.name);

            if (isArchiveFileName(entryFileName)) {
                List<ArchiveExtractorVo> nested = extract(entry.content, entryFileName, entryPath, depth + 1);
                if (isSingleFailedResult(nested)) {
                    results.add(ArchiveExtractorVo.builder()
                            .content(entry.content).fileName(entryFileName).path(entryPath).archive(true)
                            .extractError(nested.get(0).getExtractError())
                            .build());
                } else {
                    results.addAll(nested);
                }
            } else {
                results.add(ArchiveExtractorVo.builder().content(entry.content)
                        .fileName(entryFileName).path(entryPath)
                        .build());
            }
        }
        return results;
    }

    private static List<ArchiveEntry> readArchive(byte[] content, ArchiveFormat format)
            throws IOException, RarException, ArchiveException {
        switch (format) {
            case ZIP:
                return parseZip(content);
            case TAR:
                return parseTar(content, false);
            case TAR_GZ:
                return parseTar(content, true);
            case RAR:
                if (isRarV5(content)) {
                    return parseRarWithSevenZip(content);
                }
                return parseRarWithJunrar(content);
            case SEVEN_Z:
                return parse7z(content);
            default:
                throw new ArchiveException("Unsupported format: " + format);
        }
    }

    private static List<ArchiveEntry> parseZip(byte[] content) throws IOException {

        try {
            return parseZipWithCharset(content, ZIP_FILENAME_CHARSET_PRIMARY);
        } catch (IOException e) {
            if (!isCharsetDecodeFailure(e)) {
                throw e;
            }
            log.warn("ZIP filename decode failed with {}, retry {}: {}",
                    ZIP_FILENAME_CHARSET_PRIMARY.name(), ZIP_FILENAME_CHARSET_FALLBACK.name(),
                    e.toString());
            return parseZipWithCharset(content, ZIP_FILENAME_CHARSET_FALLBACK);
        }
    }

    private static List<ArchiveEntry> parseZipWithCharset(byte[] content, Charset charset) throws IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(content),
                charset.name(),
                true,
                true)) {
            ZipArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = normalizeEntryName(entry.getName());
                if (StringGarbledUtil.isGarbled(name)) {
                    throw new UnmappableCharacterException(500);
                }
                validateEntryPath(name);
                entries.add(new ArchiveEntry(name, readAllBytes(zis)));
            }
        }
        return entries;
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

    private static List<ArchiveEntry> parseTar(byte[] content, boolean gzip) throws IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        InputStream raw = new ByteArrayInputStream(content);
        InputStream input = gzip ? new GzipCompressorInputStream(raw) : raw;
        try (TarArchiveInputStream tis = new TarArchiveInputStream(input)) {
            TarArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = normalizeEntryName(entry.getName());
                validateEntryPath(name);
                entries.add(new ArchiveEntry(name, readAllBytes(tis)));
            }
        }
        return entries;
    }

    private static List<ArchiveEntry> parseRarWithJunrar(byte[] content) throws RarException, IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        if (null == content || content.length < 1024) {
            return entries;
        }
        try (Archive archive = new Archive(new ByteArrayInputStream(content))) {
            for (FileHeader header : archive.getFileHeaders()) {
                if (header.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                archive.extractFile(header, baos);
                String name = normalizeEntryName(resolveRarFileName(header));
                validateEntryPath(name);
                entries.add(new ArchiveEntry(name, baos.toByteArray()));
            }
        }
        return entries;
    }

    private static List<ArchiveEntry> parseRarWithSevenZip(byte[] content) throws IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        try (IInArchive archive = SevenZip.openInArchive(null, new ByteArrayStream(content, false))) {
            ISimpleInArchive simple = archive.getSimpleInterface();
            for (ISimpleInArchiveItem item : simple.getArchiveItems()) {
                if (item.isFolder()) {
                    continue;
                }
                String name = normalizeEntryName(item.getPath());
                validateEntryPath(name);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ExtractOperationResult result = item.extractSlow(data -> {
                    baos.write(data, 0, data.length);
                    return data.length;
                });
                if (result != ExtractOperationResult.OK) {
                    throw new IOException("Failed to extract RAR entry: " + name + ", result=" + result);
                }
                entries.add(new ArchiveEntry(name, baos.toByteArray()));
            }
        } catch (SevenZipException e) {
            throw new IOException("RAR extract failed via sevenzip", e);
        }
        return entries;
    }

    private static boolean isRarV5(byte[] content) {
        return content != null
                && content.length >= 7
                && content[0] == 0x52
                && content[1] == 0x61
                && content[2] == 0x72
                && content[3] == 0x21
                && content[4] == 0x1A
                && content[5] == 0x07
                && content[6] == 0x01;
    }

    private static List<ArchiveEntry> parse7z(byte[] content) throws IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        try (SevenZFile sevenZFile = SevenZFile.builder().setByteArray(content).get()) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = normalizeEntryName(entry.getName());
                validateEntryPath(name);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InputStream entryStream = sevenZFile.getInputStream(entry)) {
                    entries.add(new ArchiveEntry(name, readAllBytes(entryStream)));
                }
            }
        }
        return entries;
    }

    private static ArchiveFormat detectFormat(String fileName, byte[] content) {
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
        return detectFormatByMagic(content);
    }

    private static ArchiveFormat detectFormatByMagic(byte[] content) {
        if (content == null || content.length < 4) {
            return null;
        }
        if (content[0] == 'P' && content[1] == 'K') {
            return ArchiveFormat.ZIP;
        }
        if ((content[0] & 0xFF) == 0x1F && (content[1] & 0xFF) == 0x8B) {
            return ArchiveFormat.TAR_GZ;
        }
        if (content.length >= 6
                && content[0] == 'R' && content[1] == 'a' && content[2] == 'r' && content[3] == '!') {
            return ArchiveFormat.RAR;
        }
        if (content.length >= 6
                && content[0] == 0x37 && content[1] == 0x7A
                && content[2] == (byte) 0xBC && content[3] == (byte) 0xAF) {
            return ArchiveFormat.SEVEN_Z;
        }
        if (content.length > 262) {
            String magic = new String(content, 257, 5, StandardCharsets.US_ASCII);
            if ("ustar".equals(magic)) {
                return ArchiveFormat.TAR;
            }
        }
        return null;
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

    private static boolean isSingleFailedResult(List<ArchiveExtractorVo> results) {
        return results.size() == 1 && results.get(0).isExtractFailed();
    }

    private static ArchiveExtractorVo buildFailed(byte[] content, String fileName, String pathPrefix, String error) {
        String path = StringUtils.isNotEmpty(pathPrefix) ? pathPrefix : fileName;
        return ArchiveExtractorVo.builder()
                .content(content)
                .fileName(fileName)
                .path(path)
                .archive(true)
                .extractError(error)
                .build();
    }

    private static String pathPrefixOrName(String pathPrefix, String fileName) {
        return StringUtils.isNotEmpty(pathPrefix) ? pathPrefix : fileName;
    }

    private static String joinPath(String prefix, String entryName) {
        String name = normalizeEntryName(entryName);
        if (StringUtils.isEmpty(prefix)) {
            return name;
        }
        return prefix + "/" + name;
    }

    private static String getFileName(String path) {
        String normalized = normalizeEntryName(path);
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    private static String resolveRarFileName(FileHeader header) {
        if (StringUtils.isNotEmpty(header.getFileNameW())) {
            return header.getFileNameW();
        }
        return header.getFileNameString();
    }

    private static String normalizeEntryName(String name) {
        if (name == null) {
            return "";
        }
        return name.replace('\\', '/').replaceAll("^/+", "");
    }

    private static void validateEntryPath(String name) {
        if (name.contains("..")) {
            throw new SecurityException("Invalid archive entry path: " + name);
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    private enum ArchiveFormat {
        ZIP, TAR, TAR_GZ, RAR, SEVEN_Z
    }

    private static final class ArchiveEntry {
        private final String name;
        private final byte[] content;
        private ArchiveEntry(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }
    }
}
