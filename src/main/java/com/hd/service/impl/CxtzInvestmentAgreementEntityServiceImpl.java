package com.hd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hd.mapper.CxtzInvestmentAgreementEntityMapper;
import com.hd.service.CxtzInvestmentAgreementEntityService;
import com.hd.model.po.CxtzInvestmentAgreementEntityPo;
import org.springframework.stereotype.Service;

@Service
public class CxtzInvestmentAgreementEntityServiceImpl
        extends ServiceImpl<CxtzInvestmentAgreementEntityMapper, CxtzInvestmentAgreementEntityPo>
        implements CxtzInvestmentAgreementEntityService {
}
