package com.hd.client;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import com.hd.model.vo.FileDownloadResult;

public interface AttaFileBaseClient {

    FileDownloadResult downloadAttaFile(SourceFileDownloadParamsQo paramsQo);
}
