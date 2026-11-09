package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 百瑞信托 - 财务报告 - 资产负债表（固有+信托）
 */
@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_balance_sheet")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtBalanceSheetPo extends BasePo<Long> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    /** 批次号 */
    private String batchNo;

    private String companyName;

    private String projectName;

    /** 财务统计期间（对应 JSON date） */
    @TableField("\"date\"")
    private String date;

    private String indicatorName;

    private String indicatorValue;

    /** 创建时间 */
    private LocalDateTime createTime;
}
