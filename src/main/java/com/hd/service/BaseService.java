package com.hd.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hd.client.AttaFileBaseClient;
import com.hd.constants.FusionSysEnum;
import com.hd.model.vo.DataFusionBaseVo;
import com.hd.model.vo.SourceFileBaseVo;

public interface BaseService<T> extends IService<T> {

    Integer parseToStructured(List<? extends SourceFileBaseVo> sourceFileBaseVos, AttaFileBaseClient attaDownloadClient, FusionSysEnum bizNameEnum, String dataType, TypeReference<? extends DataFusionBaseVo> type);
}
