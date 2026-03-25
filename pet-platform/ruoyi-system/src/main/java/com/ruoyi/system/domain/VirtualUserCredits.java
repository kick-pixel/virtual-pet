package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户积分对象 virtual_user_credits
 */
public class VirtualUserCredits extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long balance;
    private Long frozen;
    private Long totalEarned;
    private Long totalSpent;
    private Long totalRefunded;
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozen() {
        return frozen;
    }

    public void setFrozen(Long frozen) {
        this.frozen = frozen;
    }

    public Long getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(Long totalEarned) {
        this.totalEarned = totalEarned;
    }

    public Long getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Long totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Long getTotalRefunded() {
        return totalRefunded;
    }

    public void setTotalRefunded(Long totalRefunded) {
        this.totalRefunded = totalRefunded;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
