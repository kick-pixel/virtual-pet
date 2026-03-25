package com.ruoyi.system.service;

import java.util.Map;

/**
 * 每日签到 Service 接口
 *
 * @author ruoyi
 */
public interface IVirtualCheckinService
{
    /**
     * 获取用户签到状态（含7天展示数据）
     *
     * @param userId 用户ID
     * @return { todayClaimed, currentSequence, days: [{day, credits, claimed, isToday, isGrandPrize}] }
     */
    Map<String, Object> getCheckinStatus(Long userId);

    /**
     * 领取今日签到积分（幂等：已签到则抛出异常）
     *
     * @param userId 用户ID
     * @return 本次奖励积分数
     */
    int claimCheckin(Long userId);
}
