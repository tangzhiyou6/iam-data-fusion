package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtTrustPostInvestmentReportPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信托项目投后报告（信托）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtTrustPostInvestmentReportVo extends DataFusionBaseVo<BrxtTrustPostInvestmentReportPo, String> {

    private static final long serialVersionUID = 1L;

    /** 信托项目完整名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 业务品种 */
    @JsonProperty("service_type")
    private String serviceType;

    /** 信托成立日期 */
    @JsonProperty("establish_date")
    private String establishDate;

    /** 存续规模原文 */
    @JsonProperty("outstanding_scale")
    private String outstandingScale;

    /** 报告所属期/报告日期 */
    @JsonProperty("report_date")
    private String reportDate;

    /** 风险揭示完整原文 */
    @JsonProperty("risk_disclosure")
    private String riskDisclosure;

    /** 本期收入合计金额/描述 */
    @JsonProperty("current_period_income")
    private String currentPeriodIncome;

    /** 本年收入合计金额/描述 */
    @JsonProperty("year_to_date_income")
    private String yearToDateIncome;

    /** 本年净利润合计金额/描述 */
    @JsonProperty("year_to_date_net_profit")
    private String yearToDateNetProfit;


    @JsonProperty("current_period_distributed_profit")
    private String currentPeriodDistributedProfit;

    @Override
    public List<BrxtTrustPostInvestmentReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtTrustPostInvestmentReportPo po = new BrxtTrustPostInvestmentReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setServiceType(this.serviceType);
        po.setEstablishDate(this.establishDate);
        po.setOutstandingScale(this.outstandingScale);
        po.setReportDate(this.reportDate);
        po.setRiskDisclosure(this.riskDisclosure);
        po.setCurrentPeriodIncome(this.currentPeriodIncome);
        po.setYearToDateIncome(this.yearToDateIncome);
        po.setYearToDateNetProfit(this.yearToDateNetProfit);
        po.setCurrentPeriodDistributedProfit(this.currentPeriodDistributedProfit);

        return Collections.singletonList(po);
    }
}
