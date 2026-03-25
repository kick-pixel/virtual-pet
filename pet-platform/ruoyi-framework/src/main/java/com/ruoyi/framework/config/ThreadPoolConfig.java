package com.ruoyi.framework.config;

import com.ruoyi.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author ruoyi
 **/
@Configuration
public class ThreadPoolConfig
{
    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor()
    {
        // 虚拟线程：轻量级，无需配置池大小
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 执行周期性或定时任务
     *
     * 注：ScheduledExecutorService 接口要求平台线程，此处保留小规模线程池仅用于调度触发，
     * 实际业务逻辑由调用方在虚拟线程中执行（如 AsyncManager 的 TimerTask）。
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService()
    {
        return new ScheduledThreadPoolExecutor(4,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy())
        {
            @Override
            protected void afterExecute(Runnable r, Throwable t)
            {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
}
