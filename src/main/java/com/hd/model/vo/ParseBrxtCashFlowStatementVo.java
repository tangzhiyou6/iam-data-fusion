package com.hd.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtCashFlowStatementPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 财务报告 - 现金流量表（固有 + 信托）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtCashFlowStatementVo extends DataFusionBaseVo<BrxtCashFlowStatementPo, String> {

    private static final long serialVersionUID = 1L;

    /** 企业全称 */
    @JsonProperty("company_name")
    private String companyName;

    /** 项目名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 财务指标集合（按期间分组） */
    @JsonProperty("financial_indicators")
    private List<FinancialPeriodData> financialIndicators;

    @Override
    public List<BrxtCashFlowStatementPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<BrxtCashFlowStatementPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(financialIndicators)) {
            for (FinancialPeriodData financialPeriodData : financialIndicators) {
                if (!CollectionUtils.isEmpty(financialPeriodData.getData())) {
                    for (FinancialIndicator indicator : financialPeriodData.getData()) {
                        BrxtCashFlowStatementPo po = buildCommonPo(sourceDataId, batchNo);
                        po.setDate(financialPeriodData.getDate());
                        po.setIndicatorName(indicator.getIndicatorName());
                        po.setIndicatorValue(indicator.getIndicatorValue());
                        pos.add(po);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(pos)) {
            pos.add(buildCommonPo(sourceDataId, batchNo));
        }
        return pos;
    }

    private BrxtCashFlowStatementPo buildCommonPo(String sourceDataId, String batchNo) {
        BrxtCashFlowStatementPo po = new BrxtCashFlowStatementPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setCompanyName(this.companyName);
        po.setProjectName(this.projectName);
        return po;
    }

    /**
     * 单个期间的财务数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialPeriodData {

        /** 财务统计期间 */
        @JsonProperty("date")
        private String date;

        /** 该期间内的具体指标数据 */
        @JsonProperty("data")
        private List<FinancialIndicator> data;
    }

    /**
     * 单个财务指标项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialIndicator {

        /** 标准化指标名/文档原名称 */
        @JsonProperty("indicator_name")
        private String indicatorName;

        /** 指标数值 */
        @JsonProperty("indicator_value")
        private String indicatorValue;
    }
}
