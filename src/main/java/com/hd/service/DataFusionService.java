package com.hd.service;

import java.util.List;
import java.util.Map;

import com.hd.client.AttaFileBaseClient;
import com.hd.constants.DataFusionEnum;
import com.hd.model.vo.SourceFileBaseVo;

public interface DataFusionService {

    Integer parseToStructured(List<? extends SourceFileBaseVo> sourceFileBaseVos,
                              AttaFileBaseClient attaDownloadClient, Map<String, DataFusionEnum> fusionMap, boolean archiveFileBatch);
}
