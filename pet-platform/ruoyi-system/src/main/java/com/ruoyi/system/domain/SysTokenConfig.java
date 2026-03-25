package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 代币配置对象 sys_token_config
 *
 * @author ruoyi
 */
public class SysTokenConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 代币ID */
    @Excel(name = "Token ID", cellType = ColumnType.NUMERIC)
    private Long tokenId;

    /** 所属网络类型 */
    @Excel(name = "Network Type")
    private String networkType;

    /** 代币符号（USDT/BNB/ETH） */
    @Excel(name = "Token Symbol")
    private String tokenSymbol;

    /** 代币名称 */
    @Excel(name = "Token Name")
    private String tokenName;

    /** 合约地址（原生币为NULL） */
    @Excel(name = "Contract Address")
    private String contractAddress;

    /** 小数位数 */
    @Excel(name = "Decimals", cellType = ColumnType.NUMERIC)
    private Integer decimals;

    /** 是否原生币（0否 1是） */
    @Excel(name = "Is Native", readConverterExp = "0=No,1=Yes")
    private String isNative;

    /** Logo图片URL */
    private String logoUrl;

    /** 状态（0启用 1停用） */
    @Excel(name = "Status", readConverterExp = "0=Enabled,1=Disabled")
    private String status;

    public Long getTokenId()
    {
        return tokenId;
    }

    public void setTokenId(Long tokenId)
    {
        this.tokenId = tokenId;
    }

    public String getNetworkType()
    {
        return networkType;
    }

    public void setNetworkType(String networkType)
    {
        this.networkType = networkType;
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

    public String getContractAddress()
    {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress)
    {
        this.contractAddress = contractAddress;
    }

    public Integer getDecimals()
    {
        return decimals;
    }

    public void setDecimals(Integer decimals)
    {
        this.decimals = decimals;
    }

    public String getIsNative()
    {
        return isNative;
    }

    public void setIsNative(String isNative)
    {
        this.isNative = isNative;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        this.logoUrl = logoUrl;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("tokenId", getTokenId())
            .append("networkType", getNetworkType())
            .append("tokenSymbol", getTokenSymbol())
            .append("tokenName", getTokenName())
            .append("contractAddress", getContractAddress())
            .append("decimals", getDecimals())
            .append("isNative", getIsNative())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
