package com.hd.scheduler;

import static com.hd.constants.DataFusionEnum.BRXT_BALANCE_SHEET;
import static com.hd.constants.DataFusionEnum.BRXT_BRXT_TT_DUE_DIG_REPORT;
import static com.hd.constants.DataFusionEnum.BRXT_CASH_FLOW_STATEMENT;
import static com.hd.constants.DataFusionEnum.BRXT_INCOME_STATEMENT;
import static com.hd.constants.DataFusionEnum.BRXT_INFORMATION_DISCLOSURE_REPORT;
import static com.hd.constants.DataFusionEnum.BRXT_INVESTMENT_AGREEMENT_CONTRACT_ENTITY;
import static com.hd.constants.DataFusionEnum.BRXT_INVESTMENT_AGREEMENT_CONTRACT_INFO;
import static com.hd.constants.DataFusionEnum.BRXT_INVESTMENT_AGREEMENT_CONTRACT_RELA;
import static com.hd.constants.DataFusionEnum.BRXT_MAJOR_RISK_REPORT;
import static com.hd.constants.DataFusionEnum.BRXT_MAJOR_RISK_WARNNING;
import static com.hd.constants.DataFusionEnum.BRXT_OWN_BIS_DUE_DIG_REPORT;
import static com.hd.constants.DataFusionEnum.BRXT_PAYMENT_ADVICE;
import static com.hd.constants.DataFusionEnum.BRXT_PERPETUAL_BOND;
import static com.hd.constants.DataFusionEnum.BRXT_PROJECT_DECISION_COMMITTEE;
import static com.hd.constants.DataFusionEnum.BRXT_PROJECT_REVIEW_COMMENT;
import static com.hd.constants.DataFusionEnum.BRXT_REQUEST_REPORT;
import static com.hd.constants.DataFusionEnum.BRXT_TRUST_POST_INVESTMENT_REPORT;
import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hd.client.BrxtClient;
import com.hd.config.GlobalThreadPoolConfig;
import com.hd.constants.DataBusinessCodeEnum;
import com.hd.constants.DataFusionEnum;
import com.hd.constants.FusionSysEnum;
import com.hd.mapper.BrxtFileUploadRecordMapper;
import com.hd.model.po.BrxtFileUploadRecordPo;
import com.hd.model.vo.ArchiveExtractorVo;
import com.hd.model.vo.BrxtAttachmentVo;
import com.hd.service.DataFusionService;
import com.hd.utils.ArchiveFileUtils;
import com.hd.utils.BigArchiveFileUtils;
import com.hd.utils.DateUtils;
import com.hd.utils.HdFileUtils;
import com.hd.utils.JdbcPoolUtils;
import com.hd.utils.Md5Utils;
import com.hd.utils.OssUtils;
import com.hd.utils.ParallelTaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * 1、文件下载：增量下载，安照百瑞信托压缩包文件结构，将文件下载到 localPathBase 对应的目录下。每次有文件新增，删除对应业务目录在的 SUCCESS_FILE_NAME
 * 2、定时上传，按现有上传流程，无需再修改（上传完成后，写入 SUCCESS_FILE_NAME 文件）
 * 3、定时解析
 * */

@Slf4j
@Service
@RequiredArgsConstructor
public class BrxtScheduler {

    private final JdbcPoolUtils jdbcPoolUtils;

    private static final long MAX_ARCHIVE_FILE_SIZE_GB = 1L * 1024 * 1024 * 1024;
    private static final String SUCCESS_FILE_NAME = "SUCCESS.SUCS";

    @Autowired
    private BrxtFileUploadRecordMapper brxtFileUploadRecordMapper;

    @Autowired
    private DataFusionService dataFusionService;

    @Autowired
    private BrxtClient brxtClient;

    @Autowired
    private OssUtils ossUtils;

    @Resource(name = GlobalThreadPoolConfig.GLOBAL_TASK_EXECUTOR)
    private ThreadPoolTaskExecutor globalTaskExecutor;

    @Value("${file-sync.gdtback.local-path-base}")
    private String localPathBase;

    @Value("${file-sync.gdtback.attachment_table}")
    private String brxtAttachmentInfoTable;

    private static Boolean enableJob = true;

    //上传调度：
    //@Scheduled(fixedDelay = 60 * 60 * 1000)
    public void localFileUploadScheduled() {
        if (!enableJob) {
            return;
        }
        File file = new File(localPathBase);
        if (!file.exists()) {
            enableJob = false;
            return;
        }
        File[] listFiles = file.listFiles();
        if (null == listFiles || listFiles.length < 1) {
            enableJob = false;
            return;
        }
        List<DataBusinessCodeEnum> brxtFileEnum = DataBusinessCodeEnum.findBySys(FusionSysEnum.BRXT);
        for (DataBusinessCodeEnum businessCodeEnum : brxtFileEnum) {
            if (StringUtils.isEmpty(businessCodeEnum.getBusinessCode())) {
                continue;
            }
            String localPath = businessCodeEnum.getLocalPath();
            String baseFolderPath = localPathBase + File.separator + localPath;
            uploadLocalFile2Oss(businessCodeEnum, baseFolderPath);
        }
    }

    // @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void extractStructuredData() {
        Integer totalCount = 0;
        Integer succCount = 0;
        List<BrxtAttachmentVo> attachments = findAttachments();
        while (attachments.size() > 0) {
            Long totalNum = countByDataType();
            log.info("获取[百瑞信托]-数据，剩余 {} 条，开始提取。", totalNum);
            totalCount += attachments.size();
            succCount += parseToStructured(attachments);
            attachments = findAttachments();
        }
        log.info("[百瑞信托] - {} 同步完成：共 {}条数据，成功：{}", DateUtils.getStandardDateTime(null), totalCount, succCount);
    }

    private Integer parseToStructured(List<BrxtAttachmentVo> attachments) {
        Map<String, DataFusionEnum> fusionMap = new HashMap<>();
        //项目评审意见书（固有 + 信托）
        fusionMap.put("project_review_opinion", BRXT_PROJECT_REVIEW_COMMENT);
        //重大风险报告单（固有 + 信托）
        fusionMap.put("major_risk", BRXT_MAJOR_RISK_REPORT);
        //项目预警信号报告单（固有 + 信托）
        fusionMap.put("project_alert", BRXT_MAJOR_RISK_WARNNING);
        //项目决策委员会会议纪要（固有 + 信托）
        fusionMap.put("decision_making_meeting", BRXT_PROJECT_DECISION_COMMITTEE);
        //信托业务尽职调查报告（信托）
        fusionMap.put("due_diligence", BRXT_BRXT_TT_DUE_DIG_REPORT);

        //固有业务尽调报告（固有）
        fusionMap.put("gy_due_diligence", BRXT_OWN_BIS_DUE_DIG_REPORT);
        //保管协议（固有 + 信托）
        // fusionMap.put("storage_agreement", BRXT_CUSTODY_AGREEMENT);
        //信托合同（固有 + 信托）
        // fusionMap.put("trust_contract", BRXT_TRUST_CONTRACT);
        //借款合同（固有 + 信托）
        // fusionMap.put("excuse_contract", BRXT_LOAN_CONTRACT);
        //永续债发行文件（固有 + 信托）
        fusionMap.put("perpetual_bond", BRXT_PERPETUAL_BOND);
        //信托项目投后报告（信托）
        fusionMap.put("trust_post_investment_report", BRXT_TRUST_POST_INVESTMENT_REPORT);
        //信托项目付款通知单（信托）
        fusionMap.put("payment_advice", BRXT_PAYMENT_ADVICE);
        //信息披露报告（信托）
        fusionMap.put("information_disclosure_report", BRXT_INFORMATION_DISCLOSURE_REPORT);
        //信托项目请示报告（信托）
        fusionMap.put("request_report", BRXT_REQUEST_REPORT);
        //利润表提取（固有 + 信托）
        fusionMap.put("income_statement_br", BRXT_INCOME_STATEMENT);
        //资产负债表提取（固有 + 信托）
        fusionMap.put("balance_sheet_br", BRXT_BALANCE_SHEET);
        //现金流量表提取（固有 + 信托）
        fusionMap.put("cash_flow_statement_br", BRXT_CASH_FLOW_STATEMENT);
        //投资协议合同基本信息（固有 + 信托）
        fusionMap.put("investment_agreement_contract_info", BRXT_INVESTMENT_AGREEMENT_CONTRACT_INFO);
        //投资协议合同-实体（固有 + 信托）
        fusionMap.put("investment_agreement_contract_entity", BRXT_INVESTMENT_AGREEMENT_CONTRACT_ENTITY);
        //投资协议合同-关系（固有 + 信托）
        fusionMap.put("investment_agreement_contract_rela", BRXT_INVESTMENT_AGREEMENT_CONTRACT_RELA);

        Map<String, DataFusionEnum> sharedFusionMap = Collections.unmodifiableMap(fusionMap);
        // 按附件并行调用：方法内按文件独立处理，可安全并发
        return ParallelTaskUtils.mapSum(attachments, globalTaskExecutor, attachment ->
                dataFusionService.parseToStructured(
                        ParallelTaskUtils.singleton(attachment), brxtClient, sharedFusionMap, false));
    }

    public List<BrxtAttachmentVo> findAttachments() {
        Map<String, Object> params = new HashMap<>();
        params.put("systemName", FusionSysEnum.BRXT.getSysName());
        String sql = "SELECT * from " + brxtAttachmentInfoTable + " b where " +
                " not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.file_id = r.source_id and r.system = :systemName) limit 200";
        try (Stream<BrxtAttachmentVo> brxtAttachmentVoStream = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, params, new BeanPropertyRowMapper<>(BrxtAttachmentVo.class));) {
            return brxtAttachmentVoStream.collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    private Long countByDataType() {
        Map<String, Object> params = new HashMap<>();
        params.put("systemName", FusionSysEnum.BRXT.getSysName());
        String sql = " select count(*) as tatal_count from  "
                + brxtAttachmentInfoTable + " b where " +
                " not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.file_id = r.source_id and r.system = :systemName)";
        List<Map<String, Object>> resList = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME).queryForList(sql, params);
        return (Long) (resList.get(0).get("tatal_count"));
    }

    private void uploadLocalFile2Oss(DataBusinessCodeEnum businessCodeEnum, String baseLocalPath) {
        File baseFoler = new File(baseLocalPath);
        if (!baseFoler.exists()) {
            return;
        }
        List<File> allFiles = getAllFiles(baseFoler);
        File successFile = allFiles.stream().filter(i -> SUCCESS_FILE_NAME.equalsIgnoreCase(i.getName()))
                .findFirst().orElse(null);
        if (successFile != null) {
            log.info("系统：{} 业务：{} 已全部上传到OSS，暂停处理，如果需要重新上传，请清理目录下的文件：{}/{}",
                    FusionSysEnum.BRXT.getSysName(), businessCodeEnum.getLocalPath(), baseLocalPath, SUCCESS_FILE_NAME);
            return;
        }

        String baseFolerAbsPath = baseFoler.getAbsolutePath().replace('\\', '/');
        baseFolerAbsPath = baseFolerAbsPath.substring(0, baseFolerAbsPath.indexOf(businessCodeEnum.getLocalPath()));

        //获取历史
        String absolutePath;
        String filePath;
        boolean isArchiveFile;
        for (File file : allFiles){
            absolutePath = file.getAbsolutePath().replace('\\', '/');
            filePath = absolutePath.replaceFirst(baseFolerAbsPath, "");
            isArchiveFile = ArchiveFileUtils.isArchiveFile(absolutePath);

            //校验是否存在：
            String queryFilePath = filePath;
            LambdaQueryWrapper<BrxtFileUploadRecordPo> wrapper = new LambdaQueryWrapper();
            wrapper.eq(BrxtFileUploadRecordPo::getBizType, businessCodeEnum.getBizName())
                    .and(i -> i.eq(BrxtFileUploadRecordPo::getFilePath, queryFilePath));
            List<BrxtFileUploadRecordPo> existFileRecordPos = brxtFileUploadRecordMapper.selectList(wrapper);
            BrxtFileUploadRecordPo brxtFileUploadRecordPo = CollectionUtils.isEmpty(existFileRecordPos) ? null : existFileRecordPos.get(0);

            // 跳过已上传的
            if (brxtFileUploadRecordPo != null && (isArchiveFile ?
                    brxtFileUploadRecordPo.getArchiveFileDone() == 1 && brxtFileUploadRecordPo.getArchiveFileSize() == file.length():
                    brxtFileUploadRecordPo.getFileSize() == file.length())) {
                continue;
            }

            if (isArchiveFile) {
                //处理压缩包内已经上传的
                long archiveFileSize = file.length();
                String archiveFilePath = filePath;
                Map<String, BrxtFileUploadRecordPo> archiveFileItemMap = existFileRecordPos.stream().filter(i -> i.getFilePath().equals(archiveFilePath))
                        .collect(Collectors.toMap(BrxtFileUploadRecordPo::getArchiveFilePath, t -> t,
                                (n, n1) -> n));
                if (file.length() > MAX_ARCHIVE_FILE_SIZE_GB) {
                    File unPackageFolder = null;
                    String replace = "";
                    try {
                        String  unPackageFolderPath = BigArchiveFileUtils.extractToLocal(file, true);
                        unPackageFolder = new File(unPackageFolderPath);
                        replace = unPackageFolderPath.replace('\\', '/');
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    if (unPackageFolder == null || !unPackageFolder.exists()) {
                        continue;
                    }

                    List<File> archiveFiles = getAllFiles(unPackageFolder);
                    for (File archiveFile : archiveFiles) {
                        if (ArchiveFileUtils.isArchiveFile(archiveFile.getName())) {
                            continue;
                        }
                        String itemArcPath = archiveFile.getAbsolutePath().replace('\\', '/').replaceFirst(replace, "");
                        if (archiveFileItemMap.containsKey(itemArcPath)) {
                            continue;
                        }
                        uploadFile(businessCodeEnum, archiveFile.getName(), filePath, itemArcPath, archiveFile.length(), HdFileUtils.readFileWithChannel(archiveFile), archiveFileSize);
                    }

                    // 删除超大压缩文件的解压目录
                    try {
                        FileUtils.deleteDirectory(unPackageFolder);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                } else {
                    List<ArchiveExtractorVo> archiveExtractorVos = ArchiveFileUtils.extract(file);
                    for (ArchiveExtractorVo archiveExtractorVo : archiveExtractorVos) {
                        if (archiveFileItemMap.containsKey(archiveExtractorVo.getPath())) {
                            continue;
                        }
                        uploadFile(businessCodeEnum, archiveExtractorVo.getFileName(), filePath, archiveExtractorVo.getPath(), Long.valueOf(archiveExtractorVo.getContent().length), archiveExtractorVo.getContent(), archiveFileSize);
                    }
                }
                //将 这一批次的 更新为完成
                LambdaUpdateWrapper<BrxtFileUploadRecordPo> updateWrapper = new LambdaUpdateWrapper();
                updateWrapper.set(BrxtFileUploadRecordPo::getArchiveFileDone, 1).eq(BrxtFileUploadRecordPo::getFilePath, filePath);
                brxtFileUploadRecordMapper.update(null, updateWrapper);
            } else {
                uploadFile(businessCodeEnum, file.getName(), filePath, "", file.length(), HdFileUtils.readFileWithChannel(file), 0L);
            }
        }
        if (null != successFile && successFile.exists()) {
            successFile.delete();
        }

        successFile = new File(baseLocalPath + File.separator + SUCCESS_FILE_NAME);
        try {
            log.info("系统：{} 业务：{} 全部完成上传到OSS，写入文件：{}/{}",
                    FusionSysEnum.BRXT.getSysName(), businessCodeEnum.getLocalPath(), baseLocalPath, SUCCESS_FILE_NAME);
            Files.write(successFile.toPath(),
                    Collections.singleton("Task completed successfully，Time: " + DateUtils.getStandardDateTime(new Date())),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("系统：{} 业务：{} 上传完成，写入 {} 文件报错！",
                    FusionSysEnum.BRXT.getSysName(), businessCodeEnum.getLocalPath(), SUCCESS_FILE_NAME);
            log.error(e.getMessage(), e);
        }
    }

    private void uploadFile(DataBusinessCodeEnum localFileEnum, String fileName, String filePath,
                            String archiveFilePath, Long fileSize, byte[] contents, Long archiveFileSize) {
        if (null == contents || ".DS_Store".equalsIgnoreCase(fileName) || ((fileName.startsWith(".") || fileName.startsWith("~")) && contents.length < 2048)) {
            log.error("文件{}/{}, 不合规/获取流为空，跳过处理！", filePath, fileName);
            return;
        }
        try {
            String fileId = Md5Utils.md5(localFileEnum.getBizName() + File.separator + filePath + archiveFilePath);
            BrxtFileUploadRecordPo po = new BrxtFileUploadRecordPo();
            po.setFileId(fileId);
            po.setFileName(fileName);
            po.setFilePath(filePath);
            po.setArchiveFileSize(archiveFileSize);
            po.setBizType(localFileEnum.getBizName());
            po.setArchiveFilePath(archiveFilePath);
            po.setFileSize(fileSize);

            //上传OSS，并获取oss的下载地址
            String objectKey = ossUtils.buildObjectKey(fileId, fileName, FusionSysEnum.BRXT);
            log.info("开始上传文件：{}, 文件大小：{}", po.getFileName(), HdFileUtils.formatSize(contents.length));
            ossUtils.upload(ossUtils.defaultBucketKey(), objectKey, contents);

            po.setOssFilePath(objectKey);
            brxtFileUploadRecordMapper.insert(po);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private List<File> getAllFiles(File dir) {
        List<File> fileList = new ArrayList<>();
        if (!dir.exists() || !dir.isDirectory()) {
            return fileList;
        }
        traverse(dir, fileList);
        return fileList;
    }

    private void traverse(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files == null) {
            return; // 权限不足等情况
        }
        for (File file : files) {
            if (file.isDirectory()) {
                traverse(file, fileList); // 递归子目录
            } else {
                fileList.add(file); // 只添加文件
            }
        }
    }
}
