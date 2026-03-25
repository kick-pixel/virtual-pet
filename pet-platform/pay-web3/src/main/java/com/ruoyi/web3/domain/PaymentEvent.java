package com.ruoyi.web3.domain;

import java.math.BigDecimal;

/**
 * 支付事件
 *
 * @author ruoyi
 */
public class PaymentEvent
{
    /** 交易ID */
    private Long txId;

    /** 交易哈希 */
    private String txHash;

    /** 网络类型 */
    private String networkType;

    /** 发送方地址 */
    private String fromAddress;

    /** 接收方地址 */
    private String toAddress;

    /** 代币符号 */
    private String tokenSymbol;

    /** 金额 */
    private BigDecimal amount;

    /** 显示金额 */
    private String amountDisplay;

    /** 关联用户ID */
    private Long userId;

    /** 业务类型 */
    private String businessType;

    /** 业务ID */
    private Long businessId;

    public PaymentEvent()
    {
    }

    public PaymentEvent(Long txId, String txHash, String networkType, String fromAddress,
                        String toAddress, String tokenSymbol, BigDecimal amount, String amountDisplay)
    {
        this.txId = txId;
        this.txHash = txHash;
        this.networkType = networkType;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.tokenSymbol = tokenSymbol;
        this.amount = amount;
        this.amountDisplay = amountDisplay;
    }

    public Long getTxId()
    {
        return txId;
    }

    public void setTxId(Long txId)
    {
        this.txId = txId;
    }

    public String getTxHash()
    {
        return txHash;
    }

    public void setTxHash(String txHash)
    {
        this.txHash = txHash;
    }

    public String getNetworkType()
    {
        return networkType;
    }

    public void setNetworkType(String networkType)
    {
        this.networkType = networkType;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public String getToAddress()
    {
        return toAddress;
    }

    public void setToAddress(String toAddress)
    {
        this.toAddress = toAddress;
    }

    public String getTokenSymbol()
    {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol)
    {
        this.tokenSymbol = tokenSymbol;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getAmountDisplay()
    {
        return amountDisplay;
    }

    public void setAmountDisplay(String amountDisplay)
    {
        this.amountDisplay = amountDisplay;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    @Override
    public String toString()
    {
        return "PaymentEvent{" +
            "txHash='" + txHash + '\'' +
            ", networkType='" + networkType + '\'' +
            ", fromAddress='" + fromAddress + '\'' +
            ", tokenSymbol='" + tokenSymbol + '\'' +
            ", amountDisplay='" + amountDisplay + '\'' +
            '}';
    }
}
