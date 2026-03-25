package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.domain.VirtualCreditsTransaction;
import com.ruoyi.system.domain.VirtualCreditsRule;

/**
 * Virtual platform credits service
 */
public interface IVirtualCreditsService {
    /** Get user credits balance */
    public VirtualUserCredits getBalance(Long userId);

    /** Initialize credits for new user */
    public VirtualUserCredits initCredits(Long userId);

    /** Freeze credits for a task (pre-deduction) */
    public VirtualCreditsTransaction freezeCredits(Long userId, Long amount, String businessType, Long businessId,
            String description);

    /** Confirm spending of frozen credits (task completed) */
    public VirtualCreditsTransaction confirmSpend(Long userId, Long amount, String businessType, Long businessId,
            String description);

    /** Refund frozen credits (task failed) */
    public VirtualCreditsTransaction refundFrozen(Long userId, Long amount, String businessType, Long businessId,
            String description);

    /** Add credits (recharge/reward) */
    public VirtualCreditsTransaction addCredits(Long userId, Long amount, String txType, String businessType,
            Long businessId, String description);

    /** Estimate credits cost for video generation */
    public Long estimateCost(int durationSeconds, String resolution);

    /** Get transaction history */
    public List<VirtualCreditsTransaction> getTransactionHistory(Long userId);

    /** Get transaction history with filters */
    public List<VirtualCreditsTransaction> selectTransactionList(VirtualCreditsTransaction tx);

    /** Get all active credit rules */
    public List<VirtualCreditsRule> getActiveRules();

    /** Get rule by code */
    public VirtualCreditsRule getRuleByCode(String ruleCode);
}
