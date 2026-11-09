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
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_project_decision_committee")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtProjectDecisionCommitteePo extends BasePo<Long>{

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String topicName;

    /** 审议时间原文 */
    private String reviewTime;

    /** 会议地点原文 */
    private String meetingAddress;

    /** 召开方式原文 */
    private String meetingForm;

    /** 议题内容完整原文 */
    private String topicContent;

    /** 是否涉及关联交易原文结论 */
    private String isRelatedTransaction;

    /** 表决前提条件原文 */
    private String votePrecondition;

    /** 表决结果完整原文 */
    private String voteResult;

    private String name;

    /** 人员身份、代参会、缺席原因等完整描述 */
    private String role;

    // 创建时间
    private LocalDateTime createTime;
}
