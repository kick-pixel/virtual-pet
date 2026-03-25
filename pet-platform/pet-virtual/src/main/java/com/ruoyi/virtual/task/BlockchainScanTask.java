package com.ruoyi.virtual.task;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.ruoyi.web3.config.Web3Config;
import com.ruoyi.web3.service.ITransactionScanService;

/**
 * 区块链交易扫描任务
 *
 * 采用连续循环模式（而非 @Scheduled 固定间隔）：
 *   - 发现新交易时立即以短间隔复扫，最快跟上 BSC ~3s 出块速度
 *   - 无新交易时等待较长间隔，避免无效 RPC 调用
 *   - 通过 Redisson tryLock 防多实例重复扫描
 *   - ApplicationReadyEvent 触发，确保 DB/Redis 连接池全部就绪后再启动
 *
 * ⭐ 已从 pay-web3 模块移动到 pet-virtual
 * 原因：避免 ruoyi-admin 管理后台也触发此定时任务
 */
@Component
public class BlockchainScanTask
{
    private static final Logger log = LoggerFactory.getLogger(BlockchainScanTask.class);

    /** 与 DistributedLockAspect 中的前缀保持一致 */
    private static final String LOCK_PREFIX = "distributed_lock:";
    private static final String SCAN_LOCK   = LOCK_PREFIX + "schedule:web3:scan";
    private static final String UPDATE_LOCK = LOCK_PREFIX + "schedule:web3:update";

    @Autowired
    private Web3Config web3Config;

    @Autowired
    private ITransactionScanService scanService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 发现新交易后的快速复扫间隔（ms）
     * 默认 500ms：有活动时尽快处理下一批
     */
    @Value("${pet.web3.scan-interval-active:500}")
    private long activeInterval;

    /**
     * 无新交易时的等待间隔（ms）
     * 默认 2000ms：BSC 出块 ~3s，2s 足以跟上；也不会空转 RPC
     */
    @Value("${pet.web3.scan-interval:2000}")
    private long idleInterval;

    /**
     * 服务启动后延迟多久开始扫描（ms），等待 DB/Redis 连接池、Bean 完全就绪
     */
    @Value("${pet.web3.startup-delay:10000}")
    private long startupDelay;

    /**
     * Spring 容器完全就绪后启动扫描循环。
     * 使用 ApplicationReadyEvent 而非 @PostConstruct，
     * 确保所有 Bean（含数据库连接池、Redis 连接）均已初始化。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startScanLoop()
    {
        if (!web3Config.isScanEnabled())
        {
            log.info("区块链扫描已禁用（pet.web3.scan-enabled=false），跳过启动");
            return;
        }

        Thread t = new Thread(() -> {
            sleepQuietly(startupDelay);
            log.info("区块链扫描循环启动 [activeInterval={}ms, idleInterval={}ms]",
                activeInterval, idleInterval);
            runScanLoop();
        }, "blockchain-scan");
        t.setDaemon(true);   // JVM 退出时自动结束，无需手动关闭
        t.start();
    }

    // -------------------------------------------------------------------------
    // 主循环
    // -------------------------------------------------------------------------

    /**
     * 持续运行的扫描主循环，异常后自动恢复（5s 后重试）
     */
    private void runScanLoop()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                // 1. 扫描新区块（内部已对多网络并行处理）
                int scanned = tryWithLock(SCAN_LOCK, 30, scanService::scanAllNetworks);
                if (scanned > 0)
                {
                    log.info("区块链扫描：发现 {} 笔新交易", scanned);
                }

                // 2. 更新待确认交易的确认数及状态（内部已并行处理）
                int updated = tryWithLock(UPDATE_LOCK, 30, scanService::updatePendingTransactions);
                if (updated > 0)
                {
                    log.info("交易状态更新：处理 {} 笔", updated);
                }

                // 有新活动时快速复扫，安静时按常规间隔等待
                boolean active = scanned > 0 || updated > 0;
                sleepQuietly(active ? activeInterval : idleInterval);
            }
            catch (Exception e)
            {
                log.error("扫描循环发生异常，5秒后重试: {}", e.getMessage(), e);
                sleepQuietly(5_000);
            }
        }
        log.warn("区块链扫描线程已退出");
    }

    // -------------------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------------------

    /**
     * 尝试以 tryLock（不等待）获取 Redis 分布式锁，成功则执行任务。
     * 若锁被其他实例持有则直接返回 0（本轮跳过，不记日志）。
     *
     * @param lockKey  Redis key（含前缀）
     * @param leaseSec 锁的最大持有时间（秒），防止异常死锁
     * @param task     要执行的业务逻辑，返回处理数量
     * @return 业务返回值；获取锁失败时返回 0
     */
    private int tryWithLock(String lockKey, long leaseSec, Supplier<Integer> task)
    {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try
        {
            acquired = lock.tryLock(0, leaseSec, TimeUnit.SECONDS);
            if (!acquired)
            {
                return 0;  // 另一实例正持有锁，跳过本轮
            }
            return task.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return 0;
        }
        finally
        {
            if (acquired && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
    }

    private void sleepQuietly(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }
}
