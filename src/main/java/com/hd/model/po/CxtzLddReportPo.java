package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创新投资 - 法律尽调报告
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_legal_due_diligence_report")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzLddReportPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String investedCompanyName;

    private String  projectFullName;

    private String  legalReportDate;

    // 创建时间
    private LocalDateTime createTime;
}
