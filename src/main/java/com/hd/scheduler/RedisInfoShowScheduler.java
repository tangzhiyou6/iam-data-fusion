package com.hd.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisInfoShowScheduler {

    @Autowired
    private RedisTemplate redisTemplate;

    // 从配置读取密码，若无则为空
    @Value("${spring.redis.password:}")
    private String password;

    //@Scheduled(fixedDelay = 60000)
    public void infoShow() {
        Map<String, Object> summaryData = getClusterSummary();
        String timestamp = (String) summaryData.get("timestamp");
        List<Map<String, String>> nodes = (List<Map<String, String>>) summaryData.get("nodes");
        Map<String, Object> summary = (Map<String, Object>) summaryData.get("summary");

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== Redis Cluster Resource Usage ==========\n");
        sb.append("Timestamp: ").append(timestamp).append("\n\n");

        // 表头（增加 Mem% 列）
        sb.append(String.format("%-22s %-12s %-8s %-10s %-10s %-10s\n",
                "Node", "Memory", "Mem%", "Clients", "QPS", "Hit Rate"));
        sb.append(repeatChar('-', 72)).append("\n");

        // 节点行
        for (Map<String, String> node : nodes) {
            String addr = node.get("address");
            if (node.containsKey("error")) {
                sb.append(String.format("%-22s %s\n", addr, "UNREACHABLE"));
            } else {
                sb.append(String.format("%-22s %-12s %-8s %-10s %-10s %-10s\n",
                        addr,
                        node.get("memory"),
                        node.get("memory_usage"),
                        node.get("clients"),
                        node.get("qps"),
                        node.get("hit_rate")));
            }
        }

        sb.append(repeatChar('-', 72)).append("\n");

        // 汇总行
        sb.append(String.format("%-22s %-12s %-8s %-10s %-10s %-10s\n",
                "TOTAL",
                summary.get("total_memory"),
                summary.get("avg_memory_usage"),   // 平均内存占用率
                summary.get("total_clients"),
                summary.get("total_qps"),
                summary.get("overall_hit_rate")));
        sb.append("==================================================\n");

        log.info(sb.toString());
    }

    /**
     * 获取集群各节点资源摘要，并返回汇总信息
     * 返回结构：{ "nodes": [ {...}, ... ], "summary": { "totalMemory": "...", "totalClients": ..., ... } }
     */
    public Map<String, Object> getClusterSummary() {
        List<Map<String, String>> nodeList = new ArrayList<>();
        long totalUsedMemory = 0;
        long totalConnectedClients = 0;
        long totalOpsPerSec = 0;
        long totalKeyspaceHits = 0;
        long totalKeyspaceMisses = 0;
        double totalMemoryPercent = 0;  // 用于计算平均占用率
        int validNodeCount = 0;

        RedisClusterConnection clusterConn = (RedisClusterConnection) redisTemplate.getConnectionFactory().getClusterConnection();
        Iterable<RedisClusterNode> nodes = clusterConn.clusterGetNodes();

        for (RedisNode node : nodes) {
            if (!node.isMaster()) continue;
            String addr = node.getHost() + ":" + node.getPort();
            try {
                String rawInfo = executeInfoOnNode(node.getHost(), node.getPort());
                Map<String, String> parsed = parseInfo(rawInfo);

                long usedMemory = parseBytes(parsed.get("used_memory"));
                long totalSysMemory = parseBytes(parsed.get("total_system_memory"));
                double memPercent = totalSysMemory > 0 ? (double) usedMemory / totalSysMemory * 100 : 0;

                Map<String, String> nodeInfo = new LinkedHashMap<>();
                nodeInfo.put("address", addr);
                nodeInfo.put("memory", parsed.getOrDefault("used_memory_human", "N/A"));
                nodeInfo.put("memory_usage", String.format("%.2f%%", memPercent));  // 新增
                nodeInfo.put("clients", parsed.getOrDefault("connected_clients", "0"));
                nodeInfo.put("qps", parsed.getOrDefault("instantaneous_ops_per_sec", "0"));
                nodeInfo.put("hit_rate", calculateHitRate(parsed));

                nodeList.add(nodeInfo);

                // 累加汇总
                totalUsedMemory += usedMemory;
                totalConnectedClients += Long.parseLong(parsed.getOrDefault("connected_clients", "0"));
                totalOpsPerSec += Long.parseLong(parsed.getOrDefault("instantaneous_ops_per_sec", "0"));
                totalKeyspaceHits += Long.parseLong(parsed.getOrDefault("keyspace_hits", "0"));
                totalKeyspaceMisses += Long.parseLong(parsed.getOrDefault("keyspace_misses", "0"));
                totalMemoryPercent += memPercent;
                validNodeCount++;
            } catch (Exception e) {
                Map<String, String> errorInfo = new LinkedHashMap<>();
                errorInfo.put("address", addr);
                errorInfo.put("error", "UNREACHABLE");
                nodeList.add(errorInfo);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_nodes", validNodeCount);
        summary.put("total_memory", formatBytes(totalUsedMemory));
        summary.put("avg_memory_usage", validNodeCount > 0 ? String.format("%.2f%%", totalMemoryPercent / validNodeCount) : "N/A");
        summary.put("total_clients", totalConnectedClients);
        summary.put("total_qps", totalOpsPerSec);
        double hitRate = (totalKeyspaceHits + totalKeyspaceMisses) > 0
                ? (double) totalKeyspaceHits / (totalKeyspaceHits + totalKeyspaceMisses) * 100
                : 0;
        summary.put("overall_hit_rate", String.format("%.2f%%", hitRate));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("nodes", nodeList);
        result.put("summary", summary);
        return result;
    }

    // ---------- 辅助方法 ----------

    private String executeInfoOnNode(String host, int port) {
        RedisURI uri = RedisURI.Builder.redis(host, port)
                .withTimeout(Duration.ofSeconds(5))
                .withPassword(password.toCharArray())
                .build();
        io.lettuce.core.RedisClient client = io.lettuce.core.RedisClient.create();
        try (StatefulRedisConnection<String, String> conn = client.connect(uri)) {
            return conn.sync().info();
        } finally {
            client.shutdown();
        }
    }

    private String calculateHitRate(Map<String, String> info) {
        try {
            long hits = Long.parseLong(info.getOrDefault("keyspace_hits", "0"));
            long misses = Long.parseLong(info.getOrDefault("keyspace_misses", "0"));
            long total = hits + misses;
            if (total == 0) return "0%";
            double rate = (double) hits / total * 100;
            return String.format("%.2f%%", rate);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private long parseBytes(String str) {
        if (str == null || str.isEmpty()) return 0;
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.2fKB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2fMB", bytes / (1024.0 * 1024));
        return String.format("%.2fGB", bytes / (1024.0 * 1024 * 1024));
    }

    public static Map<String, String> parseInfo(String infoText) {
        Map<String, String> map = new LinkedHashMap<>();
        if (infoText == null || infoText.isEmpty()) {
            return map;
        }
        String[] lines = infoText.split("\r\n|\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }
        return map;
    }

    /**
     * 按分段解析：保留 section 层级结构
     * 返回：section -> { key -> value }
     *
     * @param infoText INFO 命令返回的原始文本
     * @return section -> kv map
     */
    public static Map<String, Map<String, String>> parseInfoBySection(String infoText) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        if (infoText == null || infoText.isEmpty()) {
            return result;
        }
        String[] lines = infoText.split("\r\n|\n");
        String currentSection = "general";
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                currentSection = line.substring(1).trim().toLowerCase();
                result.putIfAbsent(currentSection, new LinkedHashMap<>());
                continue;
            }
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                result.computeIfAbsent(currentSection, k -> new LinkedHashMap<>())
                        .put(parts[0].trim(), parts[1].trim());
            }
        }
        return result;
    }

    /**
     * 快捷：从平铺 map 中取出内存使用量（human 格式）
     */
    public static String getUsedMemoryHuman(Map<String, String> info) {
        return info.getOrDefault("used_memory_human", "N/A");
    }

    /**
     * 快捷：计算命中率
     */
    public static String getHitRate(Map<String, String> info) {
        try {
            long hits = Long.parseLong(info.getOrDefault("keyspace_hits", "0"));
            long misses = Long.parseLong(info.getOrDefault("keyspace_misses", "0"));
            long total = hits + misses;
            if (total == 0) return "0%";
            return String.format("%.2f%%", (double) hits / total * 100);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String repeatChar(char ch, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, ch);
        return new String(chars);
    }
}
