package com.hd.model.vo;

import lombok.Getter;
import okhttp3.Headers;
import okhttp3.Response;
/**
 * 统一 HTTP 响应包装，支持泛型 body。
 */
@Getter
public class HttpResult<T> {

    private final boolean success;

    private final int httpStatus;

    private final String contentType;

    private final Headers headers;

    private final T body;

    private AutoCleanFile autoCleanFile;

    private final String rawBody;

    private final String errorMessage;
	
    private HttpResult(boolean success, int httpStatus, String contentType, Headers headers,
                       T body, String rawBody, String errorMessage) {
        this.success = success;
        this.httpStatus = httpStatus;
        this.contentType = contentType;
        this.headers = headers;
        this.body = body;
        this.rawBody = rawBody;
        this.errorMessage = errorMessage;
    }

    public static <T> HttpResult<T> ok(int httpStatus, String contentType, Headers headers, T body, String rawBody) {
        return new HttpResult<>(true, httpStatus, contentType, headers, body, rawBody, null);
    }

    public static <T> HttpResult<T> fail(int httpStatus, String contentType, Headers headers,
                                           String rawBody, String errorMessage) {
        return new HttpResult<>(false, httpStatus, contentType, headers, null, rawBody, errorMessage);
    }

    public static <T> HttpResult<T> fromResponse(Response response, T body, String rawBody) {
        String contentType = response.header("Content-Type");
        return ok(response.code(), contentType, response.headers(), body, rawBody);
    }

    public static <T> HttpResult<T> fromResponse(Response response, T body, String rawBody, AutoCleanFile downloadFile) {
        String contentType = response.header("Content-Type");
        HttpResult<T> ok = ok(response.code(), contentType, response.headers(), body, rawBody);
        ok.setAutoCleanFile(downloadFile);
        return ok;
    }

    public static <T> HttpResult<T> fromErrorResponse(Response response, String rawBody, String errorMessage) {
        String contentType = response.header("Content-Type");
        return fail(response.code(), contentType, response.headers(), rawBody, errorMessage);
    }

    public boolean isJsonResponse() {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    public AutoCleanFile getAutoCleanFile() {
        return autoCleanFile;
    }

    public void setAutoCleanFile(AutoCleanFile autoCleanFile) {
        this.autoCleanFile = autoCleanFile;
    }
}