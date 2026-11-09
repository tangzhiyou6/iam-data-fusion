package com.hd.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.HttpResult;
import com.hd.model.vo.RestResult;
import com.hd.utils.JsonUtils;
import com.hd.utils.Md5Utils;
import com.hd.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 创新投资 文件安全下载客户端。
 * <p>
 * 对接规范：通过 Content-Type 区分文件流与 JSON 错误信息。
 */
@Slf4j
@Component
public class CXTZClient implements AttaFileBaseClient {

    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("filename\\*?=(?:UTF-8''|\"?)([^\";\\n]+)", Pattern.CASE_INSENSITIVE);

    @Value("${file-sync.cxtz.free-download}")
    private String freeDownloadUrl;

    @Value("${file-sync.cxtz.secret}")
    private String secret;

    @Override
    public FileDownloadResult downloadAttaFile(SourceFileDownloadParamsQo paramsQo) {
        try {
            if (StringUtils.isBlank(paramsQo.getFileId())) {
                throw new IllegalArgumentException("fileId 不能为空");
            }
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            HttpResult<byte[]> httpResult = OkHttpUtils.builder().url(freeDownloadUrl)
                    .addParam("fileId", paramsQo.getFileId())
                    .addParam("timestamp", timestamp)
                    .addParam("sign", buildSign(paramsQo.getFileId(), timestamp))
                    .post(true)
                    .sync(byte[].class);
            if (null == httpResult) {
                log.error("创新投资 文档下载报错,错误信息：结果为空");
                return null;
            }
            if (!httpResult.isSuccess()) {
                log.error("创新投资 文档下载报错,错误信息：{}", httpResult.getRawBody());
                return null;
            }
            if (httpResult.isJsonResponse()) {
                log.error("创新投资 文档下载报错,错误信息：{}", StringUtils.defaultIfBlank(httpResult.getRawBody(), "文件下载失败"));
                return null;
            }
            return handleDownloadResponse(httpResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String buildSign(String fileId, String timestamp) {
        return Md5Utils.md5(fileId + timestamp + secret);
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

    private RestResult parseError(String rawBody) {
        if (StringUtils.isBlank(rawBody)) {
            return RestResult.error("文件下载失败，未返回错误详情");
        }
        try {
            return JsonUtils.parse(rawBody, RestResult.class);
        } catch (Exception e) {
            log.warn("解析文件下载错误响应失败: {}", rawBody, e);
            return RestResult.error(rawBody);
        }
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
