package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtInvestmentAgreementContractRelaPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 投资协议合同 - 关系信息（信托+固有）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtInvestmentAgreementContractRelaVo extends DataFusionBaseVo<BrxtInvestmentAgreementContractRelaPo, String> {

    private static final long serialVersionUID = 1L;

    @JsonProperty("trade_relation_list")
    private List<TradeRelation> tradeRelationList;

    @Override
    public List<BrxtInvestmentAgreementContractRelaPo> buildPo(String sourceDataId, String scene) {
        if (CollectionUtils.isEmpty(tradeRelationList)) {
            return Collections.emptyList();
        }
        String batchNo = UUID.randomUUID().toString();
        return tradeRelationList.stream().map(i -> {
            BrxtInvestmentAgreementContractRelaPo po = new BrxtInvestmentAgreementContractRelaPo();
            po.setSourceId(sourceDataId);
            po.setBatchNo(batchNo);
            po.setFrom(i.getFrom());
            po.setScene(scene);
            po.setTo(i.getTo());
            po.setAmount(i.getAmount());
            po.setCurrency(i.getCurrency());
            po.setRatio(i.getRatio());
            po.setRemark(i.getRemark());
            return po;
        }).collect(Collectors.toList());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRelation {
        @JsonProperty("from")
        private String from;
        @JsonProperty("to")
        private String to;
        @JsonProperty("amount")
        private String amount;
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("ratio")
        private String ratio;
        @JsonProperty("remark")
        private String remark;
    }
}
