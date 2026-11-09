package com.hd.constants;

/**
 * 系统名称枚举
 */
public enum FusionSysEnum {

    CXTZ("cxtz_gzbg", "创新投资")

    ,EAUC("eauc", "电能e拍")

    ,BRXT("brxt", "百瑞信托")

    ,SK("sk", "司库")
    ;


    private final String code;

    private final String sysName;

    FusionSysEnum(String code, String sysName) {
        this.code = code;
        this.sysName = sysName;
    }

    public String getCode() {
        return code;
    }

    public String getSysName() {
        return sysName;
    }
}
