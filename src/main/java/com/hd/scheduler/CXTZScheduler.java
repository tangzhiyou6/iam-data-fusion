package com.hd.scheduler;

import static com.hd.constants.DataFusionEnum.CXTZ_CWJD_LRB;
import static com.hd.constants.DataFusionEnum.CXTZ_CWJD_XJLL;
import static com.hd.constants.DataFusionEnum.CXTZ_CWJD_ZCFZ;
import static com.hd.constants.DataFusionEnum.CXTZ_FLJD;
import static com.hd.constants.DataFusionEnum.CXTZ_GZBG;
import static com.hd.constants.DataFusionEnum.CXTZ_HYJY;
import static com.hd.constants.DataFusionEnum.CXTZ_TZXY_GXXX;
import static com.hd.constants.DataFusionEnum.CXTZ_TZXY_JBXX;
import static com.hd.constants.DataFusionEnum.CXTZ_TZXY_STXX;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_CWJD;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_FLJD;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_GZBG;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_HYJY;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_TZXY;
import static com.hd.constants.FusionBizNameConstants.CXTZ_BIZ_TYPES;
import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.hd.client.CXTZClient;
import com.hd.config.GlobalThreadPoolConfig;
import com.hd.constants.DataFusionEnum;
import com.hd.model.vo.CXTZAttachmentVo;
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

/**
 * 创新投资 非结构模型提取调度。
 *
 * */

@Slf4j
@Service
@RequiredArgsConstructor
public class CXTZScheduler {

    private final JdbcPoolUtils jdbcPoolUtils;

    @Resource
    private CXTZClient cxtzClient;

    @Autowired
    private DataFusionService dataFusionService;

    @Resource(name = GlobalThreadPoolConfig.GLOBAL_TASK_EXECUTOR)
    private ThreadPoolTaskExecutor globalTaskExecutor;

    @Value("${file-sync.cxtz.attachment_table}")
    private String cxtzAttachmentInfoTable;


    //@Scheduled(fixedDelay = 1 * 60 * 1000)
    public void extractStructuredData() {
        if (CollectionUtils.isEmpty(CXTZ_BIZ_TYPES)) {
            return;
        }
        for (String dataType : CXTZ_BIZ_TYPES) {
            Integer totalCount = 0;
            Integer succCount = 0;
            Long totalNum = countByDataType(dataType);
            log.info("获取类型-{}-数据共 {} 条，开始提取。", dataType, totalNum);
            List<CXTZAttachmentVo> attachments = findAttachments(dataType);
            while (attachments.size() > 0) {
                totalCount += attachments.size();
                succCount += parseToStructured(attachments, dataType);
                attachments = findAttachments(dataType);
            }
            log.info("创新投资 - {} - {}同步完成：共 {}条数据，成功：{}", dataType, DateUtils.getStandardDateTime(null), totalCount, succCount);
        }
    }

    private Integer parseToStructured(List<CXTZAttachmentVo> attachments, String dataType) {
        Map<String, DataFusionEnum> fusionMap = new HashMap<>();
        switch (dataType) {

            case BIZ_NAME_CXTZ_GZBG:
                fusionMap.put("valuation_report", CXTZ_GZBG);
                break;

            case BIZ_NAME_CXTZ_CWJD:
                fusionMap.put("balance_sheet", CXTZ_CWJD_ZCFZ);
                fusionMap.put("income_statement", CXTZ_CWJD_LRB);
                fusionMap.put("cash_flow_statement", CXTZ_CWJD_XJLL);
                break;

            case BIZ_NAME_CXTZ_FLJD:
                fusionMap.put("ldd_data", CXTZ_FLJD);
                break;

            case BIZ_NAME_CXTZ_HYJY:
                fusionMap.put("mm_data", CXTZ_HYJY);
                break;

            case BIZ_NAME_CXTZ_TZXY:
                fusionMap.put("investment_info", CXTZ_TZXY_JBXX);
                fusionMap.put("investment_entity", CXTZ_TZXY_STXX);
                fusionMap.put("investment_relation", CXTZ_TZXY_GXXX);
                break;

            default:
                //log.error("{} 数据接入，不支持的类型！", INNOVATION_INVESTMENT_FUND.getName());
                return 0;
        }
        Map<String, DataFusionEnum> sharedFusionMap = Collections.unmodifiableMap(fusionMap);
        return ParallelTaskUtils.mapSum(attachments, globalTaskExecutor, attachment ->
                dataFusionService.parseToStructured(
                        ParallelTaskUtils.singleton(attachment), cxtzClient, sharedFusionMap, false));
    }

    private List<CXTZAttachmentVo> findAttachments(String dataType) {
        Map<String, Object> params = new HashMap<>();
        String sql = " select  b.id, b.name, b.url, b.create_time from  "
                + cxtzAttachmentInfoTable
                + " b where b.name like :dataType and b.del_flag='0' "
                + " and not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.id = r.source_id and r.system = :systemName and r.biz_type = :bizName) limit 100";
        params.put("dataType", "%" + dataType + "%");
        DataFusionEnum byBizName = DataFusionEnum.getByBizName(dataType);
        params.put("systemName", byBizName.getBizNameEnum().getSysName());
        params.put("bizName", byBizName.getBizName());
        try (Stream<CXTZAttachmentVo> stream = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                     .queryForStream(sql, params, new BeanPropertyRowMapper<>(CXTZAttachmentVo.class))) {
            List<CXTZAttachmentVo> brxtAttachmentVos = stream.collect(Collectors.toList());
            log.info("获取 {} 数据：{} 条", dataType, brxtAttachmentVos.size());
            return brxtAttachmentVos;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    private Long countByDataType(String dataType) {
        Map<String, Object> params = new HashMap<>();
        String sql = " select  count(*) as tatal_count from  "
                + cxtzAttachmentInfoTable
                + " b where b.name like :dataType and b.del_flag='0' "
                + " and not exists (SELECT 1 from ods_dsjsj.ods_dsjsj_sync_records r where b.id = r.source_id and r.system = :systemName and r.biz_type = :bizName)";
        params.put("dataType", "%" + dataType + "%");
        DataFusionEnum byBizName = DataFusionEnum.getByBizName(dataType);
        params.put("systemName", byBizName.getBizNameEnum().getSysName());
        params.put("bizName", byBizName.getBizName());
        List<Map<String, Object>> resList = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME).queryForList(sql, params);
        return (Long) (resList.get(0).get("tatal_count"));
    }
}
