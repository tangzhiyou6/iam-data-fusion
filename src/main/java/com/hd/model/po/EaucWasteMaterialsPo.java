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
 * 创新投资 - 废料物资
 */
@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_dnyp_waste_materials")
@AllArgsConstructor
@NoArgsConstructor
public class EaucWasteMaterialsPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String fileName;

    private String declareCompany;

    private String materialName;

    private String quantity;

    private String unit;

    private String specification;

    private String estimatedWeight;
    // 创建时间
    private LocalDateTime createTime;
}
