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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_trust_post_investment_report")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtTrustPostInvestmentReportPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    /** 批次号 */
    private String batchNo;

    private String projectName;

    private String serviceType;

    private String establishDate;

    private String outstandingScale;

    private String reportDate;

    private String riskDisclosure;

    private String currentPeriodIncome;

    private String yearToDateIncome;

    private String yearToDateNetProfit;

    private String currentPeriodDistributedProfit;

    /** 创建时间 */
    private LocalDateTime createTime;
}
