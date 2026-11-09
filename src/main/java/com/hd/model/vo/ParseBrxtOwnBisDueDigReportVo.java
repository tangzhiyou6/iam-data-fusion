package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtOwnBisDueDigReportPo;
import lombok.Data;

@Data
public class ParseBrxtOwnBisDueDigReportVo extends DataFusionBaseVo<BrxtOwnBisDueDigReportPo, String> {

    private static final long serialVersionUID = 1L;

    /** 项目/信托计划全称 */
    @JsonProperty("project_trust_name")
    private String projectTrustName;

    /** 项目类型 */
/*    @JsonProperty("project_type")
    private String projectType;*/

    /** 提交部门名称 */
    @JsonProperty("submit_department")
    private String submitDepartment;

    /** 提交日期 */
    @JsonProperty("submit_date")
    private String submitDate;

    /** 内部收益率数值/原文 */
    @JsonProperty("irr")
    private String irr;

    /** 投资回报率数值/原文 */
    @JsonProperty("roi")
    private String roi;

    /** 是否覆盖公司资金成本说明 */
    @JsonProperty("cover_capital_cost")
    private String coverCapitalCost;

    /** 与自有资金流动性匹配情况描述 */
    @JsonProperty("liquidity_match")
    private String liquidityMatch;

    /** 投资期限原文描述 */
    @JsonProperty("invest_term")
    private String investTerm;

    /** 是否有财务报表说明 */
    @JsonProperty("has_financial_statement")
    private String hasFinancialStatement;

    @Override
    public List<BrxtOwnBisDueDigReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtOwnBisDueDigReportPo po = new BrxtOwnBisDueDigReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectTrustName(this.projectTrustName);
        // po.setProjectType(this.getProjectType());
        po.setSubmitDepartment(this.getSubmitDepartment());
        po.setSubmitDate(this.getSubmitDate());
        po.setIrr(this.getIrr());
        po.setRoi(this.getRoi());
        po.setCoverCapitalCost(this.coverCapitalCost);
        po.setLiquidityMatch(this.liquidityMatch);
        po.setInvestTerm(this.getInvestTerm());
        po.setHasFinancialStatement(this.hasFinancialStatement);

        return Collections.singletonList(po);
    }
}