package com.hd.model.vo;

import java.util.Date;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import lombok.Data;

@Data
public class CXTZAttachmentVo extends SourceFileBaseVo {

    private String id;

    private String name;

    private Date createTime;

    private String url;

    @Override
    public String sourceFileId() {
        return id;
    }

    @Override
    public String sourceFileName() {
        return name;
    }

    @Override
    public String bizName() {
        return "";
    }

    @Override
    public SourceFileDownloadParamsQo buildDownloadParamsQo() {
        return SourceFileDownloadParamsQo.builder().
                fileId(id).build();
    }
}
