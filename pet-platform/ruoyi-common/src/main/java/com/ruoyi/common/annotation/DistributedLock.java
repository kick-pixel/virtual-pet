package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解
 * 基于 Redisson 实现，用于防止多实例并发执行同一方法
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock
{
    /**
     * 锁的 key，为空时自动生成（类名:方法名）
     */
    String key() default "";

    /**
     * 锁持有时间（秒），超时自动释放，防止死锁
     * 默认 60 秒
     */
    long leaseTime() default 60;

    /**
     * 等待获取锁的时间（秒）
     * 默认 0，即不等待，获取不到立即返回
     */
    long waitTime() default 0;
}
