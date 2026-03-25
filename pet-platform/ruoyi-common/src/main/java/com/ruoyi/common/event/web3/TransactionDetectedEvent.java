package com.ruoyi.common.event.web3;

import java.math.BigDecimal;

/**
 * 交易检测到事件
 *
 * 当区块链扫描检测到转账交易时发布此事件
 * 此时交易可能还未达到足够的确认数
 *
 * 业务系统可以监听此事件进行：
 * - 发送待确认通知
 * - 记录初步交易信息
 * - 触发风控检查
 * - 预处理业务逻辑
 *
 * @author ruoyi
 */
public class TransactionDetectedEvent extends PaymentEvent
{
    private static final long serialVersionUID = 1L;

    /** 区块高度 */
    private final Long blockNumber;

    /** 当前确认数 */
    private final Integer confirmations;

    /** 需要的确认数 */
    private final Integer requiredConfirmations;

    /** Gas费用 */
    private final BigDecimal gasFee;

    public TransactionDetectedEvent(Object source, Long transactionId, String txHash,
                                     Long networkId, String networkName, Long tokenId,
                                     String tokenSymbol, String fromAddress, String toAddress,
                                     BigDecimal amount, Long userId, Long blockNumber,
                                     Integer confirmations, Integer requiredConfirmations,
                                     BigDecimal gasFee)
    {
        super(source, transactionId, txHash, networkId, networkName, tokenId,
                tokenSymbol, fromAddress, toAddress, amount, userId);
        this.blockNumber = blockNumber;
        this.confirmations = confirmations;
        this.requiredConfirmations = requiredConfirmations;
        this.gasFee = gasFee;
    }

    public Long getBlockNumber()
    {
        return blockNumber;
    }

    public Integer getConfirmations()
    {
        return confirmations;
    }

    public Integer getRequiredConfirmations()
    {
        return requiredConfirmations;
    }

    public BigDecimal getGasFee()
    {
        return gasFee;
    }

    /**
     * 判断交易是否已确认
     */
    public boolean isConfirmed()
    {
        return confirmations != null && requiredConfirmations != null
                && confirmations >= requiredConfirmations;
    }

    @Override
    public String toString()
    {
        return "TransactionDetectedEvent{" +
                "transactionId=" + getTransactionId() +
                ", txHash='" + getTxHash() + '\'' +
                ", networkName='" + getNetworkName() + '\'' +
                ", tokenSymbol='" + getTokenSymbol() + '\'' +
                ", amount=" + getAmount() +
                ", blockNumber=" + blockNumber +
                ", confirmations=" + confirmations + "/" + requiredConfirmations +
                ", userId=" + getUserId() +
                '}';
    }
}
