package com.hd.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzFddBalanceSheetPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzFddBalanceSheetVo extends DataFusionBaseVo<CxtzFddBalanceSheetPo, String> {

    /**
     * 企业全称
     */
    @JsonProperty("company_name")
    private String companyName;

    /**
     * 报告出具日期
     */
    @JsonProperty("report_date")
    private String reportDate;

    /**
     * 工作截止日
     */
    @JsonProperty("work_end_date")
    private String workEndDate;

    /**
     * 财务指标集合（按期间分组）
     */
    @JsonProperty("financial_indicators")
    private List<FinancialPeriodData> financialIndicators;

    @Override
    public List<CxtzFddBalanceSheetPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<CxtzFddBalanceSheetPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(financialIndicators)) {
            for (FinancialPeriodData financialPeriodData : financialIndicators) {
                if (!CollectionUtils.isEmpty(financialPeriodData.getData())) {
                    for (FinancialIndicator indicator : financialPeriodData.getData()) {
                        CxtzFddBalanceSheetPo po = buildCommonPo(sourceDataId, batchNo);
                        po.setPeriodDate(financialPeriodData.getDate());
                        po.setIndicatorName(indicator.getIndicatorName());
                        po.setIndicatorValue(indicator.getIndicatorValue());
                        pos.add(po);
                    }
                }
            }
        } else {
            pos.add(buildCommonPo(sourceDataId, batchNo));
        }
        return pos;
    }


    private CxtzFddBalanceSheetPo buildCommonPo(String sourceDataId, String batchNo){
        CxtzFddBalanceSheetPo po = new CxtzFddBalanceSheetPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setCompanyName(this.companyName);
        po.setReportDate(this.reportDate);
        po.setWorkEndDate(this.workEndDate);
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

        /**
         * 财务统计期间（如：2024-12-31、Q1 2024等）
         */
        @JsonProperty("date")
        private String date;

        /**
         * 该期间内的具体指标数据
         */
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

        /**
         * 指标名称（标准化指标名或文档原名称）
         * 示例：流动比率、资产负债率、净利润等
         */
        @JsonProperty("indicator_name")
        private String indicatorName;

        /**
         * 指标数值
         */
        @JsonProperty("indicator_value")
        private String indicatorValue;
    }
}
