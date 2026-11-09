package com.hd.model.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *   接口返回数据的格式包装类：现阶段仅处理出现业务异常或非预期使用此格式，正常返回的数据未使用此格式。
 * @author yanlg
 * @since 1.0, 2020-06-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private boolean success = true;

    private String msg = "操作成功！";

    private int code = 0;

    private T data;

    public RestResult okMsg(String message) {
        this.msg = message;
        this.code = 200;
        this.success = true;
        return this;
    }
    
    
    public static RestResult ok() {
        RestResult r = new RestResult();
        r.setSuccess(true);
        r.setCode(200);
        r.setMsg("成功");
        return r;
    }

    public static <T> RestResult ok(T data) {
        RestResult r = new RestResult();
        r.setSuccess(true);
        r.setCode(200);
        r.setData(data);
        return r;
    }

    public static RestResult error(String msg) {
        return error(500, msg);
    }

    public static RestResult error(int code, String msg) {
        RestResult r = new RestResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setSuccess(false);
        return r;
    }
}
