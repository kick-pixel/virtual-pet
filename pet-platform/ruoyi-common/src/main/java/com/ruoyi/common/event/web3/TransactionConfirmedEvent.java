package com.ruoyi.common.event.web3;

import java.math.BigDecimal;

/**
 * 交易确认事件
 *
 * 当交易达到足够的确认数时发布此事件
 * 此时交易已经稳定，可以进行后续业务处理
 *
 * 业务系统可以监听此事件进行：
 * - **用户充值到账**
 * - **更新账户余额**
 * - **创建支付订单**
 * - **发送到账通知**
 * - **触发自动化业务流程**
 * - 记录交易统计
 *
 * @author ruoyi
 */
public class TransactionConfirmedEvent extends PaymentEvent
{
    private static final long serialVersionUID = 1L;

    /** 区块高度 */
    private final Long blockNumber;

    /** 确认数 */
    private final Integer confirmations;

    /** Gas费用 */
    private final BigDecimal gasFee;

    /** 区块时间 */
    private final Long blockTimestamp;

    public TransactionConfirmedEvent(Object source, Long transactionId, String txHash,
                                      Long networkId, String networkName, Long tokenId,
                                      String tokenSymbol, String fromAddress, String toAddress,
                                      BigDecimal amount, Long userId, Long blockNumber,
                                      Integer confirmations, BigDecimal gasFee,
                                      Long blockTimestamp)
    {
        super(source, transactionId, txHash, networkId, networkName, tokenId,
                tokenSymbol, fromAddress, toAddress, amount, userId);
        this.blockNumber = blockNumber;
        this.confirmations = confirmations;
        this.gasFee = gasFee;
        this.blockTimestamp = blockTimestamp;
    }

    public Long getBlockNumber()
    {
        return blockNumber;
    }

    public Integer getConfirmations()
    {
        return confirmations;
    }

    public BigDecimal getGasFee()
    {
        return gasFee;
    }

    public Long getBlockTimestamp()
    {
        return blockTimestamp;
    }

    @Override
    public String toString()
    {
        return "TransactionConfirmedEvent{" +
                "transactionId=" + getTransactionId() +
                ", txHash='" + getTxHash() + '\'' +
                ", networkName='" + getNetworkName() + '\'' +
                ", tokenSymbol='" + getTokenSymbol() + '\'' +
                ", amount=" + getAmount() +
                ", blockNumber=" + blockNumber +
                ", confirmations=" + confirmations +
                ", userId=" + getUserId() +
                '}';
    }
}
