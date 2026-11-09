package com.hd.constants;

import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_CUSTODY_AGREEMENT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_MAJOR_RISK_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_MAJOR_RISK_WARNNING;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_OWN_BIS_DUE_DIG_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_PROJECT_DECISION_COMMITTEE;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_PROJECT_REVIEW_COMMENT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_TB_DUE_DIG_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_LOAN_CONTRACT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_BALANCE_SHEET;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_CASH_FLOW_STATEMENT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_INCOME_STATEMENT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_ENTITY;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_INFO;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_RELA;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_INFORMATION_DISCLOSURE_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_PAYMENT_ADVICE;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_REQUEST_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_PERPETUAL_BOND;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_TRUST_CONTRACT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_BRXT_TRUST_POST_INVESTMENT_REPORT;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_CWJD;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_FLJD;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_GZBG;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_HYJY;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_CXTZ_TZXY;
import static com.hd.constants.FusionBizNameConstants.BIZ_NAME_EAUC_FLWZ;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hd.model.vo.DataFusionBaseVo;
import com.hd.model.vo.ParseBrxtCustodyAgreementVo;
import com.hd.model.vo.ParseBrxtMajorRiskReportVo;
import com.hd.model.vo.ParseBrxtMajorRiskWarnningVo;
import com.hd.model.vo.ParseBrxtOwnBisDueDigReportVo;
import com.hd.model.vo.ParseBrxtProjectDecisionCommitteeVo;
import com.hd.model.vo.ParseBrxtProjectReviewCommentVo;
import com.hd.model.vo.ParseBrxtLoanContractVo;
import com.hd.model.vo.ParseBrxtBalanceSheetVo;
import com.hd.model.vo.ParseBrxtCashFlowStatementVo;
import com.hd.model.vo.ParseBrxtIncomeStatementVo;
import com.hd.model.vo.ParseBrxtInvestmentAgreementContractEntityVo;
import com.hd.model.vo.ParseBrxtInvestmentAgreementContractInfoVo;
import com.hd.model.vo.ParseBrxtInvestmentAgreementContractRelaVo;
import com.hd.model.vo.ParseBrxtInformationDisclosureReportVo;
import com.hd.model.vo.ParseBrxtPaymentAdviceVo;
import com.hd.model.vo.ParseBrxtRequestReportVo;
import com.hd.model.vo.ParseBrxtPerpetualBondVo;
import com.hd.model.vo.ParseBrxtTrustContractVo;
import com.hd.model.vo.ParseBrxtTrustPostInvestmentReportVo;
import com.hd.model.vo.ParseBrxtTtDueDigReportVo;
import com.hd.model.vo.ParseCxtzFddBalanceSheetVo;
import com.hd.model.vo.ParseCxtzFddCashFlowVo;
import com.hd.model.vo.ParseCxtzFddIncomeStatementVo;
import com.hd.model.vo.ParseCxtzInvValReportVo;
import com.hd.model.vo.ParseCxtzInvestmentAgreementBaseInfoVo;
import com.hd.model.vo.ParseCxtzInvestmentAgreementEntityVo;
import com.hd.model.vo.ParseCxtzInvestmentAgreementRelVo;
import com.hd.model.vo.ParseCxtzLddReportVo;
import com.hd.model.vo.ParseCxtzMeetingMinutesVo;
import com.hd.model.vo.ParseEaucWasteMaterialsVo;
import com.hd.service.impl.BrxtCustodyAgreementServiceImpl;
import com.hd.service.impl.BrxtMajorRiskReportServiceImpl;
import com.hd.service.impl.BrxtMajorRiskWarnningServiceImpl;
import com.hd.service.impl.BrxtOwnBisDueDigReportServiceImpl;
import com.hd.service.impl.BrxtProjectDecisionCommitteeServiceImpl;
import com.hd.service.impl.BrxtProjectReviewCommentServiceImpl;
import com.hd.service.impl.BrxtLoanContractServiceImpl;
import com.hd.service.impl.BrxtBalanceSheetServiceImpl;
import com.hd.service.impl.BrxtCashFlowStatementServiceImpl;
import com.hd.service.impl.BrxtIncomeStatementServiceImpl;
import com.hd.service.impl.BrxtInvestmentAgreementContractEntityServiceImpl;
import com.hd.service.impl.BrxtInvestmentAgreementContractInfoServiceImpl;
import com.hd.service.impl.BrxtInvestmentAgreementContractRelaServiceImpl;
import com.hd.service.impl.BrxtInformationDisclosureReportServiceImpl;
import com.hd.service.impl.BrxtPaymentAdviceServiceImpl;
import com.hd.service.impl.BrxtRequestReportServiceImpl;
import com.hd.service.impl.BrxtPerpetualBondServiceImpl;
import com.hd.service.impl.BrxtTrustContractServiceImpl;
import com.hd.service.impl.BrxtTrustPostInvestmentReportServiceImpl;
import com.hd.service.impl.BrxtTtDueDigReportServiceImpl;
import com.hd.service.impl.CxtzFddBalanceSheetServiceImpl;
import com.hd.service.impl.CxtzFddCashFlowServiceImpl;
import com.hd.service.impl.CxtzFddIncomeStatementServiceImpl;
import com.hd.service.impl.CxtzInvValReportServiceImpl;
import com.hd.service.impl.CxtzInvestmentAgreementBaseInfoServiceImpl;
import com.hd.service.impl.CxtzInvestmentAgreementEntityServiceImpl;
import com.hd.service.impl.CxtzInvestmentAgreementRelServiceImpl;
import com.hd.service.impl.CxtzLddReportServiceImpl;
import com.hd.service.impl.CxtzMeetingMinutesServiceImpl;
import com.hd.service.impl.EaucWasteMaterialsServiceImpl;

public enum DataFusionEnum {

    //创新投资
    CXTZ_GZBG(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_GZBG, new TypeReference<ParseCxtzInvValReportVo>(){}, CxtzInvValReportServiceImpl.class)
    ,CXTZ_CWJD_ZCFZ(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_CWJD, new TypeReference<ParseCxtzFddBalanceSheetVo>(){}, CxtzFddBalanceSheetServiceImpl.class)
    ,CXTZ_CWJD_LRB(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_CWJD, new TypeReference<ParseCxtzFddIncomeStatementVo>(){}, CxtzFddIncomeStatementServiceImpl.class)
    ,CXTZ_CWJD_XJLL(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_CWJD, new TypeReference<ParseCxtzFddCashFlowVo>(){}, CxtzFddCashFlowServiceImpl.class)
    ,CXTZ_FLJD(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_FLJD,  new TypeReference<ParseCxtzLddReportVo>(){}, CxtzLddReportServiceImpl.class)
    ,CXTZ_HYJY(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_HYJY, new TypeReference<ParseCxtzMeetingMinutesVo>(){}, CxtzMeetingMinutesServiceImpl.class)
    ,CXTZ_TZXY_JBXX(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_TZXY, new TypeReference<ParseCxtzInvestmentAgreementBaseInfoVo>(){}, CxtzInvestmentAgreementBaseInfoServiceImpl.class)
    ,CXTZ_TZXY_STXX(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_TZXY, new TypeReference<ParseCxtzInvestmentAgreementEntityVo>(){}, CxtzInvestmentAgreementEntityServiceImpl.class)
    ,CXTZ_TZXY_GXXX(FusionSysEnum.CXTZ, BIZ_NAME_CXTZ_TZXY,  new TypeReference<ParseCxtzInvestmentAgreementRelVo>(){}, CxtzInvestmentAgreementRelServiceImpl.class)

    //电能e拍
    ,EAUC_FLWZ(FusionSysEnum.EAUC, BIZ_NAME_EAUC_FLWZ,  new TypeReference<ParseEaucWasteMaterialsVo>(){}, EaucWasteMaterialsServiceImpl.class)

    //百瑞信托
    ,BRXT_PROJECT_REVIEW_COMMENT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_PROJECT_REVIEW_COMMENT, new TypeReference<ParseBrxtProjectReviewCommentVo>(){}, BrxtProjectReviewCommentServiceImpl.class)
    ,BRXT_MAJOR_RISK_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_MAJOR_RISK_REPORT, new TypeReference<ParseBrxtMajorRiskReportVo>(){}, BrxtMajorRiskReportServiceImpl.class)
    ,BRXT_MAJOR_RISK_WARNNING(FusionSysEnum.BRXT, BIZ_NAME_BRXT_MAJOR_RISK_WARNNING, new TypeReference<ParseBrxtMajorRiskWarnningVo>(){}, BrxtMajorRiskWarnningServiceImpl.class)
    ,BRXT_PROJECT_DECISION_COMMITTEE(FusionSysEnum.BRXT, BIZ_NAME_BRXT_PROJECT_DECISION_COMMITTEE, new TypeReference<ParseBrxtProjectDecisionCommitteeVo>(){}, BrxtProjectDecisionCommitteeServiceImpl.class)
    ,BRXT_BRXT_TT_DUE_DIG_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_TB_DUE_DIG_REPORT, new TypeReference<ParseBrxtTtDueDigReportVo>(){}, BrxtTtDueDigReportServiceImpl.class)
    ,BRXT_OWN_BIS_DUE_DIG_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_OWN_BIS_DUE_DIG_REPORT, new TypeReference<ParseBrxtOwnBisDueDigReportVo>(){}, BrxtOwnBisDueDigReportServiceImpl.class)
    ,BRXT_CUSTODY_AGREEMENT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_CUSTODY_AGREEMENT, new TypeReference<ParseBrxtCustodyAgreementVo>(){}, BrxtCustodyAgreementServiceImpl.class)
    ,BRXT_TRUST_CONTRACT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_TRUST_CONTRACT, new TypeReference<ParseBrxtTrustContractVo>(){}, BrxtTrustContractServiceImpl.class)
    ,BRXT_LOAN_CONTRACT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_LOAN_CONTRACT, new TypeReference<ParseBrxtLoanContractVo>(){}, BrxtLoanContractServiceImpl.class)
    ,BRXT_PERPETUAL_BOND(FusionSysEnum.BRXT, BIZ_NAME_BRXT_PERPETUAL_BOND, new TypeReference<ParseBrxtPerpetualBondVo>(){}, BrxtPerpetualBondServiceImpl.class)
    ,BRXT_TRUST_POST_INVESTMENT_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_TRUST_POST_INVESTMENT_REPORT, new TypeReference<ParseBrxtTrustPostInvestmentReportVo>(){}, BrxtTrustPostInvestmentReportServiceImpl.class)
    ,BRXT_PAYMENT_ADVICE(FusionSysEnum.BRXT, BIZ_NAME_BRXT_PAYMENT_ADVICE, new TypeReference<ParseBrxtPaymentAdviceVo>(){}, BrxtPaymentAdviceServiceImpl.class)
    ,BRXT_INFORMATION_DISCLOSURE_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_INFORMATION_DISCLOSURE_REPORT, new TypeReference<ParseBrxtInformationDisclosureReportVo>(){}, BrxtInformationDisclosureReportServiceImpl.class)
    ,BRXT_REQUEST_REPORT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_REQUEST_REPORT, new TypeReference<ParseBrxtRequestReportVo>(){}, BrxtRequestReportServiceImpl.class)
    ,BRXT_INCOME_STATEMENT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_INCOME_STATEMENT, new TypeReference<ParseBrxtIncomeStatementVo>(){}, BrxtIncomeStatementServiceImpl.class)
    ,BRXT_BALANCE_SHEET(FusionSysEnum.BRXT, BIZ_NAME_BRXT_BALANCE_SHEET, new TypeReference<ParseBrxtBalanceSheetVo>(){}, BrxtBalanceSheetServiceImpl.class)
    ,BRXT_CASH_FLOW_STATEMENT(FusionSysEnum.BRXT, BIZ_NAME_BRXT_CASH_FLOW_STATEMENT, new TypeReference<ParseBrxtCashFlowStatementVo>(){}, BrxtCashFlowStatementServiceImpl.class)
    ,BRXT_INVESTMENT_AGREEMENT_CONTRACT_INFO(FusionSysEnum.BRXT, BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_INFO, new TypeReference<ParseBrxtInvestmentAgreementContractInfoVo>(){}, BrxtInvestmentAgreementContractInfoServiceImpl.class)
    ,BRXT_INVESTMENT_AGREEMENT_CONTRACT_ENTITY(FusionSysEnum.BRXT, BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_ENTITY, new TypeReference<ParseBrxtInvestmentAgreementContractEntityVo>(){}, BrxtInvestmentAgreementContractEntityServiceImpl.class)
    ,BRXT_INVESTMENT_AGREEMENT_CONTRACT_RELA(FusionSysEnum.BRXT, BIZ_NAME_BRXT_INVESTMENT_AGREEMENT_CONTRACT_RELA, new TypeReference<ParseBrxtInvestmentAgreementContractRelaVo>(){}, BrxtInvestmentAgreementContractRelaServiceImpl.class)


    ;


    private final FusionSysEnum bizNameEnum;

    public final String bizName;

    private final TypeReference<? extends DataFusionBaseVo> typeRef;

    private final Class<? extends ServiceImpl> serviceClazz;

    DataFusionEnum(FusionSysEnum bizNameEnum, String bizName,
                   TypeReference<? extends DataFusionBaseVo> typeRef,
                   Class<? extends ServiceImpl> serviceClazz){
        this.bizNameEnum = bizNameEnum;
        this.typeRef = typeRef;
        this.serviceClazz = serviceClazz;
        this.bizName = bizName;
    }

    public static DataFusionEnum getByBizName(String bizName){
        DataFusionEnum[] values = DataFusionEnum.values();
        for (int i = 0; i < values.length; i++) {
            DataFusionEnum item = values[i];
            if (item.getBizName().equals(bizName)) {
                return item;
            }
        }
        return null;
    }


    public FusionSysEnum getBizNameEnum() {
        return bizNameEnum;
    }

    public TypeReference<? extends DataFusionBaseVo> getTypeRef() {
        return typeRef;
    }

    public Class<? extends ServiceImpl> getServiceClazz() {
        return serviceClazz;
    }

    public String getBizName() {
        return bizName;
    }
}
