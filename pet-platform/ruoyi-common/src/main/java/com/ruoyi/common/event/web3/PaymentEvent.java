package com.ruoyi.common.event.web3;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.context.ApplicationEvent;

/**
 * Web3支付事件基类
 *
 * 所有Web3支付相关事件的基类，包含交易的基本信息
 * 注意：事件时间戳使用父类 ApplicationEvent 的 getTimestamp() 方法（返回 long 类型）
 *
 * @author ruoyi
 */
public abstract class PaymentEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /** 交易ID */
    private final Long transactionId;

    /** 交易哈希 */
    private final String txHash;

    /** 网络ID */
    private final Long networkId;

    /** 网络名称 */
    private final String networkName;

    /** 代币ID */
    private final Long tokenId;

    /** 代币符号 */
    private final String tokenSymbol;

    /** 发送地址 */
    private final String fromAddress;

    /** 接收地址 */
    private final String toAddress;

    /** 交易金额 */
    private final BigDecimal amount;

    /** 用户ID（可能为null，如果交易未匹配到用户） */
    private final Long userId;

    public PaymentEvent(Object source, Long transactionId, String txHash,
                        Long networkId, String networkName, Long tokenId,
                        String tokenSymbol, String fromAddress, String toAddress,
                        BigDecimal amount, Long userId)
    {
        super(source);
        this.transactionId = transactionId;
        this.txHash = txHash;
        this.networkId = networkId;
        this.networkName = networkName;
        this.tokenId = tokenId;
        this.tokenSymbol = tokenSymbol;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.userId = userId;
    }

    public Long getTransactionId()
    {
        return transactionId;
    }

    public String getTxHash()
    {
        return txHash;
    }

    public Long getNetworkId()
    {
        return networkId;
    }

    public String getNetworkName()
    {
        return networkName;
    }

    public Long getTokenId()
    {
        return tokenId;
    }

    public String getTokenSymbol()
    {
        return tokenSymbol;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    public String getToAddress()
    {
        return toAddress;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public Long getUserId()
    {
        return userId;
    }

    /**
     * 判断交易是否已匹配到用户
     */
    public boolean hasUser()
    {
        return userId != null;
    }

    /**
     * 获取事件时间（Date类型）
     *
     * @return 事件时间
     */
    public Date getEventTime()
    {
        return new Date(getTimestamp());
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "transactionId=" + transactionId +
                ", txHash='" + txHash + '\'' +
                ", networkName='" + networkName + '\'' +
                ", tokenSymbol='" + tokenSymbol + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", amount=" + amount +
                ", userId=" + userId +
                ", timestamp=" + getEventTime() +
                '}';
    }
}
