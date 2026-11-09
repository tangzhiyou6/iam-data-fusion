package com.hd.config;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 多桶配置，对应 application.yml 中 oss.buckets 节点。
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private Map<String, BucketConfig> buckets = new LinkedHashMap<>();

    private String  defaultBucketKey;

    @Data
    public static class BucketConfig {

        private String endPoint;

        private String accessKeyId;

        private String accessKeySecret;

        /** STS 临时凭证 Token，可选 */
        private String securityToken;

        /** 实际 OSS 桶名 */
        private String bucketName;

        /** 启动时校验桶是否存在且可访问 */
        private boolean validateOnStartup = true;

        /** 预签名 URL 默认过期时间（秒） */
        private long defaultUrlExpireSeconds = 3600;

        /** 自定义访问域名（CNAME），用于生成外链时替换默认域名，可选 */
        private String customDomain;
    }
}
