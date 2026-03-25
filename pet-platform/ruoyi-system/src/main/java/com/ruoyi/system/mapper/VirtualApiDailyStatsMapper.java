package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VirtualApiDailyStats;

/**
 * 视频 API 每日统计中间表 Mapper
 */
public interface VirtualApiDailyStatsMapper {

    /**
     * UPSERT 指定日期的统计数据（从 ai_video_task 实时聚合写入）
     * 用于：①定时任务每日凌晨刷新昨日 ②每小时刷新今日
     */
    int upsertDailyStats(@Param("statDate") String statDate);

    /** 按日期范围查询中间表（历史数据，O(days) 极快） */
    List<VirtualApiDailyStats> selectByDateRange(
            @Param("beginDate") String beginDate,
            @Param("endDate") String endDate);

    /** 今日实时聚合（不走中间表，直接查 ai_video_task） */
    VirtualApiDailyStats selectRealtimeForDate(@Param("statDate") String statDate);
}
