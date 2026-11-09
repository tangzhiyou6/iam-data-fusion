package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtTrustContractPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtTrustContractVo extends DataFusionBaseVo<BrxtTrustContractPo, String> {

    private static final long serialVersionUID = 1L;

    /** 信托合同完整名称 */
    @JsonProperty("contract_name")
    private String contractName;

    /** 合同编码 */
    @JsonProperty("contract_code")
    private String contractCode;

    /** 受托人主体名称 */
    @JsonProperty("trustee")
    private String trustee;

    /** 合同签署日期 */
    @JsonProperty("sign_date")
    private String signDate;

    /** 信托规模原文描述 */
    @JsonProperty("trust_scale")
    private String trustScale;

    /** 预计期限原文描述 */
    @JsonProperty("expected_term")
    private String expectedTerm;

    @Override
    public List<BrxtTrustContractPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtTrustContractPo po = new BrxtTrustContractPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setContractName(this.contractName);
        po.setContractCode(this.contractCode);
        po.setTrustee(this.trustee);
        po.setSignDate(this.signDate);
        po.setTrustScale(this.trustScale);
        po.setExpectedTerm(this.expectedTerm);
        return Collections.singletonList(po);
    }
}