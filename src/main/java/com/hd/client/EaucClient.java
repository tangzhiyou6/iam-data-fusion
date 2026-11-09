package com.hd.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hd.model.qo.SourceFileDownloadParamsQo;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.HttpResult;
import com.hd.utils.JsonUtils;
import com.hd.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 电能e拍（互联网） 文件下载客户端。
 * <p>
 * 对接规范：POST JSON 传 fileId、fileType；成功返回文件流，失败返回 JSON（header.msg）。
 */
@Slf4j
@Component
public class EaucClient implements AttaFileBaseClient {

    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("filename\\*?=(?:UTF-8''|\"?)([^\";\\n]+)", Pattern.CASE_INSENSITIVE);

    @Value("${file-sync.eauc.downloadUrl}")
    private String downloadUrl;

    public FileDownloadResult downloadAttaFile(SourceFileDownloadParamsQo paramsQo) {
        if (StringUtils.isBlank(paramsQo.getFileId()) || StringUtils.isBlank(paramsQo.getFileType())) {
            log.error("EAUC 文档下载, fileId 或 fileType 不能为空！");
            return null;
        }
        HttpResult<byte[]> httpResult = OkHttpUtils.builder().url(downloadUrl)
                .addParam("fileId", paramsQo.getFileId())
                .addParam("fileType", paramsQo.getFileType())
                .get()
                .sync(byte[].class);
        if (null == httpResult) {
            log.error("EAUC 文档下载报错,错误信息：结果为空");
            return null;
        }
        if (!httpResult.isSuccess()) {
            log.error("EAUC 文档下载报错,错误信息：{}", parseErrorMessage(httpResult.getRawBody()));
            return null;
        }
        if (httpResult.isJsonResponse()) {
            log.error("EAUC 文档下载报错,错误信息：{}", StringUtils.defaultIfBlank(parseErrorMessage(httpResult.getRawBody()), "文件下载失败"));
            return null;
        }
        try {
            return handleDownloadResponse(httpResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private FileDownloadResult handleDownloadResponse(HttpResult<byte[]> result) {
        byte[] content = result.getBody();
        if (content == null || content.length == 0) {
            throw new RuntimeException("文件下载成功但内容为空:" + result.getHttpStatus());
        }
        String fileName = extractFileName(result.getHeaders().get("Content-Disposition"));
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
        } catch (Exception e) {
            log.warn("解析 EAUC 文件下载错误响应失败: {}", rawBody, e);
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
}
