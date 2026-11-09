package com.hd.service;

import java.util.List;

import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.SkAttachmentVo;

public interface SkFusionService {

    List<SkAttachmentVo> findAttachments(Long lastDataTime);

    FileDownloadResult freeDownload(String remoteRelativePath);
}
