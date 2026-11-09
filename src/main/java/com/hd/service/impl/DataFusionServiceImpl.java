package com.hd.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hd.client.AttaFileBaseClient;
import com.hd.client.FastGptClient;
import com.hd.constants.DataBusinessCodeEnum;
import com.hd.constants.DataFusionEnum;
import com.hd.constants.FusionSysEnum;
import com.hd.model.po.SyncRecordPo;
import com.hd.model.vo.ArchiveExtractorVo;
import com.hd.model.vo.DataFusionBaseVo;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.OssFileUploadVo;
import com.hd.model.vo.SourceFileBaseVo;
import com.hd.service.DataFusionService;
import com.hd.service.SyncRecordService;
import com.hd.utils.ArchiveFileUtils;
import com.hd.utils.JsonExtractUtils;
import com.hd.utils.JsonUtils;
import com.hd.utils.OssUtils;
import com.hd.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class DataFusionServiceImpl implements DataFusionService {

    private static final Pattern JSON_EXTR_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");

    @Autowired
    private SyncRecordService syncRecordService;

    @Autowired
    private OssUtils ossUtils;

    @Autowired
    private FastGptClient fastGptClient;

    /**
     * 将源附件解析为结构化数据。
     * <p>
     * 按文件独立处理（本地下载/OSS/大模型/落库互不共享可变状态），
     * 可对同一 Service 实例并发调用；调用方可按附件拆单后用全局线程池并行提交。
     * </p>
     */
    //不做批量存储，可以确保任务随时可停
    public Integer parseToStructured(List<? extends SourceFileBaseVo> sourceFileBaseVos,
                                     AttaFileBaseClient attaDownloadClient, Map<String, DataFusionEnum> fusionMap
            , boolean archiveFileBatch) {
        Integer succCount = 0;
        Integer errCount = 0;
        FileDownloadResult fileDownloadResult;
        DataFusionEnum dataFusionEnnum = fusionMap.values().iterator().next();
        for (SourceFileBaseVo fileBaseVo : sourceFileBaseVos) {
            String businessCode = "0";
            String sysName = dataFusionEnnum.getBizNameEnum().getSysName();
            String bizName = dataFusionEnnum.getBizName();
            FusionSysEnum sysEnum = dataFusionEnnum.getBizNameEnum();
            DataBusinessCodeEnum businessCodeEnum = DataBusinessCodeEnum.findBySysAndBizName(dataFusionEnnum.getBizNameEnum(), fileBaseVo.bizName());
            if (businessCodeEnum != null) {
                businessCode = businessCodeEnum.getBusinessCode();
                bizName = businessCodeEnum.getBizName();
            }
            String s = fileBaseVo.bizName();
            SyncRecordPo recordPo = new SyncRecordPo(sysName, bizName, fileBaseVo.sourceFileId(), fileBaseVo.sourceFileName());
            try {
                fileDownloadResult = attaDownloadClient.downloadAttaFile(fileBaseVo.buildDownloadParamsQo());
                if (null == fileDownloadResult) {
                    log.error("{} - {} - 文件：{} 下载请求报错！", sysName, bizName, fileBaseVo.sourceFileId());
                    errCount ++;
                    recordPo.setFailReason(bizName + "下载请求报错");
                    syncRecordService.save(recordPo);
                    continue;
                }

                if (fileDownloadResult.getHttpStatus() != 200) {
                    errCount ++;
                    recordPo.setFailReason(bizName + " - 文件下载失败，status:" + fileDownloadResult.getHttpStatus());
                    syncRecordService.save(recordPo);
                    continue;
                }

                if (ArchiveFileUtils.isArchiveFile(fileDownloadResult.getFileName())) {
                    List<ArchiveExtractorVo> extract = ArchiveFileUtils.extract(fileDownloadResult.getContent(), fileDownloadResult.getFileName());
                    if (archiveFileBatch) {
                        recordPo = new SyncRecordPo(sysName, bizName, fileBaseVo.sourceFileId(), fileBaseVo.sourceFileName());
                        List<OssFileUploadVo> fileList = new ArrayList<>();
                        for (ArchiveExtractorVo extractorVo : extract) {
                            OssFileUploadVo fileItem = new OssFileUploadVo(fileBaseVo.sourceFileId(), extractorVo.getFileName(), extractorVo.getContent(),
                                    fileBaseVo.sourceFileId(), fileDownloadResult.getOssKey(), fileDownloadResult.getOssObjectId());
                            fileList.add(fileItem);
                            Integer result = parseFileToStructured(fileList, recordPo, fusionMap,sysEnum, businessCode);
                            if (result == 1) {
                                succCount++;
                            } else {
                                errCount ++;
                            }
                        }
                    } else {
                        for (ArchiveExtractorVo extractorVo : extract) {
                            recordPo = new SyncRecordPo(sysName, bizName, fileBaseVo.sourceFileId(), fileBaseVo.sourceFileName());
                            Integer result = parseFileToStructured(Collections.singletonList(new OssFileUploadVo(fileBaseVo.sourceFileId(), extractorVo.getFileName(),
                                            extractorVo.getContent(), fileBaseVo.sourceFileId(), fileDownloadResult.getOssKey(), fileDownloadResult.getOssObjectId())),
                                    recordPo, fusionMap, sysEnum, businessCode);
                            if (result == 1) {
                                succCount++;
                            } else {
                                errCount ++;
                            }
                        }
                    }
                } else {
                    List<OssFileUploadVo> ossFileUploadVos = Collections.singletonList(new OssFileUploadVo(fileBaseVo.sourceFileId(), fileBaseVo.sourceFileName(),
                            fileDownloadResult.getContent(), fileBaseVo.sourceFileName(), fileDownloadResult.getOssKey(), fileDownloadResult.getOssObjectId()));
                    Integer result = parseFileToStructured(ossFileUploadVos, recordPo, fusionMap,sysEnum, businessCode);
                    if (result == 1) {
                        succCount++;
                    } else {
                        errCount ++;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errCount ++;
                recordPo.setFailReason(bizName + " - 文件下载失败，" + e.getMessage());
                syncRecordService.save(recordPo);
            }
        }
        log.error("{} - 本批次：{}条数据，成功：{}, 失败：{}", dataFusionEnnum.getBizNameEnum().getSysName(), sourceFileBaseVos.size(), succCount, errCount);
        return succCount;
    }

    private Integer parseFileToStructured(List<OssFileUploadVo> fileUploadVos, SyncRecordPo recordPo, Map<String, DataFusionEnum> fusionMap,
                                          FusionSysEnum bizNameEnum, String businessCode) {
        try {
            List<String> fileOssPaths = new ArrayList<>();
            for (OssFileUploadVo fileUploadVo : fileUploadVos) {
                //上传OSS，并获取oss的下载地址
                String objectKey;
                if (StringUtils.isEmpty(fileUploadVo.getOssObjectId())) {
                    objectKey = ossUtils.buildObjectKey(fileUploadVo.getFileId(), fileUploadVo.getFileName(), bizNameEnum);
                    if (!ossUtils.existsObject(ossUtils.defaultBucketKey(), objectKey)) {
                        log.info("开始上传文件：{}, 文件大小：{}", fileUploadVo.getFileName(), fileUploadVo.getContent().length);
                        ossUtils.upload(ossUtils.defaultBucketKey(), objectKey, fileUploadVo.getContent());
                    }
                } else {
                    objectKey = fileUploadVo.getOssObjectId();
                }
                String downloadUrl = ossUtils.generateDownloadUrl(StringUtils.isEmpty(fileUploadVo.getOssKey()) ? ossUtils.defaultBucketKey() : fileUploadVo.getOssKey(),
                        objectKey, fileUploadVo.getFileName());
                recordPo.addUploadInfos(objectKey, Objects.isNull(fileUploadVo.getContent()) ? 0 : fileUploadVo.getContent().length);
                fileOssPaths.add(downloadUrl);
            }
            //调用大模型解析
            Map<String, String> chatResultMap = fastGptClient.chat(businessCode, fileOssPaths, fusionMap.keySet().toArray(new String[0]));
            if (CollectionUtils.isEmpty(chatResultMap)) {
                log.error("{} - 文件：{} 请求大模型获取结果为 NULL！", bizNameEnum.getSysName(), fileOssPaths);
                recordPo.setFailReason("请求大模型获取结果为 NULL");
                syncRecordService.save(recordPo);
                return 0;
            }
            String fileId = fileUploadVos.get(0).getFileId();
            String scene = chatResultMap.remove("scene");
            chatResultMap.entrySet().stream().forEach(i -> {
                DataFusionEnum dataFusionEnnum1 = fusionMap.get(i.getKey());
                if (null == dataFusionEnnum1) {
                    return;
                }
                String resultJson = StringUtils.isEmpty(i.getValue()) ? null : JsonExtractUtils.extract(i.getValue());
                if (null != resultJson && resultJson.length() > 2) {
                    DataFusionBaseVo reportVo = JsonUtils.parse(resultJson, dataFusionEnnum1.getTypeRef());
                    List list = reportVo.buildPo(fileId, scene);
                    ServiceImpl bean = SpringUtils.getBean(dataFusionEnnum1.getServiceClazz());
                    bean.saveBatch(list);
                }
            });
            syncRecordService.save(recordPo);
            return 1;
        } catch (Exception e) {
            if (StringUtils.isEmpty(recordPo.getOssFilePath())) {
                recordPo.setOssFilePath("-");
            }
            log.error(e.getMessage(), e);
            recordPo.setFailReason("文件下载或解析失败，" + e.getMessage());
            syncRecordService.save(recordPo);
            return 0;
        }
    }
}
