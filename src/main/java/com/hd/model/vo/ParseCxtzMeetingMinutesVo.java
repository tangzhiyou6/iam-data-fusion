package com.hd.model.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzMeetingMinutesPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 创新投资-会议纪要解析VO
 * 用于接收和映射LLM或接口返回的会议纪要JSON结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzMeetingMinutesVo extends DataFusionBaseVo<CxtzMeetingMinutesPo, String> {

    /**
     * 会议纪要全称
     */
    @JsonProperty("meeting_name")
    private String meetingName;

    /**
     * 会议举办时间
     */
    @JsonProperty("meeting_time")
    private String meetingTime;

    /**
     * 议案列表
     */
    @JsonProperty("proposals")
    private List<Proposal> proposals;

    /**
     * 参会人列表
     */
    @JsonProperty("participants")
    private List<Participant> participants;

    @Override
    public List<CxtzMeetingMinutesPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<CxtzMeetingMinutesPo> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(proposals)) {
            proposals.stream().forEach(i -> {
                CxtzMeetingMinutesPo cxtzMeetingMinutesPo = buildCommonPo(sourceDataId, batchNo);
                cxtzMeetingMinutesPo.setProposalName(i.getProposalName());
                cxtzMeetingMinutesPo.setOpinionContent(i.getOpinionContent());
                cxtzMeetingMinutesPo.setDataType("proposals");
                result.add(cxtzMeetingMinutesPo);
            });
        }

        if (!CollectionUtils.isEmpty(participants)) {
            participants.stream().forEach(i -> {
                CxtzMeetingMinutesPo cxtzMeetingMinutesPo = buildCommonPo(sourceDataId, batchNo);
                cxtzMeetingMinutesPo.setParticipantName(i.getName());
                cxtzMeetingMinutesPo.setParticipantRole(i.getRole());
                cxtzMeetingMinutesPo.setParticipantPosition(i.getPosition());
                cxtzMeetingMinutesPo.setDataType("participants");
                result.add(cxtzMeetingMinutesPo);
            });
        }
        return Collections.emptyList();
    }

    private CxtzMeetingMinutesPo buildCommonPo(String sourceDataId, String batchNo){
        CxtzMeetingMinutesPo po = new CxtzMeetingMinutesPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setMeetingName(this.meetingName);
        po.setMeetingTime(this.meetingTime);
        return po;
    }

    /**
     * 议案内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Proposal {
        /**
         * 议案完整名称
         */
        @JsonProperty("proposal_name")
        private String proposalName;

        /**
         * 审议意见内容
         */
        @JsonProperty("opinion_content")
        private String opinionContent;
    }

    /**
     * 参会人内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        /**
         * 人员姓名
         */
        @JsonProperty("name")
        private String name;

        /**
         * 人员角色/部门
         */
        @JsonProperty("role")
        private String role;

        /**
         * 具体职位
         */
        @JsonProperty("position")
        private String position;
    }
}