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
 * 创新投资 - 会议纪要
 */

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_cxtz_jjyw_meeting_minutes")
@AllArgsConstructor
@NoArgsConstructor
public class CxtzMeetingMinutesPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceId;

    // 批次号
    private String batchNo;

    private String meetingName;

    private String meetingTime;

    private String proposalName;

    private String opinionContent;

    private String participantName;

    private String participantRole;

    private String participantPosition;

    private String dataType;

    // 创建时间
    private LocalDateTime createTime;
}
