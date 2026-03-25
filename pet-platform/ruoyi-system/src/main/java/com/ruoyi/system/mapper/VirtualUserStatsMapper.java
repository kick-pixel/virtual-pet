package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VirtualUserStats;

/**
 * 用户生成统计中间表 Mapper
 */
public interface VirtualUserStatsMapper {

    /** 全量刷新：从 ai_video_task 重新聚合所有用户统计（每日凌晨兜底） */
    int upsertAllFromAiVideoTask();

    /** 增量：视频完成时 total_gen_count +1 */
    int incrementTotalGenCount(@Param("userId") Long userId);

    /** 增量：视频失败时 total_gen_count +1 且 fail_gen_count +1 */
    int incrementBothCounts(@Param("userId") Long userId);

    /** 查询单用户统计 */
    VirtualUserStats selectByUserId(@Param("userId") Long userId);
}
