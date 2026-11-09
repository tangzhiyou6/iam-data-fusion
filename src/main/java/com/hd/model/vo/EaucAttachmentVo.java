package com.hd.model.vo;

import java.io.Serializable;
import java.util.Date;

import com.hd.model.qo.SourceFileDownloadParamsQo;
import lombok.Data;

@Data
public class EaucAttachmentVo extends SourceFileBaseVo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private String id;
    
    /** 标题 */
    private String title;
    
    /** 文件扩展名 */
    private String extend;
    
    /** 文件类型 */
    private String fileType;
    
    /** 业务ID */
    private String bizId;
    
    /** 状态 */
    private String status;
    
    /** 模式 */
    private String mode;
    
    /** 创建人 */
    private String createUser;
    
    /** 创建时间(毫秒时间戳) */
    private Date createTime;
    
    /** 文件路径 */
    private String path;
    
    /** 文件中心ID */
    private String mdgFileId;

    @Override
    public String sourceFileId() {
        return id;
    }

    @Override
    public String sourceFileName() {
        return title + "." + extend;
    }

    @Override
    public String bizName() {
        return "";
    }

    @Override
    public SourceFileDownloadParamsQo buildDownloadParamsQo() {
        return SourceFileDownloadParamsQo.builder().fileId(id)
                .fileType(fileType).build();
    }
}