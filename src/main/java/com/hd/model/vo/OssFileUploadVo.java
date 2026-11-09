package com.hd.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssFileUploadVo {

    private String fileId;

    private String fileName;

    private byte[] content;

    private String recordFileName;

    private String ossKey;

    private String ossObjectId;
}
