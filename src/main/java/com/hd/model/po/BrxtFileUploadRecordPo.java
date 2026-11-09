package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_brxt_file_upload_records")
@AllArgsConstructor
@NoArgsConstructor
public class BrxtFileUploadRecordPo extends BasePo<Long> {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizType;

    private String archiveFilePath;

    private Integer archiveFileDone = 0;

    private Long archiveFileSize = 0L;

    private String fileId;

    private String fileName;

    private String filePath;

    private Long fileSize = 0L;

    private String ossFilePath = "";

    private LocalDateTime dataTime;

}
