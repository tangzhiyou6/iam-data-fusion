package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_project_review_comment")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtProjectReviewCommentPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String projectName;

    private String reviewCode;

    private String submitDepartment;

    private String reviewBasis;

    private String riskLevel;

    private String reviewConclusion;

    private String borrowerRepayAbility;

    private String borrowerShareholderBg;

    private String borrowerOperateInfo;

    private String borrowerFinanceInfo;

    private String borrowerOverallStatus;

    private String guarantorGuaranteeCapacity;

    private String guarantorShareholderBg;

    private String guarantorOperateInfo;

    private String guarantorFinanceInfo;

    private String guarantorOverallStatus;

    private String complianceAnalysis;

    private String trusteeDutyAnalysis;

    private String trusteeRiskAnalysis;

    private String transactionComplianceAnalysis;

    private String incomeCapRatio;

    // 业务类型
    private String dataType;

    // 创建时间
    private LocalDateTime createTime;
}