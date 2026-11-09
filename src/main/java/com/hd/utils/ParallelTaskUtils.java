package com.hd.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

/**
 * 基于全局线程池的并行调用辅助：将列表拆成单条任务提交并汇总 Integer 结果。
 */
@Slf4j
public final class ParallelTaskUtils {

    private ParallelTaskUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 将 list 按单元素拆分后并行执行 mapper，等待全部完成并累加返回值。
     *
     * @param items    待处理列表
     * @param executor 全局线程池
     * @param mapper   单条处理逻辑，返回成功条数（通常 0/1）
     * @return 成功条数之和
     */
    public static <T> int mapSum(List<T> items,
                                 ThreadPoolTaskExecutor executor,
                                 Function<T, Integer> mapper) {
        if (CollectionUtils.isEmpty(items)) {
            return 0;
        }
        if (items.size() == 1) {
            Integer one = mapper.apply(items.get(0));
            return one == null ? 0 : one;
        }

        List<Callable<Integer>> tasks = new ArrayList<>(items.size());
        for (T item : items) {
            tasks.add(() -> {
                Integer r = mapper.apply(item);
                return r == null ? 0 : r;
            });
        }

        int succ = 0;
        try {
            List<Future<Integer>> futures = executor.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Integer> future : futures) {
                try {
                    Integer r = future.get();
                    if (r != null) {
                        succ += r;
                    }
                } catch (Exception e) {
                    log.error("Parallel task failed: {}", e.getMessage(), e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Parallel invoke interrupted: {}", e.getMessage(), e);
        }
        return succ;
    }

    public static <T> List<T> singleton(T item) {
        return Collections.singletonList(item);
    }
}
