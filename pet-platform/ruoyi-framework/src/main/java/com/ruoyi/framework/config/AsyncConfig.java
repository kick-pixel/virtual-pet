package com.ruoyi.framework.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步任务配置
 *
 * 用于配置Spring异步任务执行器，支持事件的异步处理
 *
 * @author ruoyi
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer
{
    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * 默认异步任务执行器（虚拟线程）
     *
     * 用于处理 @Async 注解的方法
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor()
    {
        log.info("异步任务执行器使用虚拟线程（Virtual Threads）");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 事件处理专用执行器（虚拟线程）
     *
     * 用于处理业务事件（Web3、AI等）的异步执行
     */
    @Bean(name = "eventExecutor")
    public Executor eventExecutor()
    {
        log.info("事件处理执行器使用虚拟线程（Virtual Threads）");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 获取默认的异步执行器
     */
    @Override
    public Executor getAsyncExecutor()
    {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 异步任务异常处理器
     *
     * 当异步方法抛出异常时的处理逻辑
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
    {
        return (throwable, method, params) -> {
            log.error("异步任务执行异常 - 方法: {}, 参数: {}, 异常: {}",
                method.getName(), params, throwable.getMessage(), throwable);
        };
    }
}
