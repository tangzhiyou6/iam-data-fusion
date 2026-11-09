package com.hd.model.vo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtRequestReportPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信托项目请示报告（信托）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtRequestReportVo extends DataFusionBaseVo<BrxtRequestReportPo, String> {

    private static final long serialVersionUID = 1L;

    /** 信托项目完整名称 */
    @JsonProperty("project_name")
    private String projectName;

    /** 项目成立时间原文 */
    @JsonProperty("establish_time")
    private String establishTime;

    /** 项目类型描述 */
    @JsonProperty("project_type")
    private String projectType;

    /** 是否符合资管新规原文结论 */
    @JsonProperty("comply_asset_management_rule")
    private String complyAssetManagementRule;

    @Override
    public List<BrxtRequestReportPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        BrxtRequestReportPo po = new BrxtRequestReportPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setProjectName(this.projectName);
        po.setEstablishTime(this.establishTime);
        po.setProjectType(this.projectType);
        po.setComplyAssetManagementRule(this.complyAssetManagementRule);
        return Collections.singletonList(po);
    }
}
