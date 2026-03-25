package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户生成统计中间表 virtual_user_stats
 */
public class VirtualUserStats {

    private Long userId;

    private Integer totalGenCount;

    private Integer failGenCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastGenTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTotalGenCount() {
        return totalGenCount;
    }

    public void setTotalGenCount(Integer totalGenCount) {
        this.totalGenCount = totalGenCount;
    }

    public Integer getFailGenCount() {
        return failGenCount;
    }

    public void setFailGenCount(Integer failGenCount) {
        this.failGenCount = failGenCount;
    }

    public Date getLastGenTime() {
        return lastGenTime;
    }

    public void setLastGenTime(Date lastGenTime) {
        this.lastGenTime = lastGenTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
