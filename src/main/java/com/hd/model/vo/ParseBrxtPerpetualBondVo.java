package com.hd.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtPerpetualBondPo;
import lombok.Data;
import org.springframework.util.CollectionUtils;

/**
 * 永续债发行文件（固有+信托）
 */
@Data
public class ParseBrxtPerpetualBondVo extends DataFusionBaseVo<BrxtPerpetualBondPo, String> {

    private static final long serialVersionUID = 1L;

    /** 永续债项目完整名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 信托业务/固有业务/未识别 */
    @JsonProperty("business_type")
    private String businessType;

    /** 交易对手方列表 */
    @JsonProperty("transaction_parties")
    private List<TransactionParty> transactionParties;

    /** 投资合同编号 */
    @JsonProperty("invest_contract_code")
    private String investContractCode;

    /** 信托合同编号 */
    @JsonProperty("trust_contract_code")
    private String trustContractCode;

    /** 信托合同完整名称 */
    @JsonProperty("trust_contract_name")
    private String trustContractName;

    /** 清偿顺序原文描述 */
    @JsonProperty("repayment_order")
    private String repaymentOrder;

    /** 是/否 */
    @JsonProperty("has_investor_put_right")
    private String hasInvestorPutRight;

    /** 是/否 */
    @JsonProperty("has_issuer_call_right")
    private String hasIssuerCallRight;

    /** 是/否 */
    @JsonProperty("has_unconditional_principal_interest")
    private String hasUnconditionalPrincipalInterest;

    @Override
    public List<BrxtPerpetualBondPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<BrxtPerpetualBondPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(transactionParties)) {
            for (TransactionParty party : transactionParties) {
                BrxtPerpetualBondPo po = buildCommonPo(sourceDataId, batchNo);
                po.setCompanyName(party.getCompanyName());
                po.setCompanyRole(party.getCompanyRole());
                po.setDataType("transaction_parties");
                pos.add(po);
            }
        }
        if (CollectionUtils.isEmpty(pos)) {
            pos.add(buildCommonPo(sourceDataId, batchNo));
        }
        return pos;
    }

    private BrxtPerpetualBondPo buildCommonPo(String sourceDataId, String batchNo) {
        BrxtPerpetualBondPo po = new BrxtPerpetualBondPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setBusinessType(this.businessType);
        po.setInvestContractCode(this.investContractCode);
        po.setTrustContractCode(this.trustContractCode);
        po.setTrustContractName(this.trustContractName);
        po.setRepaymentOrder(this.repaymentOrder);
        po.setHasInvestorPutRight(this.hasInvestorPutRight);
        po.setHasIssuerCallRight(this.hasIssuerCallRight);
        po.setHasUnconditionalPrincipalInterest(this.hasUnconditionalPrincipalInterest);
        return po;
    }

    /**
     * 交易对手方
     */
    @Data
    public static class TransactionParty implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 主体全称 */
        @JsonProperty("company_name")
        private String companyName;

        /** 角色（投资方/被投资方） */
        @JsonProperty("company_role")
        private String companyRole;
    }
}
