package com.hd.utils;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringUtils implements ApplicationContextAware, EnvironmentAware {

    private static ApplicationContext applicationContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        SpringUtils.environment = environment;
    }

    /* ========================== Bean 相关 ========================== */

    /**
     * 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 按类型获取 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     * 按名称获取 Bean
     */
    public static Object getBean(String beanName) {
        checkApplicationContext();
        return applicationContext.getBean(beanName);
    }

    /**
     * 按名称和类型获取 Bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(beanName, clazz);
    }

    /**
     * 判断 Bean 是否存在
     */
    public static boolean containsBean(String beanName) {
        checkApplicationContext();
        return applicationContext.containsBean(beanName);
    }

    /**
     * 获取指定类型的所有 Bean
     */
    public static <T> java.util.Map<String, T> getBeansOfType(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBeansOfType(clazz);
    }

    /* ========================== 环境配置相关 ========================== */

    /**
     * 获取 Environment
     */
    public static Environment getEnvironment() {
        Assert.notNull(environment, "Environment 未初始化");
        return environment;
    }

    /**
     * 获取配置文件属性
     */
    public static String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    /**
     * 获取配置文件属性（带默认值）
     */
    public static String getProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取配置文件属性并转换为指定类型
     */
    public static <T> T getProperty(String key, Class<T> targetType) {
        return getEnvironment().getProperty(key, targetType);
    }

    /* ========================== 私有方法 ========================== */

    private static void checkApplicationContext() {
        Assert.notNull(applicationContext,
                "ApplicationContext 未初始化，请确认 SpringUtils 已被 Spring 扫描并加载");
    }
}