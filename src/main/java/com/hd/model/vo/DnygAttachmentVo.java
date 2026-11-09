package com.hd.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DnygAttachmentVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String deliveryCode;

    private Date  created;

    private Integer status;

    private Integer source;

    private String invUrl;

    private String ossFilename;

    private String ossFileurl;
}
