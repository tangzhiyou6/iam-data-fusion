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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_perpetual_bond")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtPerpetualBondPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    /** 批次号 */
    private String batchNo;

    private String projectName;

    private String businessType;

    private String companyName;

    private String companyRole;

    private String investContractCode;

    private String trustContractCode;

    private String trustContractName;

    private String repaymentOrder;

    private String hasInvestorPutRight;

    private String hasIssuerCallRight;

    private String hasUnconditionalPrincipalInterest;

    /** 业务类型（集合冗余标记） */
    private String dataType;

    /** 创建时间 */
    private LocalDateTime createTime;
}
