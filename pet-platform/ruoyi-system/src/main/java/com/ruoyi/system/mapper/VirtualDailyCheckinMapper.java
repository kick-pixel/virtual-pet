package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VirtualDailyCheckin;

/**
 * 每日签到记录 数据层
 *
 * @author ruoyi
 */
public interface VirtualDailyCheckinMapper
{
    int insert(VirtualDailyCheckin checkin);

    VirtualDailyCheckin selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") String date);

    VirtualDailyCheckin selectByUserIdAndYesterday(@Param("userId") Long userId, @Param("date") String date);

    int selectCountByUserId(@Param("userId") Long userId);

    List<VirtualDailyCheckin> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /** 管理后台：用户签到统计列表 */
    List<com.ruoyi.system.domain.VirtualCheckinAdminVO> selectAdminCheckinList(
            com.ruoyi.system.domain.VirtualCheckinAdminVO query);
}
