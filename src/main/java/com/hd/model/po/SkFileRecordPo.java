package com.hd.model.po;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hd.model.vo.SkAttachmentVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName(schema = "ods_dsjsj", value = "ods_dsjsj_sk_file_record")
@AllArgsConstructor
@NoArgsConstructor
public class SkFileRecordPo extends BasePo<String>  {

    @TableId(type = IdType.INPUT)
    private String id;

    private String fileName;

    private String fileType;

    private String fileTypeName;

    private Long fileSize;

    private String ossBucketName;

    private String ossFilePath;

    private LocalDateTime uploadTime;

    private Integer status;

    private String remark;

    private  LocalDateTime dataTime;

    public SkFileRecordPo(SkAttachmentVo vo) {
        this.id = vo.getId();
        this.fileName = vo.getFileName();
        this.fileType = vo.getFileType();
        this.fileTypeName = vo.getFileTypeName();
        this.uploadTime = vo.getUploadTime();
    }
}
