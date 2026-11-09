package com.hd.scheduler;

import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.hd.client.SkClient;
import com.hd.config.GlobalThreadPoolConfig;
import com.hd.config.SM4CryptoUtil;
import com.hd.constants.FusionSysEnum;
import com.hd.mapper.SkFileRecordMapper;
import com.hd.model.po.SkFileRecordPo;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.SkAttachmentVo;
import com.hd.utils.HdFileUtils;
import com.hd.utils.JdbcPoolUtils;
import com.hd.utils.OssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkScheduler {

    private final JdbcPoolUtils jdbcPoolUtils;

    @Autowired
    private SkClient skClient;

    @Autowired
    private OssUtils ossUtils;

    @Autowired
    private SkFileRecordMapper skFileRecordMapper;

    @Autowired
    private SM4CryptoUtil sm4CryptoUtil;

    @Resource(name = GlobalThreadPoolConfig.GLOBAL_TASK_EXECUTOR)
    private ThreadPoolTaskExecutor globalTaskExecutor;

    // 系统数据量比较大，先拉取文件 再 进行解析
    //@Scheduled(fixedDelay = 15 * 60 * 1000)
    public void extractStructuredData() {
        boolean hasFileDownload = true;
        while (hasFileDownload) {
            Long downloadCount = findDownloadCount();
            List<SkAttachmentVo> downloadVoList = findDownloadList();
            hasFileDownload = !CollectionUtils.isEmpty(downloadVoList);
            if (!hasFileDownload) {
                break;
            }
            log.info("下载司库文件到本ai审计oss，当前批次：{}, 总剩余：{}个", downloadVoList.size(), downloadCount);
            int failedCount = 0;
            for (SkAttachmentVo vo : downloadVoList) {
                boolean successUpload = uploadOss(vo);
                if (!successUpload) {
                    failedCount++;
                }
                if (failedCount > 9) {
                    log.error("本批次文件下载失败次数达到上限：10个,终止本批次下载，请排查！");
                    return;
                }
            }
        }
    }

/*    private Integer parseToStructured(List<EaucAttachmentVo> attachments, String dataType) {
        Map<String, DataFusionEnum> fusionMap = new HashMap<>();
        switch (dataType) {
            case BIZ_NAME_EAUC_FLWZ:
                fusionMap.put("waste_materials", EAUC_FLWZ);
                break;
            default:
                //log.error("{} 数据接入，不支持的类型！", INNOVATION_INVESTMENT_FUND.getName());
                return 0;
        }
        Map<String, DataFusionEnum> sharedFusionMap = Collections.unmodifiableMap(fusionMap);
        return ParallelTaskUtils.mapSum(attachments, globalTaskExecutor, attachment ->
                dataFusionService.parseToStructured(
                        ParallelTaskUtils.singleton(attachment), eaucClient, sharedFusionMap, false));
    }*/

    public List<SkAttachmentVo> findAttachments() {
        Map<String, String> param = new HashMap<>();
        param.put("systemName", FusionSysEnum.SK.getSysName());
        String sql = "SELECT t.fmid as id,  t.filename as fileName, t.filetype as fileType, t.filetypename as fileTypeName, t.filepath as filePath,  t.uploadtime as uploadTime " +
                " from ods_skgl.ods_skgl_all_vw_gdt_sj_file t where " +
                " not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where t.fmid = r.source_id and r.system = :systemName) limit 2000";
        try (Stream<SkAttachmentVo> eaucAttachmentVoStream = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, param, new BeanPropertyRowMapper<>(SkAttachmentVo.class));) {
            return eaucAttachmentVoStream.collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    public boolean uploadOss(SkAttachmentVo vo) {
        SkFileRecordPo po = new SkFileRecordPo(vo);
        FileDownloadResult fileDownloadResult = skClient.freeDownload(vo.getFilePath());
        if (null == fileDownloadResult) {
            po.setStatus(3001);
            po.setRemark("FileDownloadResult 为空！");
            upsertFileRecordPo(po);
            return false;
        }

        if (StringUtils.hasText(fileDownloadResult.getErrorMsg())) {
            po.setStatus(fileDownloadResult.getHttpStatus());
            po.setRemark(fileDownloadResult.getErrorMsg());
            upsertFileRecordPo(po);
            return false;
        }

        try {
            byte[] contents = fileDownloadResult.getContent();
            contents = sm4CryptoUtil.decrypt(contents);
            if (null == contents) {
                po.setStatus(3010);
                po.setRemark("解密报错！");
                upsertFileRecordPo(po);
                return false;
            }
            String objectKey = ossUtils.buildObjectKey(vo.getId(), vo.getFileName(), FusionSysEnum.SK);
            log.info("开始上传文件：{}, 文件大小：{}", po.getFileName(), HdFileUtils.formatSize(contents.length));
            ossUtils.upload(ossUtils.defaultBucketKey(), objectKey, contents);

            po.setFileSize(Integer.valueOf(contents.length).longValue());
            po.setOssFilePath(objectKey);
            po.setOssBucketName(ossUtils.defaultBucketKey());
            upsertFileRecordPo(po);
            return true;
        } catch (Exception e) {
            po.setStatus(3002);
            po.setRemark(e.getMessage());
            log.error(e.getMessage(), e);
            upsertFileRecordPo(po);
            return false;
        }
    }

    private void upsertFileRecordPo(SkFileRecordPo po) {
        if (StringUtils.hasText(po.getId())) {
            skFileRecordMapper.deleteById(po.getId());
        }
        skFileRecordMapper.insert(po);
    }

    private List<SkAttachmentVo> findDownloadList() {
        String sql = "SELECT t.fmid as id,  t.filename as fileName, t.filetype as fileType, t.filetypename as fileTypeName, t.filepath as filePath,  t.uploadtime as uploadTime " +
                " from ods_skgl.ods_skgl_all_vw_gdt_sj_file t where not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sk_file_record r where t.fmid = r.id) limit 2000";
        try (Stream<SkAttachmentVo> eaucAttachmentVoStream = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, new HashMap<>(), new BeanPropertyRowMapper<>(SkAttachmentVo.class));) {
            return eaucAttachmentVoStream.collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    private Long findDownloadCount() {
        Map<String, String> param = new HashMap<>();
        param.put("systemName", FusionSysEnum.SK.getSysName());
        String sql = " select  count(*) as tatal_count " +
                " from ods_skgl.ods_skgl_all_vw_gdt_sj_file t where not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sk_file_record r where t.fmid = r.id)";
        List<Map<String, Object>> resList = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME).queryForList(sql, param);
        return (Long) (resList.get(0).get("tatal_count"));
    }
}
