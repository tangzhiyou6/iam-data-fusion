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
 * 百瑞信托 - 投资协议合同 - 基本信息（信托+固有）
 */
@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_investment_agreement_contract_info")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtInvestmentAgreementContractInfoPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    private String batchNo;

    private String scene;

    private String indicatorName;

    private String indicatorValue;

    private LocalDateTime createTime;
}
