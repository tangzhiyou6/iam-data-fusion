package com.hd.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.CxtzInvestmentAgreementEntityPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 创新投资-投资协议-签约主体解析VO
 * 用于接收和映射LLM或接口返回的投资协议签约主体JSON结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseCxtzInvestmentAgreementEntityVo extends DataFusionBaseVo<CxtzInvestmentAgreementEntityPo, String> {

    /**
     * 账户列表
     */
    @JsonProperty("account_list")
    private List<Account> accountList;

    /**
     * 企业列表
     */
    @JsonProperty("company_list")
    private List<Company> companyList;

    /**
     * 个人列表
     */
    @JsonProperty("person_list")
    private List<Person> personList;

    @Override
    public List<CxtzInvestmentAgreementEntityPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<CxtzInvestmentAgreementEntityPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountList)) {
            pos.addAll(
                    accountList.stream().map(i -> {
                        CxtzInvestmentAgreementEntityPo po = buildCommon(sourceDataId, batchNo, "ACCOUNT");
                        po.setAccountNo(i.getAccountNo());
                        po.setAccountName(i.getAccountName());
                        po.setBankName(i.getBankName());
                        po.setPayType(i.getPayType());
                        po.setPayDeadline(i.getPayDeadline());
                        return po;
                    }).collect(Collectors.toList())
            );
        }

        if (!CollectionUtils.isEmpty(companyList)) {
            pos.addAll(
                    companyList.stream().map(i -> {
                        CxtzInvestmentAgreementEntityPo po = buildCommon(sourceDataId, batchNo, "COMPANY");
                        po.setCompanyName(i.getCompanyName());
                        po.setCompanyRole(i.getCompanyRole());
                        po.setCreditCode(i.getCreditCode());
                        po.setAddress(i.getAddress());
                        return po;
                    }).collect(Collectors.toList())
            );
        }

        if (!CollectionUtils.isEmpty(personList)) {
            pos.addAll(
                    personList.stream().map(i -> {
                        CxtzInvestmentAgreementEntityPo po = buildCommon(sourceDataId, batchNo, "PERSON");
                        po.setName(i.getName());
                        po.setPhone(i.getPhone());
                        po.setIdCard(i.getIdCard());
                        po.setPersonRole(i.getPersonRole());
                        po.setRelatedSubject(i.getRelatedSubject());
                        return po;
                    }).collect(Collectors.toList())
            );
        }
        return pos;
    }

    private CxtzInvestmentAgreementEntityPo buildCommon(String sourceDataId, String batchNo, String dataType) {
        CxtzInvestmentAgreementEntityPo po = new CxtzInvestmentAgreementEntityPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setDataType(dataType);
        return po;
    }

    /**
     * 账户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        /**
         * 银行账号
         */
        @JsonProperty("account_no")
        private String accountNo;

        /**
         * 账户户名
         */
        @JsonProperty("account_name")
        private String accountName;

        /**
         * 开户行全称
         */
        @JsonProperty("bank_name")
        private String bankName;

        /**
         * 收付款类型枚举值
         */
        @JsonProperty("pay_type")
        private String payType;

        /**
         * 付款期限
         */
        @JsonProperty("pay_deadline")
        private String payDeadline;
    }

    /**
     * 企业信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Company {
        /**
         * 企业完整名称
         */
        @JsonProperty("company_name")
        private String companyName;

        /**
         * 企业角色枚举值
         */
        @JsonProperty("company_role")
        private String companyRole;

        /**
         * 统一社会信用代码
         */
        @JsonProperty("credit_code")
        private String creditCode;

        /**
         * 企业注册地址
         */
        @JsonProperty("address")
        private String address;
    }

    /**
     * 个人信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        /**
         * 自然人姓名
         */
        @JsonProperty("name")
        private String name;

        /**
         * 联系方式
         */
        @JsonProperty("phone")
        private String phone;

        /**
         * 身份证号码
         */
        @JsonProperty("id_card")
        private String idCard;

        /**
         * 个人角色枚举值
         */
        @JsonProperty("person_role")
        private String personRole;

        /**
         * 关联主体完整名称
         */
        @JsonProperty("related_subject")
        private String relatedSubject;
    }
}