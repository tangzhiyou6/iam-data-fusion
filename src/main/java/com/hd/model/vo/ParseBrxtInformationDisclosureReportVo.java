package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtInformationDisclosureReportPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信息披露报告（信托）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtInformationDisclosureReportVo extends DataFusionBaseVo<BrxtInformationDisclosureReportPo, String> {

    private static final long serialVersionUID = 1L;

    /** 信托项目完整名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 项目成立时间原文 */
    @JsonProperty("establish_time")
    private String establishTime;

    /** 存续规模原文 */
    @JsonProperty("outstanding_scale")
    private String outstandingScale;

    /** 信托项目信用等级 */
    @JsonProperty("trust_credit_rating")
    private String trustCreditRating;

    @Override
    public List<BrxtInformationDisclosureReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtInformationDisclosureReportPo po = new BrxtInformationDisclosureReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setEstablishTime(this.establishTime);
        po.setOutstandingScale(this.outstandingScale);
        po.setTrustCreditRating(this.trustCreditRating);
        return Collections.singletonList(po);
    }
}
