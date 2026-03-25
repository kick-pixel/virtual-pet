package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 代币汇率配置对象 virtual_token_exchange_rate
 *
 * @author ruoyi
 */
public class VirtualTokenExchangeRate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 汇率ID */
    private Long rateId;

    /** 代币符号 */
    private String tokenSymbol;

    /** 代币名称 */
    private String tokenName;

    /** 基准货币 */
    private String baseCurrency;

    /** 当前汇率（1代币=多少USDT） */
    private BigDecimal currentRate;

    /** 汇率来源 */
    private String rateSource;

    /** 汇率最后更新时间 */
    private Date lastUpdateTime;

    /** 每1 USDT 兑换多少积分 */
    private Long creditsPerUsdt;

    /** 最小充值金额 */
    private BigDecimal minAmount;

    /** 最大充值金额 */
    private BigDecimal maxAmount;

    /** 是否手动覆盖汇率 */
    private Boolean manualOverride;

    /** 手动设置的固定汇率 */
    private BigDecimal manualRate;

    /** 状态 */
    private Integer status;

    /** 优先级 */
    private Integer priority;

    // Getters and Setters

    public Long getRateId()
    {
        return rateId;
    }

    public void setRateId(Long rateId)
    {
        this.rateId = rateId;
    }

    public String getTokenSymbol()
    {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol)
    {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenName()
    {
        return tokenName;
    }

    public void setTokenName(String tokenName)
    {
        this.tokenName = tokenName;
    }

    public String getBaseCurrency()
    {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency)
    {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getCurrentRate()
    {
        return currentRate;
    }

    public void setCurrentRate(BigDecimal currentRate)
    {
        this.currentRate = currentRate;
    }

    public String getRateSource()
    {
        return rateSource;
    }

    public void setRateSource(String rateSource)
    {
        this.rateSource = rateSource;
    }

    public Date getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime)
    {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getCreditsPerUsdt()
    {
        return creditsPerUsdt;
    }

    public void setCreditsPerUsdt(Long creditsPerUsdt)
    {
        this.creditsPerUsdt = creditsPerUsdt;
    }

    public BigDecimal getMinAmount()
    {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount)
    {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount()
    {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount)
    {
        this.maxAmount = maxAmount;
    }

    public Boolean getManualOverride()
    {
        return manualOverride;
    }

    public void setManualOverride(Boolean manualOverride)
    {
        this.manualOverride = manualOverride;
    }

    public BigDecimal getManualRate()
    {
        return manualRate;
    }

    public void setManualRate(BigDecimal manualRate)
    {
        this.manualRate = manualRate;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    /**
     * 获取有效汇率（优先使用手动汇率）
     */
    public BigDecimal getEffectiveRate()
    {
        if (Boolean.TRUE.equals(manualOverride) && manualRate != null)
        {
            return manualRate;
        }
        return currentRate;
    }

    /**
     * 计算代币等价的 USDT 金额
     */
    public BigDecimal toUsdtAmount(BigDecimal tokenAmount)
    {
        return tokenAmount.multiply(getEffectiveRate());
    }

    /**
     * 计算可兑换的积分数量
     */
    public Long calculateCredits(BigDecimal tokenAmount)
    {
        BigDecimal usdtAmount = toUsdtAmount(tokenAmount);
        return usdtAmount.multiply(new BigDecimal(creditsPerUsdt)).longValue();
    }
}
