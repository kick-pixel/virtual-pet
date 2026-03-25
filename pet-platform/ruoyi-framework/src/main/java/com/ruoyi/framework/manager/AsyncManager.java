package com.ruoyi.framework.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步任务管理器
 *
 * @author ruoyi
 */
public class AsyncManager
{
    private static final Logger log = LoggerFactory.getLogger(AsyncManager.class);

    /**
     * 单例模式
     */
    private AsyncManager(){}

    private static final AsyncManager me = new AsyncManager();

    public static AsyncManager me()
    {
        return me;
    }

    /**
     * 异步执行任务（虚拟线程）
     *
     * @param task 任务
     */
    public void execute(Runnable task)
    {
        if (task == null)
        {
            log.warn("异步任务为空，跳过执行");
            return;
        }
        Thread.ofVirtual()
            .name("async-task-", 0)
            .uncaughtExceptionHandler((t, e) -> log.error("异步任务执行异常 [{}]: {}", t.getName(), e.getMessage(), e))
            .start(task);
    }

    /**
     * 兼容旧调用，无需关闭虚拟线程
     */
    public void shutdown()
    {
        log.info("虚拟线程执行器无需手动关闭");
    }
}
