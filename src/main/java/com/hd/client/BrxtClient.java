package com.hd.client;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hd.mapper.BrxtFileUploadRecordMapper;
import com.hd.model.po.BrxtFileUploadRecordPo;
import com.hd.model.qo.SourceFileDownloadParamsQo;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.HttpResult;
import com.hd.model.vo.RestResult;
import com.hd.utils.JsonUtils;
import com.hd.utils.Md5Utils;
import com.hd.utils.OkHttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * GDTBack（百瑞信托） 文件安全下载客户端。
 * <p>
 * 对接规范：通过 Content-Type 区分文件流与 JSON 错误信息。
 */
@Slf4j
@Component
public class BrxtClient implements AttaFileBaseClient {

    @Value("${file-sync.gdtback.free-download}")
    private String freeDownloadUrl;

    @Value("${file-sync.gdtback.secret}")
    private String secret;

    @Value("${file-sync.gdtback.username}")
    private String username;

    @Value("${file-sync.gdtback.password}")
    private String password;

    @Autowired
    private BrxtFileUploadRecordMapper brxtFileUploadRecordMapper;

    @Override
    public FileDownloadResult downloadAttaFile(SourceFileDownloadParamsQo paramsQo) {
        LambdaQueryWrapper<BrxtFileUploadRecordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrxtFileUploadRecordPo::getFileId, paramsQo.getFileId());
        List<BrxtFileUploadRecordPo> brxtFileUploadRecordPos = brxtFileUploadRecordMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(brxtFileUploadRecordPos)) {
            return FileDownloadResult.builder().
                    fileName(paramsQo.getFileId()).httpStatus(70000).build();
        }
        BrxtFileUploadRecordPo brxtFileUploadRecordPo = brxtFileUploadRecordPos.get(0);
        return FileDownloadResult.builder().
                fileName(brxtFileUploadRecordPo.getFileName())
                .ossObjectId(brxtFileUploadRecordPo.getOssFilePath())
                .httpStatus(200).build();
    }















    public FileDownloadResult freeDownload(String fileId) {
        try {
            if (StringUtils.isBlank(fileId)) {
                throw new IllegalArgumentException("fileId 不能为空");
            }
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            HttpResult<byte[]> httpResult = OkHttpUtils.builder().url(freeDownloadUrl)
                    .addParam("fileId", fileId)
                    .addParam("timestamp", timestamp)
                    .addParam("sign", buildSign(fileId, timestamp))
                    .addParam("username", username)
                    .addParam("password", password)
                    .post(true)
                    .sync(byte[].class);
            if (null == httpResult) {
                log.error("EAUC 文档下载报错,错误信息：结果为空");
                return null;
            }
            if (!httpResult.isSuccess()) {
                log.error("EAUC 文档下载报错,错误信息：{}", httpResult.getRawBody());
                return null;
            }
            if (httpResult.isJsonResponse()) {
                log.error("EAUC 文档下载报错,错误信息：{}", StringUtils.defaultIfBlank(httpResult.getRawBody(), "文件下载失败"));
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
        if (content == null || (content.length == 0 && result.getAutoCleanFile() == null)) {
            throw new RuntimeException("文件下载成功但内容为空:" + result.getHttpStatus());
        }
        String fileName = OkHttpUtils.extractFileName(result.getHeaders());
        return FileDownloadResult.builder()
                .content(content)
                .contentType(result.getContentType())
                .fileName(fileName)
                .httpStatus(result.getHttpStatus())
                .autoCleanFile(result.getAutoCleanFile())
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

}
