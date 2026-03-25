package com.ruoyi.common.event.web3;

import java.math.BigDecimal;

/**
 * 交易失败事件
 *
 * 当交易状态变为失败时发布此事件
 *
 * 业务系统可以监听此事件进行：
 * - 发送交易失败通知
 * - 记录失败原因
 * - 触发告警
 * - 退款或补偿处理
 * - 统计失败率
 *
 * @author ruoyi
 */
public class TransactionFailedEvent extends PaymentEvent
{
    private static final long serialVersionUID = 1L;

    /** 失败原因 */
    private final String failureReason;

    /** 区块高度 */
    private final Long blockNumber;

    /** Gas费用（即使失败也会消耗Gas） */
    private final BigDecimal gasFee;

    public TransactionFailedEvent(Object source, Long transactionId, String txHash,
                                   Long networkId, String networkName, Long tokenId,
                                   String tokenSymbol, String fromAddress, String toAddress,
                                   BigDecimal amount, Long userId, String failureReason,
                                   Long blockNumber, BigDecimal gasFee)
    {
        super(source, transactionId, txHash, networkId, networkName, tokenId,
                tokenSymbol, fromAddress, toAddress, amount, userId);
        this.failureReason = failureReason;
        this.blockNumber = blockNumber;
        this.gasFee = gasFee;
    }

    public String getFailureReason()
    {
        return failureReason;
    }

    public Long getBlockNumber()
    {
        return blockNumber;
    }

    public BigDecimal getGasFee()
    {
        return gasFee;
    }

    @Override
    public String toString()
    {
        return "TransactionFailedEvent{" +
                "transactionId=" + getTransactionId() +
                ", txHash='" + getTxHash() + '\'' +
                ", networkName='" + getNetworkName() + '\'' +
                ", tokenSymbol='" + getTokenSymbol() + '\'' +
                ", amount=" + getAmount() +
                ", failureReason='" + failureReason + '\'' +
                ", userId=" + getUserId() +
                '}';
    }
}
