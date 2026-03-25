package com.ruoyi.system.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生成选项配置对象 virtual_generation_option
 */
public class VirtualGenerationOption extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long optionId;
    private String optionGroup;
    private String optionCode;
    private String optionName;
    private String optionValue;
    private BigDecimal creditsModifier;
    private String extraConfig;
    private Integer isDefault;
    private Integer isPremium;
    private Integer status;
    private Integer sortOrder;

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(String optionGroup) {
        this.optionGroup = optionGroup;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public BigDecimal getCreditsModifier() {
        return creditsModifier;
    }

    public void setCreditsModifier(BigDecimal creditsModifier) {
        this.creditsModifier = creditsModifier;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Integer isPremium) {
        this.isPremium = isPremium;
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
