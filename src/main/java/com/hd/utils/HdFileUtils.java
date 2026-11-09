package com.hd.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.hd.model.vo.FileDownloadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 文件下载结果落盘工具。
 */
@Slf4j
public final class HdFileUtils {

    private HdFileUtils() {
    }

    /**
     * 将下载结果写入指定目录，文件名优先取 {@link FileDownloadResult#getFileName()}。
     *
     * @param result    下载结果
     * @param targetDir 目标目录
     * @return 写入后的完整文件路径
     */
    public static Path saveToDisk(FileDownloadResult result, String targetDir) {
        return saveToDisk(result, Paths.get(targetDir));
    }

    /**
     * 将下载结果写入指定目录。
     */
    public static Path saveToDisk(FileDownloadResult result, Path targetDir) {
        return saveToDisk(result, targetDir, null);
    }

    /**
     * 将下载结果写入指定目录，可覆盖文件名。
     *
     * @param result           下载结果
     * @param targetDir        目标目录
     * @param overrideFileName 覆盖文件名，为空时使用响应头中的文件名
     * @return 写入后的完整文件路径
     */
    public static Path saveToDisk(FileDownloadResult result, Path targetDir, String overrideFileName) {
        if (result == null || result.getContent() == null ||
                (result.getContent().length == 0 && result.getAutoCleanFile() == null)) {
            throw new IllegalArgumentException("文件内容为空，无法写入磁盘");
        }
        String fileName = StringUtils.isNotBlank(overrideFileName)
                ? sanitizeFileName(overrideFileName)
                : resolveFileName(result);
        try {
            Path dir = targetDir.toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path targetPath = dir.resolve(fileName).normalize();
            if (!targetPath.startsWith(dir)) {
                throw new IllegalArgumentException("非法文件名: " + fileName);
            }
            if (result.getAutoCleanFile() == null) {
                Files.write(targetPath, result.getContent());
                log.info("文件已写入: {}", targetPath);
            } else {
                Files.copy(result.getAutoCleanFile().getPath(), targetPath);
                result.getAutoCleanFile().close();
            }

            return targetPath;
        } catch (IOException e) {
            throw new RuntimeException("文件写入失败: " + targetDir, e);
        }
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        double v = bytes;
        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        int idx = -1;
        do {
            v /= 1024.0;
            idx++;
        } while (v >= 1024.0 && idx < units.length - 1);
        return String.format("%.2f%s", v, units[idx]);
    }

    public static byte[] readFileWithChannel(File file) {
        long fileSize = file.length();
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            int bytesRead = 0;
            while (bytesRead < fileSize) {
                int count = channel.read(buffer);
                if (count == -1) {
                    break;
                }
                bytesRead += count;
            }
            buffer.flip();
            byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static String resolveFileName(FileDownloadResult result) {
        if (StringUtils.isNotBlank(result.getFileName())) {
            return sanitizeFileName(result.getFileName());
        }
        return "download_" + System.currentTimeMillis() + guessExtension(result.getContentType());
    }

    private static String sanitizeFileName(String fileName) {
        String name = fileName.replace("\\", "").replace("/", "").trim();
        if (StringUtils.isBlank(name)) {
            return "download_" + System.currentTimeMillis();
        }
        return name;
    }

    private static String guessExtension(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return "";
        }
        String type = contentType.toLowerCase();
        if (type.contains("pdf")) {
            return ".pdf";
        }
        if (type.contains("spreadsheet") || type.contains("excel")) {
            return ".xlsx";
        }
        if (type.contains("word")) {
            return ".docx";
        }
        if (type.contains("zip")) {
            return ".zip";
        }
        if (type.contains("json")) {
            return ".json";
        }
        if (type.startsWith("image/png")) {
            return ".png";
        }
        if (type.startsWith("image/jpeg")) {
            return ".jpg";
        }
        return "";
    }
}
