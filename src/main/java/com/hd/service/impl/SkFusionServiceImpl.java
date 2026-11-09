package com.hd.service.impl;

import static com.hd.constants.JDBCPoolNameConstants.JM_CLOUD_POOL_NAME;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hd.client.SkClient;
import com.hd.model.vo.FileDownloadResult;
import com.hd.model.vo.SkAttachmentVo;
import com.hd.service.SkFusionService;
import com.hd.utils.JdbcPoolUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkFusionServiceImpl implements SkFusionService {

    private final JdbcPoolUtils jdbcPoolUtils;

    @Autowired
    private SkClient skClient;

    @Value("${file-sync.sk.attachment_table}")
    private String skAttachmentInfoTable;

    @Override
    public List<SkAttachmentVo> findAttachments(Long lastDataTime) {
        Map<String, Object> params = new HashMap<>();
        String sql = " SELECT t.fmid, t.filename,  t.filetype,  t.filetypename,  t.filepath,  t.uploadtime  FROM "
                + skAttachmentInfoTable
                + " t where uploadtime > :incrementTime limit 1000";
        params.put("incrementTime", Objects.isNull(lastDataTime) ? new Date(100000) : new Date(lastDataTime));
        List<SkAttachmentVo> skAttachmentVos = jdbcPoolUtils.getJdbcTemplate(JM_CLOUD_POOL_NAME)
                .queryForStream(sql, params, new BeanPropertyRowMapper<>(SkAttachmentVo.class)).collect(Collectors.toList());
        return skAttachmentVos;
    }


    @Override
    public FileDownloadResult freeDownload(String remoteRelativePath) {
        return null; //skClient.freeDownload(remoteRelativePath);
    }
}
