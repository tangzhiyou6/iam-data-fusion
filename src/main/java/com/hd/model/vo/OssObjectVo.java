package com.hd.model.vo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 对象元信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssObjectVo {

    /** 配置中的桶标识 */
    private String bucketKey;

    /** 实际 OSS 桶名 */
    private String bucketName;

    /** 对象 Key */
    private String objectKey;

    /** 文件大小（字节） */
    private long size;

    private String etag;

    private Date lastModified;

    private String contentType;
}
