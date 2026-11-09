package com.hd.model.vo;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import lombok.Data;

@Data
public class BrxtAttachmentVo extends SourceFileBaseVo {

    private Long id;

    private String bizType;

    private String archiveFilePath;

    private Integer archiveFileDone = 0;

    private Long archiveFileSize = 0L;

    private String fileId;

    private String fileName;

    private String filePath;

    private Long fileSize = 0L;

    private String ossFilePath = "";

    @Override
    public String sourceFileId() {
        return fileId;
    }

    @Override
    public String sourceFileName() {
        return fileName;
    }

    @Override
    public String bizName() {
        return bizType;
    }

    @Override
    public SourceFileDownloadParamsQo buildDownloadParamsQo() {
        return SourceFileDownloadParamsQo.builder().fileId(fileId)
                .build();
    }
}
