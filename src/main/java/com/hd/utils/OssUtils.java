package com.hd.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CopyObjectRequest;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.DeleteObjectsResult;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.hd.config.OssProperties;
import com.hd.constants.FusionSysEnum;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.OssObjectVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 企业级工具类，支持多桶配置。
 * <p>
 * bucketKey 指配置中的桶标识（如 default、attachments），非 OSS 实际桶名。
 * </p>
 */
@Slf4j
@Component
public class OssUtils {

    public static final String BASE_OSS_PATH = "ima-data-fusion";

    private final Map<String, OSS> clients = new ConcurrentHashMap<>();
    private final Map<String, OssProperties.BucketConfig> bucketConfigs = new ConcurrentHashMap<>();
    private String defaultBucketKey;

    public OssUtils(OssProperties properties) {
        if (properties.getBuckets() != null) {
            properties.getBuckets().forEach(this::registerBucket);
        }
        this.defaultBucketKey = properties.getDefaultBucketKey();
    }

    public String defaultBucketKey() {
        return defaultBucketKey;
    }

    // ======================== 桶校验 ========================

    /**
     * 校验配置桶在 OSS 上是否存在且可访问。
     */
    public boolean existsBucket(String bucketKey) {
        OssProperties.BucketConfig config = getBucketConfig(bucketKey);
        return getClient(bucketKey).doesBucketExist(config.getBucketName());
    }

    /**
     * 校验桶，不存在时抛出异常。
     */
    public void validateBucket(String bucketKey) {
        if (!existsBucket(bucketKey)) {
            throw new IllegalArgumentException("OSS 桶不存在或不可访问: bucketKey="
                    + bucketKey + ", bucketName=" + getBucketConfig(bucketKey).getBucketName());
        }
    }

    /**
     * 校验对象是否存在。
     */
    public boolean existsObject(String bucketKey, String objectKey) {
        validateObjectKey(objectKey);
        return getClient(bucketKey).doesObjectExist(
                getBucketConfig(bucketKey).getBucketName(), normalizeObjectKey(objectKey));
    }

    // ======================== 上传（增） ========================

    public void upload(String bucketKey, String objectKey, InputStream inputStream) {
        upload(bucketKey, objectKey, inputStream, null);
    }

    public void upload(String bucketKey, String objectKey, InputStream inputStream, ObjectMetadata metadata) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        PutObjectRequest request = new PutObjectRequest(
                getBucketConfig(bucketKey).getBucketName(), key, inputStream, metadata);
        getClient(bucketKey).putObject(request);
        log.info("OSS upload success: bucketKey={}, objectKey={}", bucketKey, key);
    }

    public void upload(String bucketKey, String objectKey, byte[] content) {
        validateObjectKey(objectKey);
        if (content == null) {
            throw new IllegalArgumentException("content 不能为空");
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        upload(bucketKey, objectKey, new java.io.ByteArrayInputStream(content), metadata);
    }

    public void upload(String bucketKey, String objectKey, File file) {
        validateObjectKey(objectKey);
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("file 必须存在且为普通文件");
        }
        getClient(bucketKey).putObject(
                getBucketConfig(bucketKey).getBucketName(), normalizeObjectKey(objectKey), file);
        log.info("OSS upload file success: bucketKey={}, objectKey={}", bucketKey, objectKey);
    }

    public String buildObjectKey(String fileId, String fileName, FusionSysEnum bizNameEnum) {
        return BASE_OSS_PATH + "/" + bizNameEnum.getCode() +  "/" + fileId +  "/"  + fileName.replaceAll("\\.{2,}", ".");
    }

    // ======================== 查询（查） ========================

    /**
     * 获取对象元信息，不存在时返回 null。
     */
    public OssObjectVo getObjectInfo(String bucketKey, String objectKey) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        String bucketName = getBucketConfig(bucketKey).getBucketName();
        if (!getClient(bucketKey).doesObjectExist(bucketName, key)) {
            return null;
        }
        ObjectMetadata metadata = getClient(bucketKey).getObjectMetadata(bucketName, key);
        return toObjectVo(bucketKey, bucketName, key, metadata);
    }

    /**
     * 下载对象内容为字节数组。
     */
    public byte[] downloadBytes(String bucketKey, String objectKey) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        OSSObject ossObject = getClient(bucketKey).getObject(
                getBucketConfig(bucketKey).getBucketName(), key);
        try (InputStream inputStream = ossObject.getObjectContent()) {
            return readAllBytes(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("OSS 下载失败: bucketKey=" + bucketKey + ", objectKey=" + key, e);
        }
    }

    /**
     * 下载对象，封装为 {@link FileDownloadResult}。
     */
    public FileDownloadResult download(String bucketKey, String objectKey) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        OSSObject ossObject = getClient(bucketKey).getObject(
                getBucketConfig(bucketKey).getBucketName(), key);
        try (InputStream inputStream = ossObject.getObjectContent()) {
            ObjectMetadata metadata = ossObject.getObjectMetadata();
            return FileDownloadResult.builder()
                    .content(readAllBytes(inputStream))
                    .contentType(metadata.getContentType())
                    .fileName(extractFileName(key))
                    .httpStatus(200)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("OSS 下载失败: bucketKey=" + bucketKey + ", objectKey=" + key, e);
        }
    }

    /**
     * 按前缀列举对象。
     */
    public List<OssObjectVo> listObjects(String bucketKey, String prefix) {
        return listObjects(bucketKey, prefix, 1000);
    }

    public List<OssObjectVo> listObjects(String bucketKey, String prefix, int maxKeys) {
        String bucketName = getBucketConfig(bucketKey).getBucketName();
        ListObjectsV2Request request = new ListObjectsV2Request(bucketName);
        if (StringUtils.isNotBlank(prefix)) {
            request.withPrefix(normalizeObjectKey(prefix));
        }
        request.withMaxKeys(Math.max(1, Math.min(maxKeys, 1000)));

        List<OssObjectVo> results = new ArrayList<>();
        ListObjectsV2Result result;
        do {
            result = getClient(bucketKey).listObjectsV2(request);
            for (OSSObjectSummary summary : result.getObjectSummaries()) {
                results.add(OssObjectVo.builder()
                        .bucketKey(bucketKey)
                        .bucketName(bucketName)
                        .objectKey(summary.getKey())
                        .size(summary.getSize())
                        .etag(summary.getETag())
                        .lastModified(summary.getLastModified())
                        .build());
            }
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        return results;
    }

    // ======================== 更新（改） ========================

    /**
     * 同桶内复制对象（可用于覆盖更新）。
     */
    public void copyObject(String bucketKey, String sourceKey, String targetKey) {
        validateObjectKey(sourceKey);
        validateObjectKey(targetKey);
        String bucketName = getBucketConfig(bucketKey).getBucketName();
        CopyObjectRequest request = new CopyObjectRequest(
                bucketName, normalizeObjectKey(sourceKey),
                bucketName, normalizeObjectKey(targetKey));
        getClient(bucketKey).copyObject(request);
        log.info("OSS copy success: bucketKey={}, {} -> {}", bucketKey, sourceKey, targetKey);
    }

    /**
     * 跨配置桶复制对象（源桶与目标桶须属同一 OSS Client / Endpoint）。
     */
    public void copyObject(String sourceBucketKey, String sourceKey,
                           String targetBucketKey, String targetKey) {
        validateObjectKey(sourceKey);
        validateObjectKey(targetKey);
        String sourceBucketName = getBucketConfig(sourceBucketKey).getBucketName();
        String targetBucketName = getBucketConfig(targetBucketKey).getBucketName();
        CopyObjectRequest request = new CopyObjectRequest(
                sourceBucketName, normalizeObjectKey(sourceKey),
                targetBucketName, normalizeObjectKey(targetKey));
        getClient(sourceBucketKey).copyObject(request);
        log.info("OSS cross-bucket copy success: {}:{} -> {}:{}",
                sourceBucketKey, sourceKey, targetBucketKey, targetKey);
    }

    // ======================== 删除 ========================

    public void deleteObject(String bucketKey, String objectKey) {
        validateObjectKey(objectKey);
        getClient(bucketKey).deleteObject(
                getBucketConfig(bucketKey).getBucketName(), normalizeObjectKey(objectKey));
        log.info("OSS delete success: bucketKey={}, objectKey={}", bucketKey, objectKey);
    }

    public List<String> deleteObjects(String bucketKey, List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> normalizedKeys = new ArrayList<>(objectKeys.size());
        for (String objectKey : objectKeys) {
            validateObjectKey(objectKey);
            normalizedKeys.add(normalizeObjectKey(objectKey));
        }
        DeleteObjectsRequest request = new DeleteObjectsRequest(getBucketConfig(bucketKey).getBucketName())
                .withKeys(normalizedKeys)
                .withQuiet(false);
        DeleteObjectsResult result = getClient(bucketKey).deleteObjects(request);
        log.info("OSS batch delete success: bucketKey={}, count={}", bucketKey, normalizedKeys.size());
        return result.getDeletedObjects() == null ? new ArrayList<>() : result.getDeletedObjects();
    }

    // ======================== 预签名链接 ========================

    /**
     * 生成下载链接（Content-Disposition: attachment）。
     */
    public String generateDownloadUrl(String bucketKey, String objectKey) {
        return generateDownloadUrl(bucketKey, objectKey, 24 * 3600L, null);
    }

    public String generateDownloadUrl(String bucketKey, String objectKey, String fileName) {
        return generateDownloadUrl(bucketKey, objectKey, 24 * 3600L, fileName);
    }

    public String generateDownloadUrl(String bucketKey, String objectKey, Long expireSeconds, String fileName) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        String displayName = StringUtils.isNotBlank(fileName) ? fileName : extractFileName(key);
        OssProperties.BucketConfig config = getBucketConfig(bucketKey);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(), key, HttpMethod.GET);
        request.setExpiration(buildExpiration(config, expireSeconds));

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentDisposition(buildContentDisposition("attachment", displayName));
        request.setResponseHeaders(overrides);

        return toExternalUrl(getClient(bucketKey).generatePresignedUrl(request), config);
    }

    /**
     * 生成预览链接（Content-Disposition: inline，适用于浏览器在线预览）。
     */
    public String generatePreviewUrl(String bucketKey, String objectKey) {
        return generatePreviewUrl(bucketKey, objectKey, null, null);
    }

    public String generatePreviewUrl(String bucketKey, String objectKey, Long expireSeconds, String contentType) {
        validateObjectKey(objectKey);
        String key = normalizeObjectKey(objectKey);
        OssProperties.BucketConfig config = getBucketConfig(bucketKey);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                config.getBucketName(), key, HttpMethod.GET);
        request.setExpiration(buildExpiration(config, expireSeconds));

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentDisposition("inline");
        if (StringUtils.isNotBlank(contentType)) {
            overrides.setContentType(contentType);
        }
        request.setResponseHeaders(overrides);

        return toExternalUrl(getClient(bucketKey).generatePresignedUrl(request), config);
    }

    // ======================== 生命周期 ========================

    @PreDestroy
    public void destroy() {
        clients.forEach((key, client) -> {
            client.shutdown();
            log.info("OSS client 已关闭: {}", key);
        });
        clients.clear();
        bucketConfigs.clear();
    }

    // ======================== 内部方法 ========================

    private void registerBucket(String bucketKey, OssProperties.BucketConfig config) {
        validateBucketConfig(bucketKey, config);
        if (clients.containsKey(bucketKey)) {
            shutdownQuietly(clients.remove(bucketKey));
            bucketConfigs.remove(bucketKey);
        }

        OSS client = buildClient(config);
        if (config.isValidateOnStartup() && !client.doesBucketExist(config.getBucketName())) {
            client.shutdown();
            throw new IllegalStateException("OSS 桶不存在或不可访问: bucketKey="
                    + bucketKey + ", bucketName=" + config.getBucketName());
        }

        clients.put(bucketKey, client);
        bucketConfigs.put(bucketKey, config);
        log.info("OSS 桶已注册: bucketKey={}, bucketName={}", bucketKey, config.getBucketName());
    }

    private OSS buildClient(OssProperties.BucketConfig config) {
        if (StringUtils.isNotBlank(config.getSecurityToken())) {
            return new OSSClientBuilder().build(
                    config.getEndPoint(),
                    config.getAccessKeyId(),
                    config.getAccessKeySecret(),
                    config.getSecurityToken());
        }
        return new OSSClientBuilder().build(
                config.getEndPoint(),
                config.getAccessKeyId(),
                config.getAccessKeySecret());
    }

    private OSS getClient(String bucketKey) {
        OSS client = clients.get(bucketKey);
        if (client == null) {
            throw new IllegalArgumentException("未找到 OSS 桶配置: " + bucketKey);
        }
        return client;
    }

    private OssProperties.BucketConfig getBucketConfig(String bucketKey) {
        OssProperties.BucketConfig config = bucketConfigs.get(bucketKey);
        if (config == null) {
            throw new IllegalArgumentException("未找到 OSS 桶配置: " + bucketKey);
        }
        return config;
    }

    private void validateBucketConfig(String bucketKey, OssProperties.BucketConfig config) {
        if (StringUtils.isBlank(bucketKey)) {
            throw new IllegalArgumentException("bucketKey 不能为空");
        }
        if (config == null) {
            throw new IllegalArgumentException("OSS 桶配置不能为空: " + bucketKey);
        }
        if (StringUtils.isAnyBlank(config.getEndPoint(), config.getAccessKeyId(),
                config.getAccessKeySecret(), config.getBucketName())) {
            throw new IllegalArgumentException("OSS 桶配置不完整: " + bucketKey
                    + "，endpoint/accessKeyId/accessKeySecret/bucketName 均不能为空");
        }
    }

    private void validateObjectKey(String objectKey) {
        if (StringUtils.isBlank(objectKey)) {
            throw new IllegalArgumentException("objectKey 不能为空");
        }
        String normalized = normalizeObjectKey(objectKey);
        if (normalized.contains("..")) {
            throw new IllegalArgumentException("非法 objectKey: " + objectKey);
        }
    }

    private String normalizeObjectKey(String objectKey) {
        return objectKey.replace('\\', '/').replaceAll("^/+", "");
    }

    private Date buildExpiration(OssProperties.BucketConfig config, Long expireSeconds) {
        long seconds = expireSeconds != null && expireSeconds > 0
                ? expireSeconds
                : config.getDefaultUrlExpireSeconds();
        return new Date(System.currentTimeMillis() + seconds * 1000L);
    }

    private String buildContentDisposition(String dispositionType, String fileName) {
        try {
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            return dispositionType + ";filename=\"" + encoded + "\";filename*=UTF-8''" + encoded;
        } catch (UnsupportedEncodingException e) {
            return dispositionType + ";filename=\"" + fileName + "\"";
        }
    }

    private String toExternalUrl(URL url, OssProperties.BucketConfig config) {
        if (url == null) {
            return null;
        }
        if (StringUtils.isBlank(config.getCustomDomain())) {
            return url.toString();
        }
        String customDomain = config.getCustomDomain().trim();
        if (!customDomain.startsWith("http://") && !customDomain.startsWith("https://")) {
            customDomain = "https://" + customDomain;
        }
        if (customDomain.endsWith("/")) {
            customDomain = customDomain.substring(0, customDomain.length() - 1);
        }
        return customDomain + url.getFile();
    }

    private OssObjectVo toObjectVo(String bucketKey, String bucketName, String objectKey, ObjectMetadata metadata) {
        return OssObjectVo.builder()
                .bucketKey(bucketKey)
                .bucketName(bucketName)
                .objectKey(objectKey)
                .size(metadata.getContentLength())
                .etag(metadata.getETag())
                .lastModified(metadata.getLastModified())
                .contentType(metadata.getContentType())
                .build();
    }

    private String extractFileName(String objectKey) {
        String normalized = normalizeObjectKey(objectKey);
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    private void shutdownQuietly(OSS client) {
        if (client != null) {
            log.info("关闭oss");
            client.shutdown();
        }
    }
}
