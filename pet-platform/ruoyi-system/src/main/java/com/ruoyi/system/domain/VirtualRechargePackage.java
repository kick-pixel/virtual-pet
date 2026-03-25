package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 充值套餐对象 virtual_recharge_package
 */
public class VirtualRechargePackage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long packageId;
    private String packageName;
    private Long creditsAmount;
    private Long bonusCredits;
    private BigDecimal priceUsdt;
    private BigDecimal priceEth;
    private BigDecimal priceBnb;
    private String badge;
    private String description;
    private String iconUrl;
    private Integer minPurchase;
    private Integer maxPurchase;
    private java.util.Date validStart;
    private java.util.Date validEnd;
    private Integer status;
    private Integer sortOrder;

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getCreditsAmount() {
        return creditsAmount;
    }

    public void setCreditsAmount(Long creditsAmount) {
        this.creditsAmount = creditsAmount;
    }

    public Long getBonusCredits() {
        return bonusCredits;
    }

    public void setBonusCredits(Long bonusCredits) {
        this.bonusCredits = bonusCredits;
    }

    public BigDecimal getPriceUsdt() {
        return priceUsdt;
    }

    public void setPriceUsdt(BigDecimal priceUsdt) {
        this.priceUsdt = priceUsdt;
    }

    public BigDecimal getPriceEth() {
        return priceEth;
    }

    public void setPriceEth(BigDecimal priceEth) {
        this.priceEth = priceEth;
    }

    public BigDecimal getPriceBnb() {
        return priceBnb;
    }

    public void setPriceBnb(BigDecimal priceBnb) {
        this.priceBnb = priceBnb;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getMinPurchase() {
        return minPurchase;
    }

    public void setMinPurchase(Integer minPurchase) {
        this.minPurchase = minPurchase;
    }

    public Integer getMaxPurchase() {
        return maxPurchase;
    }

    public void setMaxPurchase(Integer maxPurchase) {
        this.maxPurchase = maxPurchase;
    }

    public java.util.Date getValidStart() {
        return validStart;
    }

    public void setValidStart(java.util.Date validStart) {
        this.validStart = validStart;
    }

    public java.util.Date getValidEnd() {
        return validEnd;
    }

    public void setValidEnd(java.util.Date validEnd) {
        this.validEnd = validEnd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
