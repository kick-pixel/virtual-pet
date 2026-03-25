package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DistributedLock;

/**
 * 分布式锁处理
 * 基于 Redisson 实现，拦截 @DistributedLock 注解的方法
 *
 * @author ruoyi
 */
@Aspect
@Component
public class DistributedLockAspect
{
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    private static final String LOCK_KEY_PREFIX = "distributed_lock:";

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint point, DistributedLock distributedLock) throws Throwable
    {
        String lockKey = getLockKey(distributedLock, point);
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try
        {
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired)
            {
                log.warn("【分布式锁】获取锁失败，跳过执行: key={}", lockKey);
                return null;
            }

            log.debug("【分布式锁】获取锁成功: key={}, leaseTime={}s", lockKey, leaseTime);
            return point.proceed();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            log.error("【分布式锁】获取锁被中断: key={}", lockKey);
            return null;
        }
        finally
        {
            if (acquired && lock.isHeldByCurrentThread())
            {
                lock.unlock();
                log.debug("【分布式锁】释放锁: key={}", lockKey);
            }
        }
    }

    /**
     * 获取锁的 key
     */
    private String getLockKey(DistributedLock distributedLock, ProceedingJoinPoint point)
    {
        String key = distributedLock.key();
        if (key != null && !key.isEmpty())
        {
            return LOCK_KEY_PREFIX + key;
        }

        // 自动生成 key: 类名:方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        return LOCK_KEY_PREFIX + targetClass.getSimpleName() + ":" + method.getName();
    }
}
