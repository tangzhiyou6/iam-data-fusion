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
 * 创新投资 - 投资协议 - 关系信息
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_investment_agreement_rel")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzInvestmentAgreementRelPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String sourceData;

    private String targerData;

    private String amount;

    private String currency;

    private String ratio;

    private String remark;

    // 创建时间
    private LocalDateTime createTime;
}
