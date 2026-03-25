package com.ruoyi.system.domain;

/**
 * 管理后台签到统计 VO
 */
public class VirtualCheckinAdminVO {

    private Long userId;
    private String email;

    /** 最近签到时间 yyyy-MM-dd HH:mm:ss */
    private String checkinTime;

    /** 最近签到日期 yyyy-MM-dd */
    private String lastCheckinDate;

    /** 当前连续签到天数（来自 virtual_user.consecutive_days） */
    private Integer currentStreak;

    /** 历史最长连续签到天数 */
    private Integer maxStreakDays;

    /** 历史总签到天数 */
    private Integer totalCheckinDays;

    // ---- 查询条件 ----
    private Long queryUserId;
    private String beginTime;
    private String endTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(String checkinTime) {
        this.checkinTime = checkinTime;
    }

    public String getLastCheckinDate() {
        return lastCheckinDate;
    }

    public void setLastCheckinDate(String lastCheckinDate) {
        this.lastCheckinDate = lastCheckinDate;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getMaxStreakDays() {
        return maxStreakDays;
    }

    public void setMaxStreakDays(Integer maxStreakDays) {
        this.maxStreakDays = maxStreakDays;
    }

    public Integer getTotalCheckinDays() {
        return totalCheckinDays;
    }

    public void setTotalCheckinDays(Integer totalCheckinDays) {
        this.totalCheckinDays = totalCheckinDays;
    }

    public Long getQueryUserId() {
        return queryUserId;
    }

    public void setQueryUserId(Long queryUserId) {
        this.queryUserId = queryUserId;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
