package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 视频 API 每日统计中间表 virtual_api_daily_stats
 */
public class VirtualApiDailyStats {

    /** 统计日期 yyyy-MM-dd */
    private String statDate;

    /** 调用总次数 */
    private Integer totalCount;

    /** 成功次数 */
    private Integer successCount;

    /** 失败次数 */
    private Integer failCount;

    /** 超时次数 */
    private Integer timeoutCount;

    /** 平均生成耗时（秒） */
    private BigDecimal avgDurationS;

    /** 成功率（百分比） */
    private BigDecimal successRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public Integer getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(Integer timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public BigDecimal getAvgDurationS() {
        return avgDurationS;
    }

    public void setAvgDurationS(BigDecimal avgDurationS) {
        this.avgDurationS = avgDurationS;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(BigDecimal successRate) {
        this.successRate = successRate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
