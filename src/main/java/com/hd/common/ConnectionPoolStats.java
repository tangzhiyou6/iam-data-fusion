package com.hd.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * OkHttp 连接池状态监控类
 * 用于获取连接池的详细运行状态，帮助诊断连接池相关的问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ConnectionPoolStats {

    private ConnectionPool connectionPool;

    private Dispatcher dispatcher;
    
    public ConnectionPoolStats(OkHttpClient okHttpClient) {
        this.connectionPool = okHttpClient.connectionPool();
        this.dispatcher = okHttpClient.dispatcher();
    }
    
    /**
     * 获取连接池基本统计信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Slf4j
    public static class PoolStats {
        // 活跃连接数
        private int activeConnectionCount;
        // 空闲连接数
        private int idleConnectionCount;
        // 总连接数
        private int totalConnectionCount;
        // 排队请求数
        private int queuedCallCount;
        // 运行中请求数
        private int runningCallCount;
        // 每个主机的连接数统计
        private Map<String, Integer> connectionsPerHost = new HashMap<>();
        // 连接池配置
        private int maxIdleConnections;
        private long keepAliveDurationMinutes;
        // 反射获取的内部状态
        private int realIdleCount = -1;
        private int realConnectionCount = -1;
        // 上次清理时间
        private long lastCleanupTime = -1;
        
        // Getters
        public int getActiveConnectionCount() { return activeConnectionCount; }
        public int getIdleConnectionCount() { return idleConnectionCount; }
        public int getTotalConnectionCount() { return totalConnectionCount; }
        public int getQueuedCallCount() { return queuedCallCount; }
        public int getRunningCallCount() { return runningCallCount; }
        public Map<String, Integer> getConnectionsPerHost() { return connectionsPerHost; }
        public int getMaxIdleConnections() { return maxIdleConnections; }
        public long getKeepAliveDurationMinutes() { return keepAliveDurationMinutes; }
        public int getRealIdleCount() { return realIdleCount; }
        public int getRealConnectionCount() { return realConnectionCount; }
        public long getLastCleanupTime() { return lastCleanupTime; }
        
        // Setters
        public void setActiveConnectionCount(int activeConnectionCount) { this.activeConnectionCount = activeConnectionCount; }
        public void setIdleConnectionCount(int idleConnectionCount) { this.idleConnectionCount = idleConnectionCount; }
        public void setTotalConnectionCount(int totalConnectionCount) { this.totalConnectionCount = totalConnectionCount; }
        public void setQueuedCallCount(int queuedCallCount) { this.queuedCallCount = queuedCallCount; }
        public void setRunningCallCount(int runningCallCount) { this.runningCallCount = runningCallCount; }
        public void setConnectionsPerHost(Map<String, Integer> connectionsPerHost) { this.connectionsPerHost = connectionsPerHost; }
        public void setMaxIdleConnections(int maxIdleConnections) { this.maxIdleConnections = maxIdleConnections; }
        public void setKeepAliveDurationMinutes(long keepAliveDurationMinutes) { this.keepAliveDurationMinutes = keepAliveDurationMinutes; }
        public void setRealIdleCount(int realIdleCount) { this.realIdleCount = realIdleCount; }
        public void setRealConnectionCount(int realConnectionCount) { this.realConnectionCount = realConnectionCount; }
        public void setLastCleanupTime(long lastCleanupTime) { this.lastCleanupTime = lastCleanupTime; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("连接池状态统计:\n");
            sb.append("├─ 运行中请求数: ").append(runningCallCount).append("\n");
            sb.append("├─ 排队请求数: ").append(queuedCallCount).append("\n");
            sb.append("├─ 活跃连接数: ").append(activeConnectionCount).append("\n");
            sb.append("├─ 空闲连接数: ").append(idleConnectionCount).append("\n");
            sb.append("├─ 总连接数: ").append(totalConnectionCount).append("\n");
            if (realIdleCount >= 0) {
                sb.append("├─ 实际空闲连接数(反射): ").append(realIdleCount).append("\n");
            }
            if (realConnectionCount >= 0) {
                sb.append("├─ 实际总连接数(反射): ").append(realConnectionCount).append("\n");
            }
            sb.append("├─ 最大空闲连接数配置: ").append(maxIdleConnections).append("\n");
            sb.append("├─ 连接保活时间配置: ").append(keepAliveDurationMinutes).append(" 分钟\n");
            
            if (!connectionsPerHost.isEmpty()) {
                sb.append("├─ 各主机连接分布:\n");
                connectionsPerHost.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        sb.append("│  ├─ ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" 个连接\n");
                    });
            }
            
            if (lastCleanupTime > 0) {
                long minutesSinceLastCleanup = (System.currentTimeMillis() - lastCleanupTime) / (1000 * 60);
                sb.append("└─ 上次清理时间: ").append(minutesSinceLastCleanup).append(" 分钟前\n");
            } else {
                sb.append("└─ 上次清理时间: 未知\n");
            }
            
            return sb.toString();
        }
    }

    /**
     * 获取连接池统计信息
     */
    public PoolStats getStats() {
        PoolStats stats = new PoolStats();
        try {
            // 获取Dispatcher状态
            stats.setRunningCallCount(dispatcher.runningCallsCount());
            stats.setQueuedCallCount(dispatcher.queuedCallsCount());

            // 获取连接池公开API信息
            stats.setIdleConnectionCount(connectionPool.idleConnectionCount());

            // 尝试通过反射获取更多内部信息
            try {
                getInternalPoolStats(stats);
            } catch (Exception e) {
                log.warn("无法通过反射获取连接池内部状态: {}", e.getMessage());
            }

            // 计算活跃连接数
            int activeConnections = Math.max(0, stats.getRealConnectionCount() - stats.getIdleConnectionCount());
            stats.setActiveConnectionCount(activeConnections);

            // 设置配置信息
            stats.setMaxIdleConnections(getMaxIdleConnections());
            stats.setKeepAliveDurationMinutes(getKeepAliveDurationMinutes());

            // 尝试获取各主机连接分布
            try {
                stats.setConnectionsPerHost(getConnectionsPerHost());
            } catch (Exception e) {
                log.debug("无法获取各主机连接分布: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("获取连接池状态失败", e);
        }
        return stats;
    }

    /**
     * 通过反射获取连接池内部状态
     */
    private void getInternalPoolStats(PoolStats stats) throws Exception {
        try {
            // 获取连接池的真实空闲连接数
            Method idleConnectionCountMethod = ConnectionPool.class.getDeclaredMethod("idleConnectionCount");
            idleConnectionCountMethod.setAccessible(true);
            int realIdleCount = (int) idleConnectionCountMethod.invoke(connectionPool);
            stats.setRealIdleCount(realIdleCount);

            // 获取连接池中的真实连接数
            Method connectionCountMethod = ConnectionPool.class.getDeclaredMethod("connectionCount");
            connectionCountMethod.setAccessible(true);
            int realConnectionCount = (int) connectionCountMethod.invoke(connectionPool);
            stats.setRealConnectionCount(realConnectionCount);
            stats.setTotalConnectionCount(realConnectionCount);

            // 获取清理线程信息
            try {
                Field cleanupRunnableField = ConnectionPool.class.getDeclaredField("cleanupRunnable");
                cleanupRunnableField.setAccessible(true);
                Object cleanupRunnable = cleanupRunnableField.get(connectionPool);

                if (cleanupRunnable != null) {
                    Class<?> cleanupRunnableClass = cleanupRunnable.getClass();
                    Field nextCleanupNanosField = cleanupRunnableClass.getDeclaredField("nextCleanupNanos");
                    nextCleanupNanosField.setAccessible(true);
                    long nextCleanupNanos = (long) nextCleanupNanosField.get(cleanupRunnable);

                    // 计算上次清理时间
                    long nowNanos = System.nanoTime();
                    long lastCleanupNanos = nowNanos - (Long.MAX_VALUE - nextCleanupNanos);
                    long lastCleanupMillis = TimeUnit.NANOSECONDS.toMillis(lastCleanupNanos);
                    stats.setLastCleanupTime(lastCleanupMillis);
                }
            } catch (Exception e) {
                log.debug("无法获取清理线程信息: {}", e.getMessage());
            }

        } catch (NoSuchMethodException e) {
            log.warn("当前OkHttp版本不支持反射获取内部状态");
        }
    }

    /**
     * 获取各主机连接分布
     */
    private Map<String, Integer> getConnectionsPerHost() throws Exception {
        Map<String, Integer> hostConnections = new HashMap<>();

        try {
            // 获取连接池内部的连接列表
            Field connectionsField = ConnectionPool.class.getDeclaredField("connections");
            connectionsField.setAccessible(true);

            // 不同OkHttp版本的内部结构可能不同
            Object connections = connectionsField.get(connectionPool);

            if (connections instanceof java.util.List) {
                java.util.List<?> connectionList = (java.util.List<?>) connections;

                for (Object conn : connectionList) {
                    try {
                        // 获取连接的socket
                        Field socketField = conn.getClass().getDeclaredField("socket");
                        socketField.setAccessible(true);
                        java.net.Socket socket = (java.net.Socket) socketField.get(conn);

                        // 获取远程地址
                        String host = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

                        // 统计
                        hostConnections.put(host, hostConnections.getOrDefault(host, 0) + 1);
                    } catch (Exception e) {
                        // 忽略无法处理的连接
                    }
                }
            }
        } catch (Exception e) {
            log.warn("无法获取连接分布: {}", e.getMessage());
        }

        return hostConnections;
    }

    /**
     * 获取最大空闲连接数配置
     */
    private int getMaxIdleConnections() {
        try {
            Field maxIdleConnectionsField = ConnectionPool.class.getDeclaredField("maxIdleConnections");
            maxIdleConnectionsField.setAccessible(true);
            return maxIdleConnectionsField.getInt(connectionPool);
        } catch (Exception e) {
            return 5; // 默认值
        }
    }

    /**
     * 获取保活时间配置（分钟）
     */
    private long getKeepAliveDurationMinutes() {
        try {
            Field keepAliveDurationNsField = ConnectionPool.class.getDeclaredField("keepAliveDurationNs");
            keepAliveDurationNsField.setAccessible(true);
            long keepAliveDurationNs = keepAliveDurationNsField.getLong(connectionPool);
            return TimeUnit.NANOSECONDS.toMinutes(keepAliveDurationNs);
        } catch (Exception e) {
            return 5; // 默认值
        }
    }

    /**
     * 清理连接池（强制清理所有空闲连接）
     */
    public void evictAll() {
        connectionPool.evictAll();
        log.info("已清理所有空闲连接");
    }

}