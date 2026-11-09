package com.hd.service.impl;

import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hd.model.vo.DnygAttachmentVo;
import com.hd.service.DnygFusionService;
import com.hd.utils.JdbcPoolUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DnygFusionServiceImpl implements DnygFusionService {

    private final JdbcPoolUtils jdbcPoolUtils;

    @Value("${file-sync.dnyg.attachment_table}")
    private String dnygAttachmentInfoTable;

    @Override
    public List<DnygAttachmentVo> findAttachments(Long lastDataTime) {
        Map<String, Object> params = new HashMap<>();
        String sql = " SELECT id, delivery_code, created, status, source, inv_url, oss_filename, oss_fileurl from "
                + dnygAttachmentInfoTable
                + " where created > :incrementTime";
        params.put("incrementTime", Objects.isNull(lastDataTime) ? new Date(100000) : new Date(lastDataTime));
        List<DnygAttachmentVo> eaucAttachmentVos = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, params, new BeanPropertyRowMapper<>(DnygAttachmentVo.class)).collect(Collectors.toList());
        return eaucAttachmentVos;
    }
}
