package com.hd.model.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtInvestmentAgreementContractInfoPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 投资协议合同 - 基本信息（信托+固有）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtInvestmentAgreementContractInfoVo extends DataFusionBaseVo<BrxtInvestmentAgreementContractInfoPo, String> {

    private static final long serialVersionUID = 1L;

    @JsonProperty("base_indicators")
    private List<BaseIndicator> baseIndicators;

    @Override
    public List<BrxtInvestmentAgreementContractInfoPo> buildPo(String sourceDataId, String scene) {
        if (CollectionUtils.isEmpty(baseIndicators)) {
            return Collections.emptyList();
        }
        String batchNo = UUID.randomUUID().toString();
        List<BrxtInvestmentAgreementContractInfoPo> pos = new ArrayList<>();
        for (BaseIndicator baseIndicator : baseIndicators) {
            BrxtInvestmentAgreementContractInfoPo po = new BrxtInvestmentAgreementContractInfoPo();
            po.setSourceId(sourceDataId);
            po.setBatchNo(batchNo);
            po.setScene(scene);
            po.setIndicatorName(baseIndicator.getIndicatorName());
            po.setIndicatorValue(baseIndicator.getIndicatorValue());
            pos.add(po);
        }
        return pos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseIndicator {
        @JsonProperty("indicator_name")
        private String indicatorName;

        @JsonProperty("indicator_value")
        private String indicatorValue;
    }
}
