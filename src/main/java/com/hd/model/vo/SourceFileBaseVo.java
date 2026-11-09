package com.hd.model.vo;

import com.hd.model.qo.SourceFileDownloadParamsQo;

public abstract class SourceFileBaseVo {

    public abstract String sourceFileId();

    public abstract String sourceFileName();

    public abstract String bizName();

    public abstract SourceFileDownloadParamsQo buildDownloadParamsQo();
}
