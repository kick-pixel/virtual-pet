package com.ruoyi.system.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.VirtualDailyCheckin;
import com.ruoyi.system.mapper.VirtualDailyCheckinMapper;
import com.ruoyi.system.service.IVirtualCheckinService;
import com.ruoyi.system.service.IVirtualCreditsService;

/**
 * 每日签到 Service 实现
 *
 * 签到积分规则（Day 1-7 循环）：
 *   Day1-6=30, Day7=50
 *
 * @author ruoyi
 */
@Service
public class VirtualCheckinServiceImpl implements IVirtualCheckinService
{
    /** 签到积分规则（索引 0=Day1 … 6=Day7） */
    private static final int[] CHECKIN_CREDITS = { 30, 30, 30, 30, 30, 30, 50 };

    @Autowired
    private VirtualDailyCheckinMapper checkinMapper;

    @Autowired
    private IVirtualCreditsService creditsService;

    @Override
    public Map<String, Object> getCheckinStatus(Long userId)
    {
        String today = LocalDate.now().toString();
        VirtualDailyCheckin todayRecord = checkinMapper.selectByUserIdAndDate(userId, today);
        int sequence = calculateCurrentSequence(userId, today, todayRecord);

        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 1; i <= 7; i++)
        {
            Map<String, Object> day = new HashMap<>();
            day.put("day", i);
            day.put("credits", CHECKIN_CREDITS[i - 1]);
            day.put("isGrandPrize", i == 7);
            day.put("claimed", i < sequence || (i == sequence && todayRecord != null));
            day.put("isToday", i == sequence);
            days.add(day);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayClaimed", todayRecord != null);
        result.put("currentSequence", sequence);
        result.put("days", days);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimCheckin(Long userId)
    {
        String today = LocalDate.now().toString();
        if (checkinMapper.selectByUserIdAndDate(userId, today) != null)
        {
            throw new ServiceException("今日已签到，请明天再来");
        }

        VirtualDailyCheckin todayRecord = null;
        int sequence = calculateCurrentSequence(userId, today, todayRecord);
        int credits = CHECKIN_CREDITS[sequence - 1];

        VirtualDailyCheckin checkin = new VirtualDailyCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(Date.valueOf(LocalDate.now()));
        checkin.setDaySequence(sequence);
        checkin.setCreditsAwarded(credits);
        checkinMapper.insert(checkin);

        // 6-param: userId, amount, txType, businessType, businessId, description
        creditsService.addCredits(userId, (long) credits, "checkin", "daily_checkin",
                checkin.getCheckinId(), "Daily checkin Day " + sequence);
        return credits;
    }

    /**
     * 计算当前应处于的签到序号（1-7）
     * 逻辑：按历史签到次数循环，不要求连续自然日
     */
    private int calculateCurrentSequence(Long userId, String today, VirtualDailyCheckin todayRecord)
    {
        if (todayRecord != null)
        {
            return todayRecord.getDaySequence();
        }
        int totalCount = checkinMapper.selectCountByUserId(userId);
        return totalCount % 7 + 1;
    }
}
