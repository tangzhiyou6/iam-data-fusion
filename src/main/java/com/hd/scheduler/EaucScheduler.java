package com.hd.scheduler;

import static com.hd.constants.DataFusionEnum.EAUC_FLWZ;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_EAUC_FLWZ;
import static com.hd.constants.FusionBizNameConstants.EAUC_BIZ_TYPES;
import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.hd.client.EaucClient;
import com.hd.config.GlobalThreadPoolConfig;
import com.hd.constants.DataFusionEnum;
import com.hd.model.vo.EaucAttachmentVo;
import com.hd.service.DataFusionService;
import com.hd.utils.DateUtils;
import com.hd.utils.JdbcPoolUtils;
import com.hd.utils.ParallelTaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EaucScheduler {

    private final JdbcPoolUtils jdbcPoolUtils;

    @Autowired
    private DataFusionService dataFusionService;

    @Autowired
    private EaucClient eaucClient;

    @Resource(name = GlobalThreadPoolConfig.GLOBAL_TASK_EXECUTOR)
    private ThreadPoolTaskExecutor globalTaskExecutor;

    @Value("${file-sync.eauc.attachment_view}")
    private String eaucAttachmentInfoView;

    //@Scheduled(fixedDelay = 1 * 60 * 1000)
    public void extractStructuredData() {
        if (CollectionUtils.isEmpty(EAUC_BIZ_TYPES)) {
            return;
        }
        for (String dataType : EAUC_BIZ_TYPES) {
            Integer totalCount = 0;
            Integer succCount = 0;
            Long totalNum = countByDataType(dataType);
            log.info("获取类型-{}-数据共 {} 条，开始提取。", dataType, totalNum);
            List<EaucAttachmentVo> attachments = findAttachments(dataType);
            while (attachments.size() > 0) {
                totalCount += attachments.size();
                succCount += parseToStructured(attachments, dataType);
                attachments = findAttachments(dataType);
            }
            log.info("电能e拍 - {} - {}同步完成：共 {}条数据，成功：{}", dataType, DateUtils.getStandardDateTime(null), totalCount, succCount);
        }
    }

    private Integer parseToStructured(List<EaucAttachmentVo> attachments, String dataType) {
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
    }

    public List<EaucAttachmentVo> findAttachments(String dataType) {
        DataFusionEnum byBizName = DataFusionEnum.getByBizName(dataType);
        Map<String, String> param = new HashMap<>();
        param.put("systemName", byBizName.getBizNameEnum().getSysName());
        param.put("bizName", byBizName.getBizName());
        String sql = "SELECT * from " + eaucAttachmentInfoView + " b where " +
                " not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.id = r.source_id and r.system = :systemName and r.biz_type = :bizName) limit 200";
        try (Stream<EaucAttachmentVo> eaucAttachmentVoStream = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, param, new BeanPropertyRowMapper<>(EaucAttachmentVo.class));) {
            return eaucAttachmentVoStream.collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    private Long countByDataType(String dataType) {
        DataFusionEnum byBizName = DataFusionEnum.getByBizName(dataType);
        Map<String, String> param = new HashMap<>();
        param.put("systemName", byBizName.getBizNameEnum().getSysName());
        param.put("bizName", byBizName.getBizName());
        String sql = " select  count(*) as tatal_count from  "
                + eaucAttachmentInfoView
                + " b where not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.id = r.source_id and r.system = :systemName and r.biz_type = :bizName)";
        List<Map<String, Object>> resList = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME).queryForList(sql, param);
        return (Long) (resList.get(0).get("tatal_count"));
    }
}
