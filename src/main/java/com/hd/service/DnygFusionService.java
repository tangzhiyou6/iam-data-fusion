package com.hd.service;

import java.util.List;

import com.hd.model.vo.DnygAttachmentVo;

public interface DnygFusionService {
    List<DnygAttachmentVo> findAttachments(Long lastDataTime);
}
