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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_loan_contract")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtLoanContractPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    /** 批次号 */
    private String batchNo;

    private String contractName;

    private String contractCode;

    private String borrower;

    private String lender;

    private String signDate;

    private String loanFundSource;

    private String trustPlanName;

    private String loanType;

    private String loanPrincipal;

    private String loanTerm;

    private String loanRate;

    private String borrowerAccountName;

    private String borrowerBank;

    private String borrowerAccountNo;

    private String lenderAccountName;

    private String lenderBank;

    private String lenderAccountNo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
