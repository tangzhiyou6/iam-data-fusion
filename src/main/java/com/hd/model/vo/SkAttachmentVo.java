package com.hd.model.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import lombok.Data;

@Data
public class SkAttachmentVo extends SourceFileBaseVo implements Serializable {

    private String id;

    private String fileName;

    private String fileType;

    private String fileTypeName;

    private String filePath;

    private LocalDateTime uploadTime;

    @Override
    public String sourceFileId() {
        return this.id;
    }

    @Override
    public String sourceFileName() {
        return this.fileName;
    }

    @Override
    public String bizName() {
        return "";
    }

    @Override
    public SourceFileDownloadParamsQo buildDownloadParamsQo() {
        return null;
    }
}

