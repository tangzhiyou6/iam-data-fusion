package com.hd.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtProjectDecisionCommitteePo;
import lombok.Data;
import org.springframework.util.CollectionUtils;


@Data
public class ParseBrxtProjectDecisionCommitteeVo extends DataFusionBaseVo<BrxtProjectDecisionCommitteePo, String> {

    @JsonProperty("topic_name")
    private String topicName;

    /** 审议时间原文 */
    @JsonProperty("review_time")
    private String reviewTime;

    /** 会议地点原文 */
    @JsonProperty("meeting_address")
    private String meetingAddress;

    /** 召开方式原文 */
    @JsonProperty("meeting_form")
    private String meetingForm;

    /** 议题内容完整原文 */
    @JsonProperty("topic_content")
    private String topicContent;

    /** 是否涉及关联交易原文结论 */
    @JsonProperty("is_related_transaction")
    private String isRelatedTransaction;

    /** 表决前提条件原文 */
    @JsonProperty("vote_precondition")
    private String votePrecondition;

    /** 表决结果完整原文 */
    @JsonProperty("vote_result")
    private String voteResult;

    /** 参会人员列表 */
    @JsonProperty("meeting_member_list")
    private List<MeetingMember> meetingMemberList;

    @Data
    public static class MeetingMember implements Serializable {

        /** 人员姓名 */
        @JsonProperty("name")
        private String name;

        /** 人员身份、代参会、缺席原因等完整描述 */
        @JsonProperty("role")
        private String role;
    }

    @Override
    public List<BrxtProjectDecisionCommitteePo> buildPo(String sourceDataId, String scene) {
        List<BrxtProjectDecisionCommitteePo> list = new ArrayList<>();
        String batchNo = UUID.randomUUID().toString();
        if (CollectionUtils.isEmpty(meetingMemberList)) {
            BrxtProjectDecisionCommitteePo po = this.buildCommonPo(sourceDataId, batchNo);
            list.add(po);
        } else {
            for (MeetingMember meetingMember : meetingMemberList) {
                BrxtProjectDecisionCommitteePo po = this.buildCommonPo(sourceDataId, batchNo);
                po.setName(meetingMember.getName());
                po.setRole(meetingMember.getRole());
                list.add(po);
            }
        }
        return list;
    }


    private BrxtProjectDecisionCommitteePo buildCommonPo(String sourceDataId, String batchNo){
        BrxtProjectDecisionCommitteePo po = new BrxtProjectDecisionCommitteePo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setTopicName(this.topicName);
        po.setReviewTime(this.reviewTime);
        po.setMeetingAddress(this.meetingAddress);
        po.setMeetingForm(this.meetingForm);
        po.setTopicContent(this.topicContent);
        po.setIsRelatedTransaction(this.isRelatedTransaction);
        po.setVotePrecondition(this.votePrecondition);
        po.setVoteResult(this.voteResult);
        return po;
    }

}
