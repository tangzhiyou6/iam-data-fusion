package com.hd.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtProjectReviewCommentPo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * 项目评审报告封装类
 */
@Data
public class ParseBrxtProjectReviewCommentVo extends DataFusionBaseVo<BrxtProjectReviewCommentPo, String> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 信托/项目全称
     */
    @JsonProperty("project_name")
    private String projectName;
    
    /**
     * 评审意见书编码
     */
    @JsonProperty("review_code")
    private String reviewCode;
    
    /**
     * 提交部门
     */
    @JsonProperty("submit_department")
    private String submitDepartment;
    
    /**
     * 评审依据文本
     */
    @JsonProperty("review_basis")
    private String reviewBasis;
    
    /**
     * 风险等级
     */
    @JsonProperty("risk_level")
    private String riskLevel;
    
    /**
     * 评审结论
     */
    @JsonProperty("review_conclusion")
    private String reviewConclusion;
    
    /**
     * 借款人分析列表
     */
    @JsonProperty("borrower_analysis")
    private List<BorrowerAnalysis> borrowerAnalysis;
    
    /**
     * 担保人分析列表
     */
    @JsonProperty("guarantor_analysis")
    private List<GuarantorAnalysis> guarantorAnalysis;
    
    /**
     * 风险分析列表
     */
    @JsonProperty("risk_analysis")
    private List<RiskAnalysis> riskAnalysis;


    @Override
    public List<BrxtProjectReviewCommentPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<BrxtProjectReviewCommentPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(borrowerAnalysis)) {
            for (BorrowerAnalysis borrowerAnalysy : borrowerAnalysis) {
                BrxtProjectReviewCommentPo po = buildCommonPo(sourceDataId, batchNo);
                po.setBorrowerRepayAbility(borrowerAnalysy.getRepayAbility());
                po.setBorrowerShareholderBg(borrowerAnalysy.getShareholderBg());
                po.setBorrowerOperateInfo(borrowerAnalysy.getOperateInfo());
                po.setBorrowerFinanceInfo(borrowerAnalysy.getFinanceInfo());
                po.setBorrowerOverallStatus(borrowerAnalysy.getOverallStatus());
                po.setDataType("borrower_analysis");
                pos.add(po);
            }
        }

        if (!CollectionUtils.isEmpty(guarantorAnalysis)) {
            for (GuarantorAnalysis analysy : guarantorAnalysis) {
                BrxtProjectReviewCommentPo po = buildCommonPo(sourceDataId, batchNo);
                po.setGuarantorGuaranteeCapacity(analysy.getGuaranteeCapacity());
                po.setGuarantorShareholderBg(analysy.getShareholderBg());
                po.setGuarantorOperateInfo(analysy.getOperateInfo());
                po.setGuarantorFinanceInfo(analysy.getFinanceInfo());
                po.setGuarantorOverallStatus(analysy.getOverallStatus());
                po.setDataType("guarantor_analysis");
                pos.add(po);
            }
        }

        if (!CollectionUtils.isEmpty(riskAnalysis)) {
            for (RiskAnalysis riskAnalysy : riskAnalysis) {
                BrxtProjectReviewCommentPo po = buildCommonPo(sourceDataId, batchNo);
                po.setComplianceAnalysis(riskAnalysy.getComplianceAnalysis());
                po.setTrusteeDutyAnalysis(riskAnalysy.getTrusteeDutyAnalysis());
                po.setTrusteeRiskAnalysis(riskAnalysy.getTrusteeRiskAnalysis());
                po.setTransactionComplianceAnalysis(riskAnalysy.getTransactionComplianceAnalysis());
                po.setIncomeCapRatio(riskAnalysy.getIncomeCapRatio());
                po.setDataType("risk_analysis");
                pos.add(po);
            }
        }

        if (CollectionUtils.isEmpty(pos)) {
            pos.add(buildCommonPo(sourceDataId, batchNo));
        }
        return pos;
    }

    private BrxtProjectReviewCommentPo buildCommonPo(String sourceDataId, String batchNo){
        BrxtProjectReviewCommentPo po = new BrxtProjectReviewCommentPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setReviewBasis(this.reviewBasis);
        po.setReviewCode(this.reviewCode);
        po.setSubmitDepartment(this.submitDepartment);
        po.setRiskLevel(this.riskLevel);
        po.setReviewConclusion(this.reviewConclusion);
        return po;
    }

    /**
     * 借款人分析内部类
     */
    @Data
    public static class BorrowerAnalysis implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 借款人还款能力分析内容
         */
        @JsonProperty("repay_ability")
        private String repayAbility;
        
        /**
         * 还款方股东背景描述
         */
        @JsonProperty("shareholder_bg")
        private String shareholderBg;
        
        /**
         * 还款方经营情况描述
         */
        @JsonProperty("operate_info")
        private String operateInfo;
        
        /**
         * 还款方财务情况描述
         */
        @JsonProperty("finance_info")
        private String financeInfo;
        
        /**
         * 还款方整体情况总结
         */
        @JsonProperty("overall_status")
        private String overallStatus;
    }
    
    /**
     * 担保人分析内部类
     */
    @Data
    public static class GuarantorAnalysis implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 担保人担保能力分析
         */
        @JsonProperty("guarantee_capacity")
        private String guaranteeCapacity;
        
        /**
         * 担保方股东背景描述
         */
        @JsonProperty("shareholder_bg")
        private String shareholderBg;
        
        /**
         * 担保方经营情况描述
         */
        @JsonProperty("operate_info")
        private String operateInfo;
        
        /**
         * 担保方财务情况描述
         */
        @JsonProperty("finance_info")
        private String financeInfo;
        
        /**
         * 担保方整体情况总结
         */
        @JsonProperty("overall_status")
        private String overallStatus;
    }
    
    /**
     * 风险分析内部类
     */
    @Data
    public static class RiskAnalysis implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 合规性分析全文
         */
        @JsonProperty("compliance_analysis")
        private String complianceAnalysis;
        
        /**
         * 受托责任分析内容
         */
        @JsonProperty("trustee_duty_analysis")
        private String trusteeDutyAnalysis;
        
        /**
         * 受托责任风险分析内容
         */
        @JsonProperty("trustee_risk_analysis")
        private String trusteeRiskAnalysis;
        
        /**
         * 交易模式合规法律分析
         */
        @JsonProperty("transaction_compliance_analysis")
        private String transactionComplianceAnalysis;
        
        /**
         * 收入资本比说明/数值
         */
        @JsonProperty("income_cap_ratio")
        private String incomeCapRatio;
    }
}