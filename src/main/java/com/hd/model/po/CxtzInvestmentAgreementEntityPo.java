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
 * 创新投资 - 投资协议 - 实体信息
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_investment_agreement_entity")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzInvestmentAgreementEntityPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String accountNo;
    private String accountName;
    private String bankName;
    private String payType;
    private String payDeadline;


    private String companyName;
    private String companyRole;
    private String creditCode;
    private String address;


    private String name;
    private String phone;
    private String idCard;
    private String personRole;
    private String relatedSubject;

    private String dataType;

    // 创建时间
    private LocalDateTime createTime;
}
