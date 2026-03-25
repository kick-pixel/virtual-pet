package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 积分规则对象 virtual_credits_rule
 */
public class VirtualCreditsRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private Long baseCredits;
    private Long perSecond;
    private String resolutionRate;
    private Long rewardCredits;
    private String rewardCondition;
    private String description;
    private Integer status;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Long getBaseCredits() {
        return baseCredits;
    }

    public void setBaseCredits(Long baseCredits) {
        this.baseCredits = baseCredits;
    }

    public Long getPerSecond() {
        return perSecond;
    }

    public void setPerSecond(Long perSecond) {
        this.perSecond = perSecond;
    }

    public String getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(String resolutionRate) {
        this.resolutionRate = resolutionRate;
    }

    public Long getRewardCredits() {
        return rewardCredits;
    }

    public void setRewardCredits(Long rewardCredits) {
        this.rewardCredits = rewardCredits;
    }

    public String getRewardCondition() {
        return rewardCondition;
    }

    public void setRewardCondition(String rewardCondition) {
        this.rewardCondition = rewardCondition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
