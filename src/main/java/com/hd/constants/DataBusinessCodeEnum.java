package com.hd.constants;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum DataBusinessCodeEnum {


    BRXT_FILE_REQUEST_ITEM_DECISION_APPROVAL_FLOW(
            FusionSysEnum.BRXT, "请示事项决策审批流程", "v76ce68f9ad411e883d900505688bf34", "项目-请示审批与决策流程/请示事项决策审批流程"
    ),
    BRXT_FILE_OWN_REQUEST_ITEM_DECISION_APPROVAL_FLOW(
            FusionSysEnum.BRXT, "固有请示事项决策审批流程", "t825528fb58c11e89611c85b76568133", "项目-请示审批与决策流程/固有请示事项决策审批流程"
    ),
    BRXT_FILE_PASSED_PROJECT_LEGAL_REVIEW_SEAL_FLOW_V30(
            FusionSysEnum.BRXT, "过会类项目法律文本审核及用印流程-V3.0", "q762eff6134f11ef886600505688bf63", "过会类项目法律文本审核及用印流程/过会类项目法律文本审核及用印流程-V3.0"
    ),
    BRXT_FILE_OWN_PASSED_PROJECT_LEGAL_REVIEW_SEAL_FLOW_V30(
            FusionSysEnum.BRXT, "固有过会类项目法律文本审核及用印流程-V3.0", "oa73358cd24d11efaf24005056889512", "过会类项目法律文本审核及用印流程/固有过会类项目法律文本审核及用印流程-V3.0"
    ),
    BRXT_FILE_MEETING_MINUTES_FLOW(
            FusionSysEnum.BRXT, "会议纪要流程", "p4cfd7c6abf611e88d4400505688f972", "项目决策会会议纪要/会议纪要流程"
    ),
    BRXT_FILE_BUSINESS_RISK_SIGNAL_HANDLE_FLOW_V20(
            FusionSysEnum.BRXT, "业务风险信号处理流程_2.0", "p0d1086cee7911eab64d005056882405", "业务风险信号处理流程/业务风险信号处理流程_2.0"
    ),
    BRXT_FILE_OWN_BUSINESS_RISK_SIGNAL_HANDLE_FLOW_V20(
            FusionSysEnum.BRXT, "固有业务风险信号处理流程_2.0", "tad9b5a6f33011ea88a4005056882405", "业务风险信号处理流程/固有业务风险信号处理流程_2.0"
    ),
    BRXT_FILE_OWN_PROJECT_REVIEW_DECISION_FLOW(
            FusionSysEnum.BRXT, "固有项目审核及决策流程", "f7a140b0b33c11e8856bc85b76568133", "项目审核及决策流程/固有项目审核及决策流程"
    ),
    BRXT_FILE_PROJECT_REVIEW_DECISION_FLOW(
            FusionSysEnum.BRXT, "项目审核及决策流程", "sbbceccf1c3011e7816d8fafc29309c7", "项目审核及决策流程/项目审核及决策流程"
    ),
    BRXT_FILE_INCOME_EXPENSE_PLAN_GROUP_LEDGER(FusionSysEnum.BRXT,"收支计划管理-分组台账","", "收支计划管理-分组台账");

    private final FusionSysEnum sysEnum;

    private final String bizName;

    private final String businessCode;

    private final String localPath;

    DataBusinessCodeEnum(FusionSysEnum sysEnum, String bizName, String businessCode, String localPath) {
        this.sysEnum = sysEnum;
        this.businessCode = businessCode;
        this.localPath = localPath;
        this.bizName = bizName;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getBizName() {
        return bizName;
    }

    public FusionSysEnum getSysEnum() {
        return sysEnum;
    }

    public static DataBusinessCodeEnum findBySysAndBizName(FusionSysEnum sysEnum, String bizName) {
        if (null == sysEnum || StringUtils.isEmpty(bizName)) {
            return null;
        }
        for (DataBusinessCodeEnum item: DataBusinessCodeEnum.values()) {
            if (sysEnum == item.getSysEnum() && item.getBizName().equalsIgnoreCase(bizName)) {
                return item;
            }
        }
        return null;
    }

    public static List<DataBusinessCodeEnum> findBySys(FusionSysEnum sysEnum) {
        List<DataBusinessCodeEnum> list = new ArrayList<>();
        if (null == sysEnum) {
            return list;
        }
        for (DataBusinessCodeEnum item: DataBusinessCodeEnum.values()) {
            if (sysEnum == item.getSysEnum()) {
                list.add(item);
            }
        }
        return list;
    }
}
