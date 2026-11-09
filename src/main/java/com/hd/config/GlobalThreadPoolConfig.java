package com.hd.config;

import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 全局业务线程池：供数据融合解析等可并行任务复用。
 */
@Slf4j
@Configuration
public class GlobalThreadPoolConfig {

    public static final String GLOBAL_TASK_EXECUTOR = "globalTaskExecutor";

    @Bean(name = GLOBAL_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor globalTaskExecutor(
            @Value("${thread-pool.global.core-size:3}") int coreSize,
            @Value("${thread-pool.global.max-size:5}") int maxSize,
            @Value("${thread-pool.global.queue-capacity:2000}") int queueCapacity,
            @Value("${thread-pool.global.keep-alive-seconds:60}") int keepAliveSeconds) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("global-fusion-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("Global task executor started: core={}, max={}, queue={}",
                coreSize, maxSize, queueCapacity);
        return executor;
    }
}
