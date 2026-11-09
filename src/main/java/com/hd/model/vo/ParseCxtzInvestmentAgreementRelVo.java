package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzInvestmentAgreementRelPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 创新投资-投资协议-交易关系解析VO
 * 用于接收和映射LLM或接口返回的投资协议交易关系JSON结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzInvestmentAgreementRelVo extends DataFusionBaseVo<CxtzInvestmentAgreementRelPo, String> {

    /**
     * 交易关系列表
     */
    @JsonProperty("trade_relation_list")
    private List<TradeRelation> tradeRelationList;

    @Override
    public List<CxtzInvestmentAgreementRelPo> buildPo(String sourceDataId, String scene) {
        if (CollectionUtils.isEmpty(tradeRelationList)) {
            return Collections.EMPTY_LIST;
        }
        String batchNo = UUID.randomUUID().toString();
        return tradeRelationList.stream().map(i -> {
            CxtzInvestmentAgreementRelPo po = new CxtzInvestmentAgreementRelPo();
            po.setSourceId(sourceDataId);
            po.setBatchNo(batchNo);
            po.setSourceData(i.getFrom());
            po.setTargerData(i.getTo());
            po.setAmount(i.getAmount());
            po.setCurrency(i.getCurrency());
            po.setRatio(i.getRatio());
            po.setRemark(i.getRemark());
            return po;
        }).collect(Collectors.toList());
    }

    /**
     * 交易关系内部类
     * 对应JSON中的 trade_relation_list 数组元素
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRelation {
        /**
         * 流出主体全称（投资方/资金来源方）
         */
        @JsonProperty("from")
        private String from;

        /**
         * 流入主体全称（被投方/资金接收方）
         */
        @JsonProperty("to")
        private String to;

        /**
         * 交易金额
         */
        @JsonProperty("amount")
        private String amount;

        /**
         * 币种
         */
        @JsonProperty("currency")
        private String currency;

        /**
         * 股权占比
         */
        @JsonProperty("ratio")
        private String ratio;

        /**
         * 备注（枚举值【资金细分拆分原文】或原文摘要）
         */
        @JsonProperty("remark")
        private String remark;
    }
}