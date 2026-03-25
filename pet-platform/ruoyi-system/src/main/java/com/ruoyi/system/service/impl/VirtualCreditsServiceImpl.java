package com.ruoyi.system.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.VirtualCreditsRule;
import com.ruoyi.system.domain.VirtualCreditsTransaction;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.mapper.VirtualCreditsRuleMapper;
import com.ruoyi.system.mapper.VirtualCreditsTransactionMapper;
import com.ruoyi.system.mapper.VirtualUserCreditsMapper;
import com.ruoyi.system.service.IVirtualCreditsService;

/**
 * Virtual platform credits service implementation
 */
@Service
public class VirtualCreditsServiceImpl implements IVirtualCreditsService {
    private static final Logger log = LoggerFactory.getLogger(VirtualCreditsServiceImpl.class);

    /** Max optimistic lock retry count */
    private static final int MAX_RETRIES = 3;
    /** Register reward credits */
    private static final long REGISTER_REWARD = 0L;

    @Autowired
    private VirtualUserCreditsMapper creditsMapper;

    @Autowired
    private VirtualCreditsTransactionMapper txMapper;

    @Autowired
    private VirtualCreditsRuleMapper ruleMapper;

    @Override
    public VirtualUserCredits getBalance(Long userId) {
        return creditsMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public VirtualUserCredits initCredits(Long userId) {
        VirtualUserCredits credits = creditsMapper.selectByUserId(userId);
        if (credits != null) {
            return credits;
        }

         credits = new VirtualUserCredits();
         credits.setUserId(userId);
         credits.setBalance(REGISTER_REWARD);
         credits.setFrozen(0L);
         credits.setTotalEarned(REGISTER_REWARD);
         credits.setTotalSpent(0L);
         credits.setTotalRefunded(0L);
         creditsMapper.insertVirtualUserCredits(credits);

//         Record registration reward transaction
//         VirtualCreditsTransaction tx = buildTransaction(userId, REGISTER_REWARD,
//         "reward", 1,
//         0L, REGISTER_REWARD, "register", null, "Registration bonus");
//         txMapper.insertVirtualCreditsTransaction(tx);

         log.info("Initialized credits for user {}, reward: {}", userId,
         REGISTER_REWARD);
        return credits;
    }

    @Override
    @Transactional
    public VirtualCreditsTransaction freezeCredits(Long userId, Long amount, String businessType,
            Long businessId, String description) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            VirtualUserCredits credits = creditsMapper.selectByUserId(userId);
            if (credits == null) {
                throw new ServiceException("Credits account not found");
            }
            if (credits.getBalance() < amount) {
                throw new ServiceException("Insufficient credits balance");
            }

            int rows = creditsMapper.freezeCredits(userId, amount, credits.getVersion());
            if (rows > 0) {
                VirtualCreditsTransaction tx = buildTransaction(userId, amount, "freeze", -1,
                        credits.getBalance(), credits.getBalance() - amount,
                        businessType, businessId, description);
                txMapper.insertVirtualCreditsTransaction(tx);
                log.info("Frozen {} credits for user {}, business: {}/{}", amount, userId, businessType, businessId);
                return tx;
            }
        }
        throw new ServiceException("Credits operation failed, please try again");
    }

    @Override
    @Transactional
    public VirtualCreditsTransaction confirmSpend(Long userId, Long amount, String businessType,
            Long businessId, String description) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            VirtualUserCredits credits = creditsMapper.selectByUserId(userId);
            if (credits == null) {
                throw new ServiceException("Credits account not found");
            }

            int rows = creditsMapper.confirmSpend(userId, amount, credits.getVersion());
            if (rows > 0) {
                VirtualCreditsTransaction tx = buildTransaction(userId, amount, "spend", -1,
                        credits.getBalance(), credits.getBalance(),
                        businessType, businessId, description);
                txMapper.insertVirtualCreditsTransaction(tx);
                log.info("Confirmed spend {} credits for user {}, business: {}/{}", amount, userId, businessType,
                        businessId);
                return tx;
            }
        }
        throw new ServiceException("Credits operation failed, please try again");
    }

    @Override
    @Transactional
    public VirtualCreditsTransaction refundFrozen(Long userId, Long amount, String businessType,
            Long businessId, String description) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            VirtualUserCredits credits = creditsMapper.selectByUserId(userId);
            if (credits == null) {
                throw new ServiceException("Credits account not found");
            }

            int rows = creditsMapper.refundFrozen(userId, amount, credits.getVersion());
            if (rows > 0) {
                VirtualCreditsTransaction tx = buildTransaction(userId, amount, "refund", 1,
                        credits.getBalance(), credits.getBalance() + amount,
                        businessType, businessId, description);
                txMapper.insertVirtualCreditsTransaction(tx);
                log.info("Refunded {} credits for user {}, business: {}/{}", amount, userId, businessType, businessId);
                return tx;
            }
        }
        throw new ServiceException("Credits operation failed, please try again");
    }

    @Override
    @Transactional
    public VirtualCreditsTransaction addCredits(Long userId, Long amount, String txType,
            String businessType, Long businessId, String description) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            VirtualUserCredits credits = creditsMapper.selectByUserId(userId);
            if (credits == null) {
                throw new ServiceException("Credits account not found");
            }

            int rows = creditsMapper.addCredits(userId, amount, credits.getVersion());
            if (rows > 0) {
                VirtualCreditsTransaction tx = buildTransaction(userId, amount, txType, 1,
                        credits.getBalance(), credits.getBalance() + amount,
                        businessType, businessId, description);
                txMapper.insertVirtualCreditsTransaction(tx);
                log.info("Added {} credits for user {}, type: {}", amount, userId, txType);
                return tx;
            }
        }
        throw new ServiceException("Credits operation failed, please try again");
    }

    @Override
    public Long estimateCost(int durationSeconds, String resolution) {
        VirtualCreditsRule rule = ruleMapper.selectByRuleCode("video_generation");
        if (rule == null) {
            return 0L;
        }

        long baseCost = rule.getBaseCredits() != null ? rule.getBaseCredits() : 100;
        long perSecond = rule.getPerSecond() != null ? rule.getPerSecond() : 10;

        // Parse resolution rate from JSON
        double resRate = 1.0;
        if (rule.getResolutionRate() != null && resolution != null) {
            String rateStr = rule.getResolutionRate();
            // Simple JSON parsing for resolution rate
            String key = "\"" + resolution + "\":";
            int idx = rateStr.indexOf(key);
            if (idx >= 0) {
                int start = idx + key.length();
                int end = rateStr.indexOf(",", start);
                if (end < 0)
                    end = rateStr.indexOf("}", start);
                if (end > start) {
                    try {
                        resRate = Double.parseDouble(rateStr.substring(start, end).trim());
                    } catch (NumberFormatException e) {
                        resRate = 1.0;
                    }
                }
            }
        }

        long cost = (long) ((baseCost + perSecond * durationSeconds) * resRate);
        return cost;
    }

    @Override
    public List<VirtualCreditsTransaction> getTransactionHistory(Long userId) {
        return txMapper.selectByUserId(userId);
    }

    @Override
    public List<VirtualCreditsTransaction> selectTransactionList(VirtualCreditsTransaction tx) {
        return txMapper.selectVirtualCreditsTransactionList(tx);
    }

    @Override
    public List<VirtualCreditsRule> getActiveRules() {
        return ruleMapper.selectActiveRules();
    }

    @Override
    public VirtualCreditsRule getRuleByCode(String ruleCode) {
        return ruleMapper.selectByRuleCode(ruleCode);
    }

    /**
     * Build credit transaction record
     */
    private VirtualCreditsTransaction buildTransaction(Long userId, Long amount, String txType, Integer direction,
            Long balanceBefore, Long balanceAfter,
            String businessType, Long businessId, String description) {
        VirtualCreditsTransaction tx = new VirtualCreditsTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setTxType(txType);
        tx.setDirection(direction);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setBusinessType(businessType);
        tx.setBusinessId(businessId);
        tx.setDescription(description);
        return tx;
    }
}
