package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创新投资 - 估值报告
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_investment_project_valuation_report")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzInvValReportPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    // 项目名称
    private String projectName;

    //  评估基准日
    private String valuationBaseDate;

    // 报告日期
    private String reportIssueDate;

    // 估值单位
    private String valuationUnit;

    // 指标名称
    private String indicatorName;

    // 指标值
    private String indicatorValue;

    // 创建时间
    private LocalDateTime createTime;
}
