package com.hd.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.hd.config.JdbcPoolProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * 多 JDBC 连接池工具类，基于 {@link NamedParameterJdbcTemplate} 支持 :param 命名参数。
 */
@Slf4j
@Component
public class JdbcPoolUtils {

    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<String, NamedParameterJdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();

    @Autowired
    private DataSource dataSource;

    public JdbcPoolUtils(JdbcPoolProperties properties) {
        if (properties.getPools() != null) {
            properties.getPools().forEach(this::registerPool);
        }
    }

    /**
     * 注册一个连接池。
     */
    private void registerPool(String poolName, JdbcPoolProperties.PoolConfig config) {
        if (StringUtils.isBlank(poolName)) {
            throw new IllegalArgumentException("poolName 不能为空");
        }
        if (config == null || StringUtils.isBlank(config.getUrl())) {
            throw new IllegalArgumentException("数据源 " + poolName + " 的 url 不能为空");
        }
        if (dataSources.containsKey(poolName)) {
            closeQuietly(dataSources.remove(poolName));
            jdbcTemplates.remove(poolName);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("jdbc-pool-" + poolName);
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        dataSources.put(poolName, dataSource);
        jdbcTemplates.put(poolName, new NamedParameterJdbcTemplate(dataSource));
        log.info("JDBC 连接池已注册: {}", poolName);
    }

    public DataSource getDataSource(String poolName) {
        HikariDataSource dataSource = dataSources.get(poolName);
        if (dataSource == null) {
            return this.dataSource;
        }
        return dataSource;
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(String poolName) {
        NamedParameterJdbcTemplate template = jdbcTemplates.get(poolName);
        if (template == null) {
            template = new NamedParameterJdbcTemplate(dataSource);
            jdbcTemplates.put(poolName, template);
        }
        return template;
    }

    /**
     * 查询列表，SQL 使用 :param 命名参数。
     */
    public List<Map<String, Object>> queryList(String poolName, String sql, Map<String, ?> params) {
        return getJdbcTemplate(poolName).queryForList(sql, params);
    }

    /**
     * 查询列表，SQL 使用 :param 命名参数。
     */
    public List<Map<String, Object>> queryList(String poolName, String sql, SqlParameterSource params) {
        return getJdbcTemplate(poolName).queryForList(sql, params);
    }

    /**
     * 查询单条记录，无数据时返回 null。
     */
    public Map<String, Object> queryOne(String poolName, String sql, Map<String, ?> params) {
        List<Map<String, Object>> rows = queryList(poolName, sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 查询单条记录，无数据时返回 null。
     */
    public Map<String, Object> queryOne(String poolName, String sql, SqlParameterSource params) {
        List<Map<String, Object>> rows = queryList(poolName, sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 执行更新/删除/插入。
     */
    public int execute(String poolName, String sql, Map<String, ?> params) {
        return getJdbcTemplate(poolName).update(sql, params);
    }

    /**
     * 执行更新/删除/插入。
     */
    public int execute(String poolName, String sql, SqlParameterSource params) {
        return getJdbcTemplate(poolName).update(sql, params);
    }

    @PreDestroy
    public void destroy() {
        dataSources.forEach((name, ds) -> {
            closeQuietly(ds);
            log.info("JDBC 连接池已关闭: {}", name);
        });
        dataSources.clear();
        jdbcTemplates.clear();
    }

    private void closeQuietly(HikariDataSource dataSource) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
