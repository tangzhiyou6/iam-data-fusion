package com.hd.client;


import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hd.model.qo.FastgptCompletionQo;
import com.hd.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@ConfigurationProperties(prefix = "fastgpt")
public class FastGptClient {
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000; // 重试间隔1秒

    private static volatile OkHttpClient httpClient = null;

    private ObjectMapper objectMapper;

    @Value("${fastgpt.baseUrl}")
    private String baseUrl;

    @Value("${fastgpt.commonToken}")
    private String commonToken;

    @Value("${fastgpt.appId}")
    private String appId;

    public FastGptClient() {
        this.objectMapper = new ObjectMapper();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(200);
        dispatcher.setMaxRequestsPerHost(50);
        TrustManager[] trustManagers = buildTrustManagers();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(600))
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                .hostnameVerifier((hostName, session) -> true)
                .retryOnConnectionFailure(true)
                .build();
    }

    public Map<String, String> chat(String businessCode, List<String> fileOssPaths, String...dataKeys) {
        FastgptCompletionQo completionQo = new FastgptCompletionQo(appId);
        completionQo.addVariable("file", fileOssPaths);
        // completionQo.addVariable("dataType", dataType);
        completionQo.addVariable("system", businessCode);
        completionQo.addVariable("business_code", businessCode);
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                String jsonBody = objectMapper.writeValueAsString(completionQo);
                log.info("请求大模型参数：\n{}", jsonBody);
                Request httpRequest = new Request.Builder()
                        .url(baseUrl + "/v1/chat/completions")
                        .addHeader("Authorization", "Bearer " + commonToken)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(
                                jsonBody,
                                MediaType.parse("application/json; charset=utf-8")))
                        .build();

                try (Response response = httpClient.newCall(httpRequest).execute()) {
                    // 1. 检查HTTP调用是否成功
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "null";
                        throw new IOException("LLM call failed: " + response.code()
                                + ", body=" + errorBody);
                    }
                    String respStr = response.body().string();
                    // 2. 检查响应体是否为空
                    if (respStr == null || respStr.trim().isEmpty()) {
                        throw new IOException("LLM response body is empty");
                    }
                    Map<String, String> resultMap = extractTargetField(respStr, dataKeys);
                    if (CollectionUtils.isEmpty(resultMap)) {
                        throw new IOException("LLM response body target value is empty！");
                    }
                    log.info("非结构化提取结果：{}", JsonUtils.toJSON(resultMap));
                    return resultMap;
                }
            } catch (IOException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    log.error("LLM call failed after {} retries", MAX_RETRIES, e);
                    break;
                }
                log.warn("LLM call failed (attempt {}/{}), retrying... Error: {}",
                        retryCount, MAX_RETRIES, e.getMessage());
                // 指数退避策略：每次重试等待时间递增
                try {
                    Thread.sleep(RETRY_DELAY_MS * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    private Map<String, String> extractTargetField(String jsonStr, String...dataKeys) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            JsonNode responseData = rootNode.get("responseData");
            if (responseData != null && responseData.isArray() && responseData.size() > 0) {
                JsonNode lastElement = responseData.get(responseData.size() - 1);
                JsonNode pluginOutput = lastElement
                        .get("pluginOutput");
                JsonNode sceneNode = pluginOutput.get("scene");
                resultMap.put("scene", null == sceneNode ? "" : sceneNode.asText());
                for (String dataKey : dataKeys) {
                    JsonNode jsonNode = pluginOutput.get(dataKey);
                    if (null != jsonNode) {
                        resultMap.put(dataKey, jsonNode.asText());
                    }
                }
            }
        } catch (Exception e) {
            log.error("从LLM结果中提取结果字段报错, 原始数据：{}", jsonStr);
        }
        return resultMap;
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

/*    public <T> T chat(Map<String, Object> request, TypeReference<T> typeRef) throws IOException {
        return null;
    }*/
}
