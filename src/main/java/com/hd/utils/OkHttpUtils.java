package com.hd.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.fastjson.JSON;
import com.hd.common.ConnectionPoolStats;
import com.hd.model.vo.AutoCleanFile;
import com.hd.model.vo.HttpResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
public class OkHttpUtils {
    private static final long MAX_MEMORY_BODY_SIZE = 1024 * 1024 * 1024;
    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("filename\\*?=(?:UTF-8''|\"?)([^\";\\n]+)", Pattern.CASE_INSENSITIVE);

    private static volatile OkHttpClient okHttpClient = null;

    private Map<String, String> headerMap;

    private Map<String, String> paramMap;

    private Map<String, File> fileMap;

    private String bodyJson;

    private String url;

    private Request.Builder request;

    private String userCode;

    /**
     * 初始化okHttpClient，并且允许https访问
     */
    private OkHttpUtils() {
        if (okHttpClient == null) {
            synchronized (OkHttpUtils.class) {
                if (okHttpClient == null) {
                    TrustManager[] trustManagers = buildTrustManagers();
                    // 创建Dispatcher控制并发
                    Dispatcher dispatcher = new Dispatcher();
                    dispatcher.setMaxRequests(200);
                    dispatcher.setMaxRequestsPerHost(50);
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(180, TimeUnit.HOURS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .callTimeout(200, TimeUnit.HOURS)  // 添加总超时
                            .dispatcher(dispatcher)  // 设置并发控制
                            .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                            .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                            .hostnameVerifier((hostName, session) -> true)
                            .retryOnConnectionFailure(true)
                            .build();
                    addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                }
            }
        }
    }

    /**
     * 创建OkHttpUtils
     *
     * @return
     */
    public static OkHttpUtils builder() {
        return new OkHttpUtils();
    }

    /**
     * 添加url
     *
     * @param url
     * @return
     */
    public OkHttpUtils url(String url) {
        this.url = url;
        return this;
    }

    public OkHttpUtils userCode(String userCode) {
        this.userCode = userCode;
        return this;
    }

    public String getUserCode() {
        return this.userCode;
    }

    /**
     * 添加参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addParam(String key, String value) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.put(key, value);
        return this;
    }

    public OkHttpUtils addParams(Map<String, String> params) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.putAll(params);
        return this;
    }

    public OkHttpUtils addBodyRawStr(String bodyRawStr) {
        this.bodyJson = bodyRawStr;
        return this;
    }

    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpUtils addHeader(String key, String value) {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.put(key, value);
        return this;
    }

    /**
     * 添加多个文件参数，用于批量文件上传
     *
     * @param files 文件Map，key为参数名，value为文件对象
     * @return
     */
    public OkHttpUtils addFiles(Map<String, File> files) {
        if (fileMap == null) {
            fileMap = new LinkedHashMap<>(16);
        }
        fileMap.putAll(files);
        return this;
    }

    public OkHttpUtils addFile(String key, File file) {
        if (fileMap == null) {
            fileMap = new LinkedHashMap<>(16);
        }
        fileMap.put(key, file);
        return this;
    }

    /**
     * 初始化get方法
     *
     * @return
     */
    public OkHttpUtils get() {
        request = new Request.Builder().get();
        StringBuilder urlBuilder = new StringBuilder(url);
        if (paramMap != null && !paramMap.isEmpty()) {
            urlBuilder.append("?");
            try {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                            append("=").
                            append(URLEncoder.encode(entry.getValue(), "utf-8")).
                            append("&");
                }
            } catch (Exception e) {
                log.error("[OkHttpUtils] GET参数编码异常", e);
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        request.url(urlBuilder.toString());
        return this;
    }

    public OkHttpUtils method(RequestMethod method, Boolean isJsonPost) {
        if (paramMap == null || RequestMethod.GET == method) {
            get();
        } else {
            post(isJsonPost);
        }
        return this;
    }

    /**
     * 初始化post方法
     *
     * @param isJsonPost true=JSON格式(类似postman的raw), false=表单格式, null=无请求体
     * @return
     */
    public OkHttpUtils post(Boolean isJsonPost) {
        Request.Builder requestBuilder = new Request.Builder();
        
        if (isJsonPost == null) {
            // null: POST请求不带请求体
            log.info("[OkHttpUtils] POST请求(无请求体),url={}", url);
            request = requestBuilder.post(RequestBody.create(null, new byte[0])).url(url);
        } else if (isJsonPost) {
            // true: JSON格式
            String json = "";
            if (StringUtils.isEmpty(bodyJson)) {
                if (paramMap != null) {
                    json = JSON.toJSONString(paramMap);
                }
            } else {
                json = this.bodyJson;
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            log.info("[OkHttpUtils] POST请求(JSON格式),url={}, requestBody={}", url, json);
            request = requestBuilder.post(requestBody).url(url);
        } else {
            // false: 表单格式
            FormBody.Builder formBody = new FormBody.Builder();
            if (paramMap != null) {
                paramMap.forEach(formBody::add);
            }
            RequestBody requestBody = formBody.build();
            log.info("[OkHttpUtils] POST请求(表单格式),url={}", url);
            request = requestBuilder.post(requestBody).url(url);
        }
        return this;
    }

    /**
     * 初始化支持文件上传的postForm方法
     * @return
     */
    public OkHttpUtils postForm() {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        // 添加普通表单参数
        if (paramMap != null) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        // 添加文件参数
        if (fileMap != null && !fileMap.isEmpty()) {
            for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                String key = entry.getKey();
                File file = entry.getValue();
                if (file != null && file.exists()) {
                    String fileName = file.getName();
                    MediaType mediaType = MediaType.parse("multipart/form-data");
                    RequestBody fileBody = RequestBody.create(mediaType, file);
                    multipartBuilder.addFormDataPart(key, fileName, fileBody);
                }
            }
        }
        RequestBody requestBody = multipartBuilder.build();
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }

    public OkHttpUtils delete(Boolean isJsonBody) {
        if (paramMap == null || paramMap.isEmpty()) {
            // 不带请求体的DELETE请求
            request = new Request.Builder().delete().url(url);
        } else {
            // 带请求体的DELETE请求
            RequestBody requestBody;
            if (null == isJsonBody || isJsonBody) {
                String json = JSON.toJSONString(paramMap);
                requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                request = new Request.Builder().delete(requestBody).url(url);
            } else {
                StringBuilder urlBuilder = new StringBuilder(url);
                if (paramMap != null && !paramMap.isEmpty()) {
                    urlBuilder.append("?");
                    try {
                        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                            urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                                    append("=").
                                    append(URLEncoder.encode(entry.getValue(), "utf-8")).
                                    append("&");
                        }
                    } catch (Exception e) {
                        log.error("[OkHttpUtils] DELETE参数编码异常", e);
                    }
                    urlBuilder.deleteCharAt(urlBuilder.length() - 1);
                }
                request = new Request.Builder().delete().url(urlBuilder.toString());
            }
        }
        return this;
    }

    /**
     * 同步请求
     *
     * @return
     */
    public <T> HttpResult<T> sync(Class<T> responseType) {
        setHeader(request);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            ResponseBody responseBody = response.body();
            String contentType = response.header("Content-Type");
            long contentLength = responseBody.contentLength();

            String rawBody = null;
            byte[] bytes = null;
            AutoCleanFile downloadFile = null;
            if (contentLength > MAX_MEMORY_BODY_SIZE) {
                bytes = new byte[0];
                log.warn("超大文件：{}，使用下载的方式接收！", String.format("%.2f GB", contentLength / 1024.0 / 1024 / 1024));
                downloadFile = download(response);
            } else {
                bytes = responseBody != null ? responseBody.bytes() : new byte[0];
                rawBody = bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
            }
            if (!response.isSuccessful()) {
                return HttpResult.fromErrorResponse(response, rawBody, String.format("HTTP 请求失败，status=%d", response.code()));
            }

            T body = null;
            if (responseType == byte[].class) {
                @SuppressWarnings("unchecked")
                T byteArray = (T) bytes;
                body = byteArray;
            } else if (responseType != Void.class && StringUtils.isNotBlank(rawBody)
                    && contentType != null && contentType.toLowerCase().contains("application/json")) {
                if (responseType == String.class) {
                    @SuppressWarnings("unchecked")
                    T str = (T) rawBody;
                    body = str;
                } else {
                    body = JsonUtils.parse(rawBody, responseType);
                }
            } else if (responseType == String.class) {
                @SuppressWarnings("unchecked")
                T str = (T) rawBody;
                body = str;
            }
            return HttpResult.fromResponse(response, body, rawBody, downloadFile);
        } catch (Exception e) {
            throw new RuntimeException("HTTP 请求异常: " + url, e);
        }
    }

    /**
     * 为request添加请求头
     *
     * @param request
     */
    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            try {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                log.error("[OkHttpUtils] 设置请求头异常", e);
            }
        }
    }

    public void setRequest(Request.Builder request) {
        this.request = request;
    }


    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     * @return
     */
    public static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("[OkHttpUtils] 创建SSLSocketFactory失败", e);
        }
        return ssfFactory;
    }

    public static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    public static void printPoolStatus() {
        if (null == okHttpClient) {
            return;
        }
        ConnectionPoolStats stats = new ConnectionPoolStats(okHttpClient);
        log.info("当前连接池资源信息：{}", stats.getStats().toString());
    }

    /**
     * 自定义一个接口回调
     */
    public interface ICallBack {
        void onSuccessful(Call call, String data);
        void onFailure(Call call, String errorMsg);
    }

    public AutoCleanFile download(Response response) {
        AutoCleanFile tempFile = new AutoCleanFile("dsjsj_temp_large_file_temp" + File.separator + extractFileName(response.headers()));
        try (BufferedSink sink = Okio.buffer(Okio.sink(tempFile.getPath().toFile()));
             BufferedSource source = response.body().source()) {
            sink.writeAll(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    public static String extractFileName(Headers headers) {
        String contentDisposition = headers.get("Content-Disposition");
        if (StringUtils.isBlank(contentDisposition)) {
            return null;
        }
        Matcher matcher = FILENAME_PATTERN.matcher(contentDisposition);
        if (matcher.find()) {
            try {
                return URLDecoder.decode(matcher.group(1).trim(), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    private boolean isSupportRange(Response response) {
        // 1. 必须有 206 状态码
        if (response.code() != 206) {
            return false;
        }

        // 2. 必须声明支持 bytes 分片
        String acceptRanges = response.header("Accept-Ranges");
        if (acceptRanges == null || !"bytes".equalsIgnoreCase(acceptRanges)) {
            return false;
        }

        // 3. 必须返回 Content-Range（强校验）
        String contentRange = response.header("Content-Range");
        if (contentRange == null || !contentRange.startsWith("bytes ")) {
            return false;
        }

        return true;
    }


    private long parseTotalSize(Response response) {
        String contentRange = response.header("Content-Range");
        if (contentRange == null) {
            return -1;
        }
        // 格式: bytes 0-0/107374182400
        int slashIndex = contentRange.lastIndexOf('/');
        if (slashIndex == -1) {
            return -1;
        }
        return Long.parseLong(contentRange.substring(slashIndex + 1));
    }
}
