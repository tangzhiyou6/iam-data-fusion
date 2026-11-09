package com.hd.model.vo;

import lombok.Builder;
import lombok.Data;
/**
 * 文件下载成功结果。
 */
@Data
@Builder
public class FileDownloadResult {
    /** 文件二进制内容 */
    private byte[] content;
	
    /** Content-Type */
    private String contentType;
	
    /** 文件名（来自 Content-Disposition） */
    private String fileName;

    /** HTTP 状态码 */
    private int httpStatus;

    private String errorMsg;

    private AutoCleanFile autoCleanFile;

    private String ossKey;

    private String ossObjectId;
}