package com.ruoyi.system.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 每日签到记录 virtual_daily_checkin
 *
 * @author ruoyi
 */
public class VirtualDailyCheckin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 签到ID */
    private Long checkinId;

    /** 用户ID */
    private Long userId;

    /** 签到日期 */
    private Date checkinDate;

    /** 连续签到第几天(1-7) */
    private Integer daySequence;

    /** 本次奖励积分 */
    private Integer creditsAwarded;

    public Long getCheckinId()
    {
        return checkinId;
    }

    public void setCheckinId(Long checkinId)
    {
        this.checkinId = checkinId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Date getCheckinDate()
    {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate)
    {
        this.checkinDate = checkinDate;
    }

    public Integer getDaySequence()
    {
        return daySequence;
    }

    public void setDaySequence(Integer daySequence)
    {
        this.daySequence = daySequence;
    }

    public Integer getCreditsAwarded()
    {
        return creditsAwarded;
    }

    public void setCreditsAwarded(Integer creditsAwarded)
    {
        this.creditsAwarded = creditsAwarded;
    }
}
