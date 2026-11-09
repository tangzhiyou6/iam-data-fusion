package com.hd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hd.mapper.SyncRecordMapper;
import com.hd.model.po.SyncRecordPo;
import com.hd.service.SyncRecordService;
import org.springframework.stereotype.Service;

@Service
public class SyncRecordServiceImpl extends ServiceImpl<SyncRecordMapper, SyncRecordPo> implements SyncRecordService {
}
