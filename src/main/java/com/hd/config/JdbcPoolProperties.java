package com.hd.config;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 多数据源连接池配置，对应 application.yml 中 jdbc-pools 节点。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jdbc-pools")
public class JdbcPoolProperties {

    private Map<String, PoolConfig> pools = new LinkedHashMap<>();

    @Data
    public static class PoolConfig {
        private String driverClassName = "org.postgresql.Driver";
        private String url;
        private String username;
        private String password;
        private int maximumPoolSize = 10;
        private long connectionTimeout = 180000;
        private long idleTimeout = 180000;
        private long maxLifetime = 180000;
    }
}
