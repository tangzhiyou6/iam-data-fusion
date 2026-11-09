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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_trust_bis_due_dig_report")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtTtDueDigReportPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String projectTrustName;

    /*private String projectType;*/

    private String investigateTarget;

    private String investigateDate;

    private String productType;

    private String riskLevel;

    private String targetCompany;

    private String investAppropriateness;

    private String riskIsolation;

    private String prioritySubordinate;

    private String differenceComplement;

    private String infoDisclosureSupport;

    private String complyRegulation;

    private String counterpartyRating;

    private String exitPath;

    // 创建时间
    private LocalDateTime createTime;
}
