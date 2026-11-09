package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_own_bis_due_dig_report")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtOwnBisDueDigReportPo extends BasePo<Long> {


    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String projectTrustName;

/*    private String projectType;*/

    private String submitDepartment;

    private String submitDate;

    private String irr;

    private String roi;

    private String coverCapitalCost;

    private String liquidityMatch;

    private String investTerm;

    private String hasFinancialStatement;

    // 创建时间
    private LocalDateTime createTime;
}
