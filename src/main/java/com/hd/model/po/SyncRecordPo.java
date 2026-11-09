package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_sync_records")
@AllArgsConstructor
@NoArgsConstructor
public class SyncRecordPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String system;

    private String bizType;

    private String subBizType;

    private String sourceId;

    private String ossFilePath = "";

    private String fileName;

    private String fileSuffix;

    private Integer fileSize;

    private Integer parseStatus;

    private String failReason;

    private LocalDateTime dataTime;

    public void setFailReason(String failReason) {
        this.failReason = failReason;
        this.parseStatus = 0;
    }

    public SyncRecordPo(String system, String bizType, String sourceId, String fileName) {
        this.system = system;
        this.bizType = bizType;
        this.sourceId = sourceId;
        this.fileName = fileName;
        this.parseStatus = 1;
        this.fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
    }

    public SyncRecordPo(String system, String bizType, String subBizType, String sourceId, String fileName) {
        this(system, bizType, sourceId, fileName);
        this.subBizType = subBizType;
    }

    public void addUploadInfos(String ossFilePath, int fileSize) {
        this.ossFilePath = ossFilePath;
        this.fileSize = fileSize;
    }
}
