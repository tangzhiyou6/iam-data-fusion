package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzLddReportPo;

public class ParseCxtzLddReportVo  extends DataFusionBaseVo<CxtzLddReportPo, String>{
    /**
     * 被投企业全称
     */
    @JsonProperty("invested_company_name")
    private String investedCompanyName;

    /**
     * 完整投资项目名称
     */
    @JsonProperty("project_full_name")
    private String projectFullName;

    /**
     * 法律尽调报告出具时间
     */
    @JsonProperty("legal_report_date")
    private String legalReportDate;

    @Override
    public List<CxtzLddReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        CxtzLddReportPo po = new CxtzLddReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setInvestedCompanyName(this.investedCompanyName);
        po.setProjectFullName(this.projectFullName);
        po.setLegalReportDate(this.legalReportDate);
        return Collections.singletonList(po);
    }
}
