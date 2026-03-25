package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 管理后台平台用户列表 VO（含关联统计数据）
 */
public class VirtualUserAdminVO {

    private Long userId;
    private String email;
    private String nickname;
    private String telegramId;
    private String walletAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    private String registerIp;
    private String countryRegion;

    /** 账号状态 0-禁用 1-正常 */
    private Integer status;

    /** 禁止生成 0-正常 1-禁止 */
    private Integer genDisabled;

    /** 累计生成次数 */
    private Integer totalGenCount;

    /** 生成失败次数 */
    private Integer failGenCount;

    /** 累计购买积分 */
    private Long totalEarned;

    /** 累计消耗积分 */
    private Long totalSpent;

    /** 剩余积分 */
    private Long balance;

    // ---- 查询条件（不参与响应，供 Mapper 使用） ----
    private String beginTime;
    private String endTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getTelegramId() { return telegramId; }
    public void setTelegramId(String telegramId) { this.telegramId = telegramId; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public String getRegisterIp() { return registerIp; }
    public void setRegisterIp(String registerIp) { this.registerIp = registerIp; }

    public String getCountryRegion() { return countryRegion; }
    public void setCountryRegion(String countryRegion) { this.countryRegion = countryRegion; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getGenDisabled() { return genDisabled; }
    public void setGenDisabled(Integer genDisabled) { this.genDisabled = genDisabled; }

    public Integer getTotalGenCount() { return totalGenCount; }
    public void setTotalGenCount(Integer totalGenCount) { this.totalGenCount = totalGenCount; }

    public Integer getFailGenCount() { return failGenCount; }
    public void setFailGenCount(Integer failGenCount) { this.failGenCount = failGenCount; }

    public Long getTotalEarned() { return totalEarned; }
    public void setTotalEarned(Long totalEarned) { this.totalEarned = totalEarned; }

    public Long getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Long totalSpent) { this.totalSpent = totalSpent; }

    public Long getBalance() { return balance; }
    public void setBalance(Long balance) { this.balance = balance; }

    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
