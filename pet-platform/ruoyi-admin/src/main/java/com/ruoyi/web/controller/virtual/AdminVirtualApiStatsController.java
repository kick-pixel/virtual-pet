package com.ruoyi.web.controller.virtual;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.VirtualApiDailyStats;
import com.ruoyi.system.mapper.VirtualApiDailyStatsMapper;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 管理后台 - 视频 API 统计
 */
@RestController
@RequestMapping("/admin/virtual/stats")
public class AdminVirtualApiStatsController extends BaseController {

    @Autowired
    private VirtualApiDailyStatsMapper apiDailyStatsMapper;

    /**
     * 查询 API 每日统计（混合策略：历史走中间表，今日实时聚合）
     *
     * @param days      最近 N 天（默认 7，beginDate 优先）
     * @param beginDate 开始日期 yyyy-MM-dd
     * @param endDate   结束日期 yyyy-MM-dd（默认今日）
     */
    @PreAuthorize("@ss.hasPermi('virtual:stats:list')")
    @GetMapping("/api")
    public AjaxResult getApiStats(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {

        LocalDate today = LocalDate.now();
        LocalDate end = (endTime != null) ? LocalDate.parse(endTime) : today;
        LocalDate begin = (beginTime != null) ? LocalDate.parse(beginTime) : end.minusDays(days - 1);

        Map<String, VirtualApiDailyStats> statsMap = new HashMap<>();

        // ① 历史日期：读中间表，O(days) 极快
        if (begin.isBefore(today)) {
            LocalDate histEnd = end.isBefore(today) ? end : today.minusDays(1);
            List<VirtualApiDailyStats> history = apiDailyStatsMapper.selectByDateRange(
                    begin.toString(), histEnd.toString());
            for (VirtualApiDailyStats s : history) {
                statsMap.put(s.getStatDate(), s);
            }
        }

        // ② 今日数据：实时聚合（今日数据持续变化，不依赖中间表）
        if (!end.isBefore(today)) {
            VirtualApiDailyStats todayStats = apiDailyStatsMapper.selectRealtimeForDate(today.toString());
            if (todayStats != null && todayStats.getTotalCount() != null) {
                statsMap.put(today.toString(), todayStats);
            }
        }

        // ③ 补齐日期返回
        List<VirtualApiDailyStats> result = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            VirtualApiDailyStats s = statsMap.get(dateStr);
            if (s == null || s.getTotalCount() == null || s.getTotalCount() == 0) {
                s = new VirtualApiDailyStats();
                s.setStatDate(dateStr);
                s.setTotalCount(0);
                s.setSuccessCount(0);
                s.setFailCount(0);
                s.setTimeoutCount(0);
                s.setSuccessRate(BigDecimal.ZERO);
            }
            result.add(s);
            current = current.plusDays(1);
        }

        return AjaxResult.success(result);
    }
}
