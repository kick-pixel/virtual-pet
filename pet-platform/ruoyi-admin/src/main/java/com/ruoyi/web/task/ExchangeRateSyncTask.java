package com.ruoyi.web.task;

import com.ruoyi.common.annotation.DistributedLock;
import com.ruoyi.system.domain.VirtualTokenExchangeRate;
import com.ruoyi.system.service.ITokenExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 代币汇率同步定时任务
 *
 * 功能：
 * 1. 定期从 CoinGecko/Binance API 获取最新汇率
 * 2. 更新数据库中的汇率配置
 * 3. 跳过手动设置的汇率
 *
 * 配置开关：pet.exchange-rate.sync-enabled (默认: false)
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "pet.exchange-rate.sync-enabled", havingValue = "true", matchIfMissing = false)
public class ExchangeRateSyncTask
{
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateSyncTask.class);

    @Autowired
    private ITokenExchangeRateService tokenExchangeRateService;

    /**
     * 同步所有代币汇率
     *
     * 执行频率：每 5 分钟执行一次
     * 分布式锁：防止多实例重复执行
     *
     * CoinGecko 免费额度：10-50 calls/min，足够使用
     */
    @Scheduled(cron = "0 */5 * * * ?")  // 每 5 分钟执行
    @DistributedLock(key = "schedule:exchange:rate:sync", leaseTime = 240)
    public void syncAllExchangeRates()
    {
        log.info("========== 开始同步代币汇率 ==========");

        try
        {
            // 查询所有启用的汇率配置
            List<VirtualTokenExchangeRate> rates = tokenExchangeRateService.listEnabledRates();

            if (rates.isEmpty())
            {
                log.warn("⚠️ 没有启用的汇率配置，跳过同步");
                return;
            }

            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;

            for (VirtualTokenExchangeRate rate : rates)
            {
                String tokenSymbol = rate.getTokenSymbol();

                // 跳过手动汇率
                if (Boolean.TRUE.equals(rate.getManualOverride()))
                {
                    log.debug("跳过手动汇率: {}", tokenSymbol);
                    skipCount++;
                    continue;
                }

                // 跳过稳定币（汇率固定为 1）
                if (isStablecoin(tokenSymbol))
                {
                    log.debug("跳过稳定币: {}", tokenSymbol);
                    skipCount++;
                    continue;
                }

                try
                {
                    // 从外部 API 同步汇率
                    boolean success = tokenExchangeRateService.syncExchangeRateFromApi(tokenSymbol);

                    if (success)
                    {
                        successCount++;
                        log.info("✅ 汇率同步成功: {}", tokenSymbol);
                    }
                    else
                    {
                        failCount++;
                        log.warn("❌ 汇率同步失败: {}", tokenSymbol);
                    }
                }
                catch (Exception e)
                {
                    failCount++;
                    log.error("❌ 汇率同步异常: tokenSymbol={}", tokenSymbol, e);
                }

                // 避免 API 限流，每次请求间隔 200ms
                Thread.sleep(200);
            }

            log.info("========== 汇率同步完成 ========== 成功: {}, 跳过: {}, 失败: {}",
                successCount, skipCount, failCount);
        }
        catch (Exception e)
        {
            log.error("汇率同步任务执行失败", e);
        }
    }

    /**
     * 判断是否为稳定币
     */
    private boolean isStablecoin(String tokenSymbol)
    {
        return tokenSymbol.equalsIgnoreCase("USDT")
            || tokenSymbol.equalsIgnoreCase("USDC")
            || tokenSymbol.equalsIgnoreCase("BUSD")
            || tokenSymbol.equalsIgnoreCase("DAI");
    }

    /**
     * 手动触发汇率同步（供管理员调用）
     */
    public void syncNow()
    {
        log.info("🔄 手动触发汇率同步");
        syncAllExchangeRates();
    }
}
