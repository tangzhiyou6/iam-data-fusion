package com.hd.client;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.HttpResult;
import com.hd.model.vo.RestResult;
import com.hd.utils.JsonUtils;
import com.hd.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 电投e购（DNYG）文档下载客户端。
 * <p>
 * 通过 inv_url 完整地址直接下载文件，成功返回文件流，失败返回 JSON 错误信息。
 */
@Slf4j
@Component
public class DnygClient {

    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("filename\\*?=(?:UTF-8''|\"?)([^\";\\n]+)", Pattern.CASE_INSENSITIVE);

    public FileDownloadResult download(String invUrl) {
        if (StringUtils.isBlank(invUrl)) {
            log.error("电投e购文档下载, invUrl 不能为空！");
            return null;
        }
        HttpResult<byte[]> httpResult = OkHttpUtils.builder()
                .url(invUrl.trim())
                .get()
                .sync(byte[].class);
        if (null == httpResult) {
            log.error("电投e购 文档下载报错,错误信息：结果为空");
            return null;
        }
        if (!httpResult.isSuccess()) {
            log.error("电投e购 文档下载报错,错误信息：{}", parseErrorMessage(httpResult.getRawBody()));
            return null;
        }
        if (httpResult.isJsonResponse()) {
            log.error("电投e购 文档下载报错,错误信息：{}", StringUtils.defaultIfBlank(parseErrorMessage(httpResult.getRawBody()), "文件下载失败"));
            return null;
        }
        try {
            return handleDownloadResponse(httpResult, invUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private FileDownloadResult handleDownloadResponse(HttpResult<byte[]> result, String invUrl) {
        byte[] content = result.getBody();
        if (content == null || content.length == 0) {
            throw new RuntimeException("文件下载成功但内容为空:" + result.getHttpStatus());
        }
        String fileName = extractFileName(result.getHeaders().get("Content-Disposition"));
        if (StringUtils.isBlank(fileName)) {
            fileName = extractFileNameFromUrl(invUrl);
        }
        return FileDownloadResult.builder()
                .content(content)
                .contentType(result.getContentType())
                .fileName(fileName)
                .httpStatus(result.getHttpStatus())
                .build();
    }

    private String parseErrorMessage(String rawBody) {
        if (StringUtils.isBlank(rawBody)) {
            return "文件下载失败，未返回错误详情";
        }
        try {
            Map<String, Object> root = JsonUtils.parse(rawBody, new TypeReference<Map<String, Object>>() {});
            if (root != null && root.get("header") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> header = (Map<String, Object>) root.get("header");
                Object msg = header.get("msg");
                if (msg != null && StringUtils.isNotBlank(msg.toString())) {
                    return msg.toString();
                }
            }
            RestResult error = JsonUtils.parse(rawBody, RestResult.class);
            if (error != null && StringUtils.isNotBlank(error.getMsg())) {
                return error.getMsg();
            }
        } catch (Exception e) {
            log.warn("解析 DNYG 文件下载错误响应失败: {}", rawBody, e);
        }
        return rawBody;
    }

    private String extractFileName(String contentDisposition) {
        if (StringUtils.isBlank(contentDisposition)) {
            return null;
        }
        Matcher matcher = FILENAME_PATTERN.matcher(contentDisposition);
        if (matcher.find()) {
            try {
                return URLDecoder.decode(matcher.group(1).trim(), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    private String extractFileNameFromUrl(String invUrl) {
        try {
            String path = new URL(invUrl.trim()).getPath();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                return URLDecoder.decode(path.substring(lastSlash + 1), StandardCharsets.UTF_8.name());
            }
        } catch (Exception e) {
            log.warn("从 invUrl 解析文件名失败: {}", invUrl, e);
        }
        return null;
    }
}
