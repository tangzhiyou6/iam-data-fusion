package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtCustodyAgreementPo;
import lombok.Data;

@Data
public class ParseBrxtCustodyAgreementVo extends DataFusionBaseVo<BrxtCustodyAgreementPo, String> {

    private static final long serialVersionUID = 1L;

    /** 保管协议完整名称 */
    @JsonProperty("contract_name")
    private String contractName;

    /** 合同编码 */
    @JsonProperty("contract_code")
    private String contractCode;

    /** 受托人主体名称 */
    @JsonProperty("trustee")
    private String trustee;

    /** 保管机构完整名称 */
    @JsonProperty("custodian")
    private String custodian;

    /** 协议签订日期 */
    @JsonProperty("sign_date")
    private String signDate;

    /** 保管费收款账户完整原文信息 */
    @JsonProperty("custodian_fee_bank_info")
    private String custodianFeeBankInfo;

    @Override
    public List<BrxtCustodyAgreementPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtCustodyAgreementPo po = new BrxtCustodyAgreementPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setContractName(this.contractName);
        po.setContractCode(this.contractCode);
        po.setTrustee(this.trustee);
        po.setCustodian(this.custodian);
        po.setSignDate(this.signDate);
        po.setCustodianFeeBankInfo(this.custodianFeeBankInfo);
        return Collections.singletonList(po);
    }
}
