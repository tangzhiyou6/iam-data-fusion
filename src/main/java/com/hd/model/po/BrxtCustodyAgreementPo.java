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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_custody_agreement")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtCustodyAgreementPo extends BasePo<Long>{

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String contractName;

    private String contractCode;

    private String trustee;

    private String custodian;

    private String signDate;

    private String custodianFeeBankInfo;

    private LocalDateTime dataTime;
}
