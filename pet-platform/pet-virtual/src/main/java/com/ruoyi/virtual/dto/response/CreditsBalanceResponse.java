package com.ruoyi.virtual.dto.response;

/**
 * 积分余额响应
 */
public class CreditsBalanceResponse
{
    private Long totalCredits;
    private Long availableCredits;
    private Long frozenCredits;
    private Long totalSpent;
    private Long totalEarned;
    private Long totalRefunded;

    public Long getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Long totalCredits) { this.totalCredits = totalCredits; }

    public Long getAvailableCredits() { return availableCredits; }
    public void setAvailableCredits(Long availableCredits) { this.availableCredits = availableCredits; }

    public Long getFrozenCredits() { return frozenCredits; }
    public void setFrozenCredits(Long frozenCredits) { this.frozenCredits = frozenCredits; }

    public Long getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Long totalSpent) { this.totalSpent = totalSpent; }

    public Long getTotalEarned() { return totalEarned; }
    public void setTotalEarned(Long totalEarned) { this.totalEarned = totalEarned; }

    public Long getTotalRefunded() { return totalRefunded; }
    public void setTotalRefunded(Long totalRefunded) { this.totalRefunded = totalRefunded; }
}
