package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtTtDueDigReportPo;
import lombok.Data;

@Data
public class ParseBrxtTtDueDigReportVo extends DataFusionBaseVo<BrxtTtDueDigReportPo, String> {

    private static final long serialVersionUID = 1L;

    /** 项目/信托计划全称 */
    @JsonProperty("project_trust_name")
    private String projectTrustName;

    /** 项目类型 */
/*    @JsonProperty("project_type")
    private String projectType;*/

    /** 调查对象主体信息 */
    @JsonProperty("investigate_target")
    private String investigateTarget;

    /** 调查时间 */
    @JsonProperty("investigate_date")
    private String investigateDate;

    /** 产品类型 */
    @JsonProperty("product_type")
    private String productType;

    /** 风险等级 */
    @JsonProperty("risk_level")
    private String riskLevel;

    /** 被调查公司全称 */
    @JsonProperty("target_company")
    private String targetCompany;

    /** 投资适当性管理原文描述 */
    @JsonProperty("invest_appropriateness")
    private String investAppropriateness;

    /** 风险隔离相关说明 */
    @JsonProperty("risk_isolation")
    private String riskIsolation;

    /** 优先/劣后级结构描述 */
    @JsonProperty("priority_subordinate")
    private String prioritySubordinate;

    /** 差额补足约定内容 */
    @JsonProperty("difference_complement")
    private String differenceComplement;

    /** 是否足以支撑信息披露原文 */
    @JsonProperty("info_disclosure_support")
    private String infoDisclosureSupport;

    /** 是否符合“资管新规”要求原文 */
    @JsonProperty("comply_regulation")
    private String complyRegulation;

    /** 交易对手的主体信用评级 */
    @JsonProperty("counterparty_rating")
    private String counterpartyRating;

    /** 项目退出路径描述 */
    @JsonProperty("exit_path")
    private String exitPath;

    @Override
    public List<BrxtTtDueDigReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtTtDueDigReportPo po  = new BrxtTtDueDigReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectTrustName(this.projectTrustName);
        // po.setProjectType(this.projectType);
        po.setInvestigateTarget(this.investigateTarget);
        po.setInvestigateDate(this.investigateDate);
        po.setProductType(this.productType);
        po.setRiskLevel(this.riskLevel);
        po.setTargetCompany(this.targetCompany);
        po.setInvestAppropriateness(this.investAppropriateness);
        po.setRiskIsolation(this.riskIsolation);
        po.setPrioritySubordinate(this.prioritySubordinate);
        po.setDifferenceComplement(this.differenceComplement);
        po.setInfoDisclosureSupport(this.infoDisclosureSupport);
        po.setComplyRegulation(this.complyRegulation);
        po.setCounterpartyRating(this.counterpartyRating);
        po.setExitPath(this.exitPath);
        return Collections.singletonList(po);
    }
}