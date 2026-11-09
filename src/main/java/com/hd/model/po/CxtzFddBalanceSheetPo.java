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
 * 创新投资 - 财务尽调 - 资产负债表
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_fdd_balance_sheet ")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzFddBalanceSheetPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String  companyName;

    private String reportDate;

    private String workEndDate;

    private String periodDate;

    private String indicatorName;

    private String indicatorValue;

    // 创建时间
    private LocalDateTime createTime;
}
