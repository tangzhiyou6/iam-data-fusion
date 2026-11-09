package com.hd.client;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.HttpResult;
import com.hd.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 以上是司库系统的非结构化对接的文档，需要先在我方测试环境进行调试（10.104.33.178、10.104.39.157），调试通过后再切到生产
 * 审计方的测试环境10.104.33.178、10.104.39.157   访问对方测试网地址：10.70.163.201:28001
 *
 * 审计方生产环境10.104.21.1、10.104.21.4
 * 访问对方的（三个任选其一）
 * 生产广域网地址：10.70.163.101:17004
 * 生产广域网地址：10.70.163.101:17005
 * 生产广域网地址：10.70.163.101:17006
 *
 * */
@Slf4j
@Component
public class SkClient {

    @Value("${file-sync.sk.baseUrl}")
    private String baseUrl;

    @Value("${file-sync.sk.username}")
    private String username;

    @Value("${file-sync.sk.password}")
    private String password;

    public FileDownloadResult freeDownload(String remoteRelativePath) {
        log.info("remoteRelativePath:{}", remoteRelativePath);
        if (StringUtils.isBlank(remoteRelativePath)) {
            return FileDownloadResult.builder().httpStatus(5002).errorMsg("remoteRelativePath 不能为空").build();
        }
        try {
            String encodedPath = URLEncoder.encode(remoteRelativePath, "UTF-8");
            String fullUrl = baseUrl + "/attachment/download?url=" + encodedPath;
            String credentials = username + ":" + password;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            HttpResult<byte[]> httpResult = OkHttpUtils.builder()
                    .url(fullUrl)
                    .addHeader("Authorization", basicAuth)
                    .get().sync(byte[].class);
            if (null == httpResult) {
                return FileDownloadResult.builder().httpStatus(5003).errorMsg("司库 文档下载报错,错误信息：结果为空").build();
            }
            if (!httpResult.isSuccess()) {
                log.error("司库 文档下载报错,错误信息：{}", httpResult.getRawBody());
                // 根据状态码抛出具体异常
                int httpStatus = httpResult.getHttpStatus();
                try {
                    switch (httpStatus) {
                        case 400: return FileDownloadResult.builder().httpStatus(httpStatus).errorMsg("请求参数非法: " + remoteRelativePath).build();
                        case 401: return FileDownloadResult.builder().httpStatus(httpStatus).errorMsg("认证失败").build();
                        case 403: return FileDownloadResult.builder().httpStatus(httpStatus).errorMsg("访问被拒绝").build();
                        case 404: return FileDownloadResult.builder().httpStatus(httpStatus).errorMsg("文件不存在: ").build();
                        default: return FileDownloadResult.builder().httpStatus(httpStatus).errorMsg("服务器错误: ").build();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return FileDownloadResult.builder().httpStatus(5001).errorMsg(e.getMessage()).build();
                }
            }
            if (httpResult.isJsonResponse()) {
                log.error("司库 文档下载报错,错误信息：{}", StringUtils.defaultIfBlank(httpResult.getRawBody(), "文件下载失败"));
                return FileDownloadResult.builder().httpStatus(5004).errorMsg(StringUtils.defaultIfBlank(httpResult.getRawBody(), "文件下载失败")).build();
            }
            return handleDownloadResponse(httpResult, remoteRelativePath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return FileDownloadResult.builder().httpStatus(5005).errorMsg(e.getMessage()).build();
        }
    }

    private FileDownloadResult handleDownloadResponse(HttpResult<byte[]> result, String remoteRelativePath) {
        byte[] content = result.getBody();
        if (content == null || content.length == 0) {
            throw new RuntimeException("文件下载成功但内容为空:" + result.getHttpStatus());
        }
        String fileName = parseFileName(result.getHeaders().get("Content-Disposition"));
        if (fileName == null || fileName.isEmpty()) {
            int slashIdx = remoteRelativePath.lastIndexOf('/');
            fileName = (slashIdx >= 0) ? remoteRelativePath.substring(slashIdx + 1) : remoteRelativePath;
        }
        return FileDownloadResult.builder()
                .content(content)
                .contentType(result.getContentType())
                .fileName(fileName)
                .httpStatus(result.getHttpStatus())
                .build();
    }

    private static String parseFileName(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isEmpty()) {
            return null;
        }
        try {
            // 优先解析 RFC 5987 格式 filename*=UTF-8''xxx
            int starIdx = contentDisposition.toLowerCase().indexOf("filename*=");
            if (starIdx >= 0) {
                String value = contentDisposition.substring(starIdx + "filename*=".length()).trim();
                // 去除可能的尾部分号
                int semi = value.indexOf(';');
                if (semi >= 0) {
                    value = value.substring(0, semi);
                }
                // 形如 UTF-8''%E6%8A%A5%E8%A1%A8.pdf
                int quoteIdx = value.indexOf("''");
                if (quoteIdx >= 0) {
                    String charset = value.substring(0, quoteIdx);
                    String encoded = value.substring(quoteIdx + 2);
                    return URLDecoder.decode(encoded, charset.isEmpty() ? "UTF-8" : charset);
                }
            }

            // 回退解析 filename="xxx" 或 filename=xxx
            int idx = contentDisposition.toLowerCase().indexOf("filename=");
            if (idx >= 0) {
                String value = contentDisposition.substring(idx + "filename=".length()).trim();
                int semi = value.indexOf(';');
                if (semi >= 0) {
                    value = value.substring(0, semi);
                }
                // 去除两端引号
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (Exception ignored) {
            // 解析失败则返回 null，由调用方使用兜底文件名
        }
        return null;
    }
}
