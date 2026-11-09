package com.hd.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzInvValReportPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 投资项目估值报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzInvValReportVo extends DataFusionBaseVo<CxtzInvValReportPo, String>{

    /**
     * 项目名称
     */
    @JsonProperty("project_name")
    private String projectName;

    /**
     * 估值基准日
     */
    @JsonProperty("valuation_base_date")
    private String valuationBaseDate;

    /**
     * 报告出具日期
     */
    @JsonProperty("report_issue_date")
    private String reportIssueDate;

    /**
     * 估值单位
     */
    @JsonProperty("valuation_unit")
    private String valuationUnit;

    /**
     * 财务指标列表
     */
    @JsonProperty("financial_indicators")
    private List<FinancialIndicator> financialIndicators;



    /**
     * 财务指标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialIndicator {

        /**
         * 指标名称
         */
        @JsonProperty("indicator_name")
        private String indicatorName;

        /**
         * 指标值
         */
        @JsonProperty("indicator_value")
        private String indicatorValue;
    }

    @Override
    public List<CxtzInvValReportPo> buildPo(String sourceDataId, String scene){
        String batchNo = UUID.randomUUID().toString();
        List<CxtzInvValReportPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(financialIndicators)) {
            for (FinancialIndicator indicator : financialIndicators) {
                CxtzInvValReportPo cxtzInvValReportPo = buildCommonPo(sourceDataId, batchNo);
                cxtzInvValReportPo.setIndicatorName(indicator.getIndicatorName());
                cxtzInvValReportPo.setIndicatorValue(indicator.getIndicatorValue());
                pos.add(cxtzInvValReportPo);
            }
        } else {
            pos.add(buildCommonPo(sourceDataId, batchNo));
        }
        return pos;
    }

    private CxtzInvValReportPo buildCommonPo(String sourceDataId, String batchNo){
        CxtzInvValReportPo po = new CxtzInvValReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setValuationBaseDate(this.valuationBaseDate);
        po.setReportIssueDate(this.reportIssueDate);
        po.setValuationUnit(this.valuationUnit);
        return po;
    }
}