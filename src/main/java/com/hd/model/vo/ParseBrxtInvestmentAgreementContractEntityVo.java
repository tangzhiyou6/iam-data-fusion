package com.hd.model.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.BrxtInvestmentAgreementContractEntityPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

/**
 * 投资协议合同 - 实体信息（信托+固有）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseBrxtInvestmentAgreementContractEntityVo extends DataFusionBaseVo<BrxtInvestmentAgreementContractEntityPo, String> {

    private static final long serialVersionUID = 1L;

    @JsonProperty("account_list")
    private List<Account> accountList;

    @JsonProperty("company_list")
    private List<Company> companyList;

    @JsonProperty("person_list")
    private List<Person> personList;

    @Override
    public List<BrxtInvestmentAgreementContractEntityPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        List<BrxtInvestmentAgreementContractEntityPo> pos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountList)) {
            pos.addAll(accountList.stream().map(i -> {
                BrxtInvestmentAgreementContractEntityPo po = buildCommon(sourceDataId, batchNo, "ACCOUNT", scene);
                po.setAccountNo(i.getAccountNo());
                po.setAccountName(i.getAccountName());
                po.setBankName(i.getBankName());
                po.setPayType(i.getPayType());
                po.setPayDeadline(i.getPayDeadline());
                return po;
            }).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(companyList)) {
            pos.addAll(companyList.stream().map(i -> {
                BrxtInvestmentAgreementContractEntityPo po = buildCommon(sourceDataId, batchNo, "COMPANY", scene);
                po.setCompanyName(i.getCompanyName());
                po.setCompanyRole(i.getCompanyRole());
                po.setCreditCode(i.getCreditCode());
                po.setAddress(i.getAddress());
                po.setCompanyContactPerson(i.getCompanyContactPerson());
                po.setCompanyContactPhone(i.getCompanyContactPhone());
                return po;
            }).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(personList)) {
            pos.addAll(personList.stream().map(i -> {
                BrxtInvestmentAgreementContractEntityPo po = buildCommon(sourceDataId, batchNo, "PERSON", scene);
                po.setName(i.getName());
                po.setPhone(i.getPhone());
                po.setIdCard(i.getIdCard());
                po.setPersonRole(i.getPersonRole());
                po.setRelatedSubject(i.getRelatedSubject());
                return po;
            }).collect(Collectors.toList()));
        }
        return pos;
    }

    private BrxtInvestmentAgreementContractEntityPo buildCommon(String sourceDataId, String batchNo, String dataType, String scene) {
        BrxtInvestmentAgreementContractEntityPo po = new BrxtInvestmentAgreementContractEntityPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setDataType(dataType);
        po.setScene(scene);
        return po;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        @JsonProperty("account_no")
        private String accountNo;
        @JsonProperty("account_name")
        private String accountName;
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("pay_type")
        private String payType;
        @JsonProperty("pay_deadline")
        private String payDeadline;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Company {
        @JsonProperty("company_name")
        private String companyName;
        @JsonProperty("company_role")
        private String companyRole;
        @JsonProperty("credit_code")
        private String creditCode;
        @JsonProperty("address")
        private String address;
        @JsonProperty("company_contact_person")
        private String companyContactPerson;
        @JsonProperty("company_contact_phone")
        private String companyContactPhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        @JsonProperty("name")
        private String name;
        @JsonProperty("phone")
        private String phone;
        @JsonProperty("id_card")
        private String idCard;
        @JsonProperty("person_role")
        private String personRole;
        @JsonProperty("related_subject")
        private String relatedSubject;
    }
}
