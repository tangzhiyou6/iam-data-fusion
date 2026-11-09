package com.hd.model.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzInvestmentAgreementBaseInfoPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 创新投资-投资协议-基本信息解析VO
 * 用于接收和映射LLM或接口返回的投资协议基本信息JSON结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzInvestmentAgreementBaseInfoVo extends DataFusionBaseVo<CxtzInvestmentAgreementBaseInfoPo, String> {

    /**
     * 基础指标列表
     */
    @JsonProperty("base_indicators")
    private List<BaseIndicator> baseIndicators;

    @Override
    public List<CxtzInvestmentAgreementBaseInfoPo> buildPo(String sourceDataId, String scene) {
        if (CollectionUtils.isEmpty(baseIndicators)) {
            return Collections.EMPTY_LIST;
        }
        String batchNo = UUID.randomUUID().toString();
        List<CxtzInvestmentAgreementBaseInfoPo> pos = new ArrayList<>();
        for (BaseIndicator baseIndicator : baseIndicators) {
            CxtzInvestmentAgreementBaseInfoPo po = new CxtzInvestmentAgreementBaseInfoPo();
            po.setSourceId(sourceDataId);
            po.setBatchNo(batchNo);
            po.setIndicatorValue(baseIndicator.getIndicatorValue());
            po.setIndicatorName(baseIndicator.getIndicatorName());
        }
        return pos;
    }

    /**
     * 基础指标内部类
     * 对应JSON中的 base_indicators 数组元素
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseIndicator {
        /**
         * 指标名称
         */
        @JsonProperty("indicator_name")
        private String indicatorName;

        /**
         * 指标数值
         */
        @JsonProperty("indicator_value")
        private String indicatorValue;
    }
}