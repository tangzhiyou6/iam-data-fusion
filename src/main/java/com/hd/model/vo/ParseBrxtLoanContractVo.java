package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtLoanContractPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtLoanContractVo extends DataFusionBaseVo<BrxtLoanContractPo, String> {

    private static final long serialVersionUID = 1L;

    /** 借款合同完整名称 */
    @JsonProperty("contract_name")
    private String contractName;

    /** 合同编码 */
    @JsonProperty("contract_code")
    private String contractCode;

    /** 借款人主体全称 */
    @JsonProperty("borrower")
    private String borrower;

    /** 贷款人主体全称 */
    @JsonProperty("lender")
    private String lender;

    /** 合同签署日期 */
    @JsonProperty("sign_date")
    private String signDate;

    /** 贷款资金来源原文 */
    @JsonProperty("loan_fund_source")
    private String loanFundSource;

    /** 信托计划完整名称 */
    @JsonProperty("trust_plan_name")
    private String trustPlanName;

    /** 贷款种类 */
    @JsonProperty("loan_type")
    private String loanType;

    /** 贷款本金完整原文 */
    @JsonProperty("loan_principal")
    private String loanPrincipal;

    /** 贷款期限原文 */
    @JsonProperty("loan_term")
    private String loanTerm;

    /** 贷款利率原文 */
    @JsonProperty("loan_rate")
    private String loanRate;

    /** 借款人户名 */
    @JsonProperty("borrower_account_name")
    private String borrowerAccountName;

    /** 借款人开户银行 */
    @JsonProperty("borrower_bank")
    private String borrowerBank;

    /** 借款人银行账号 */
    @JsonProperty("borrower_account_no")
    private String borrowerAccountNo;

    /** 贷款人户名 */
    @JsonProperty("lender_account_name")
    private String lenderAccountName;

    /** 贷款人开户银行 */
    @JsonProperty("lender_bank")
    private String lenderBank;

    /** 贷款人银行账号 */
    @JsonProperty("lender_account_no")
    private String lenderAccountNo;

    @Override
    public List<BrxtLoanContractPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtLoanContractPo po = new BrxtLoanContractPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setContractName(this.contractName);
        po.setContractCode(this.contractCode);
        po.setBorrower(this.borrower);
        po.setLender(this.lender);
        po.setSignDate(this.signDate);
        po.setLoanFundSource(this.loanFundSource);
        po.setTrustPlanName(this.trustPlanName);
        po.setLoanType(this.loanType);
        po.setLoanPrincipal(this.loanPrincipal);
        po.setLoanTerm(this.loanTerm);
        po.setLoanRate(this.loanRate);
        po.setBorrowerAccountName(this.borrowerAccountName);
        po.setBorrowerBank(this.borrowerBank);
        po.setBorrowerAccountNo(this.borrowerAccountNo);
        po.setLenderAccountName(this.lenderAccountName);
        po.setLenderBank(this.lenderBank);
        po.setLenderAccountNo(this.lenderAccountNo);
        return Collections.singletonList(po);
    }
}
