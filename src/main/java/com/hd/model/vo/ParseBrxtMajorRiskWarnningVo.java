package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtMajorRiskWarnningPo;
import lombok.Data;

@Data
public class ParseBrxtMajorRiskWarnningVo extends DataFusionBaseVo<BrxtMajorRiskWarnningPo, String> {

    @JsonProperty("report_name")
    private String reportName;

    @JsonProperty("report_date")
    private String reportDate;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("belong_department")
    private String belongDepartment;

    @JsonProperty("risk_description")
    private String riskDescription;

    @JsonProperty("handling_suggestion")
    private String handlingSuggestion;

    @Override
    public List<BrxtMajorRiskWarnningPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtMajorRiskWarnningPo po = new BrxtMajorRiskWarnningPo();
        po.setBatchNo(batchNo);
        po.setSourceId(sourceDataId);
        po.setReportName(this.reportName);
        po.setReportDate(this.reportDate);
        po.setProjectName(this.getProjectName());
        po.setBelongDepartment(this.getBelongDepartment());
        po.setRiskDescription(this.riskDescription);
        po.setHandlingSuggestion(this.getHandlingSuggestion());
        return Collections.singletonList(po);
    }
}