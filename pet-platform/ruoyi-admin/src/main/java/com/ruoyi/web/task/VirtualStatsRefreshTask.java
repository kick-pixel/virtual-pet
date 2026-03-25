package com.ruoyi.web.task;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DistributedLock;
import com.ruoyi.system.mapper.VirtualApiDailyStatsMapper;
import com.ruoyi.system.mapper.VirtualUserStatsMapper;

/**
 * 平台统计数据定时刷新任务
 *
 * <ul>
 *   <li>凌晨 1:00 - 全量刷新 virtual_user_stats（兜底，防止事件驱动遗漏）</li>
 *   <li>凌晨 2:00 - 刷新昨日 API 统计到 virtual_api_daily_stats</li>
 *   <li>每小时30分 - 刷新今日 API 统计（减少 Admin 实时查询压力）</li>
 * </ul>
 */
@Component
public class VirtualStatsRefreshTask {

    private static final Logger log = LoggerFactory.getLogger(VirtualStatsRefreshTask.class);

    @Autowired
    private VirtualApiDailyStatsMapper apiDailyStatsMapper;

    @Autowired
    private VirtualUserStatsMapper userStatsMapper;

    /**
     * 每日凌晨 1:00 全量刷新 virtual_user_stats
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @DistributedLock(key = "schedule:refresh:user:stats", leaseTime = 600)
    public void refreshUserStats() {
        log.info("[VirtualStats] 全量刷新 virtual_user_stats 开始");
        try {
            int rows = userStatsMapper.upsertAllFromAiVideoTask();
            log.info("[VirtualStats] virtual_user_stats 刷新完成，影响行数: {}", rows);
        } catch (Exception e) {
            log.error("[VirtualStats] virtual_user_stats 刷新失败", e);
        }
    }

    /**
     * 每日凌晨 2:00 刷新昨日 API 统计
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @DistributedLock(key = "schedule:refresh:api:stats:yesterday", leaseTime = 300)
    public void refreshApiDailyStats() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        log.info("[VirtualStats] 刷新 API 统计日期: {}", yesterday);
        try {
            int rows = apiDailyStatsMapper.upsertDailyStats(yesterday);
            log.info("[VirtualStats] virtual_api_daily_stats 刷新完成，日期: {}, 行数: {}", yesterday, rows);
        } catch (Exception e) {
            log.error("[VirtualStats] API 统计刷新失败，日期: {}", yesterday, e);
        }
    }

    /**
     * 每小时 30 分刷新今日 API 统计（减少管理页面实时查询压力）
     */
    @Scheduled(cron = "0 30 * * * ?")
    @DistributedLock(key = "schedule:refresh:api:stats:today", leaseTime = 60)
    public void refreshTodayApiStats() {
        String today = LocalDate.now().toString();
        try {
            apiDailyStatsMapper.upsertDailyStats(today);
            log.debug("[VirtualStats] 今日 API 统计刷新完成: {}", today);
        } catch (Exception e) {
            log.error("[VirtualStats] 今日 API 统计刷新失败: {}", today, e);
        }
    }
}
