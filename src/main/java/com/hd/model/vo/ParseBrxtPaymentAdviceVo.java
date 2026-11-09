package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtPaymentAdvicePo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信托项目付款通知单（信托）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtPaymentAdviceVo extends DataFusionBaseVo<BrxtPaymentAdvicePo, String> {

    private static final long serialVersionUID = 1L;

    /** 信托项目完整名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 合同编号 */
    @JsonProperty("contract_code")
    private String contractCode;

    /** 付款截止日期原文 */
    @JsonProperty("payment_deadline")
    private String paymentDeadline;

    /** 应付合计金额原文 */
    @JsonProperty("total_payable_amount")
    private String totalPayableAmount;

    @Override
    public List<BrxtPaymentAdvicePo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtPaymentAdvicePo po = new BrxtPaymentAdvicePo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setContractCode(this.contractCode);
        po.setPaymentDeadline(this.paymentDeadline);
        po.setTotalPayableAmount(this.totalPayableAmount);
        return Collections.singletonList(po);
    }
}
