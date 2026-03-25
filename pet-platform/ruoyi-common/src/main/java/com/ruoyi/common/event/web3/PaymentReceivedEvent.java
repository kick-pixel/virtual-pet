package com.ruoyi.common.event.web3;

import java.math.BigDecimal;

/**
 * 支付到账事件
 *
 * 当支付交易确认并成功匹配到用户时发布此事件
 * 这是最重要的业务事件，表示用户支付已完成
 *
 * 业务系统可以监听此事件进行：
 * - **用户余额充值**（最常用）
 * - **订单状态更新**
 * - **发送到账通知**（短信、邮件、站内信）
 * - **触发会员升级**
 * - **赠送积分/优惠券**
 * - **执行自动购买逻辑**
 * - 记录支付统计
 * - 财务对账
 *
 * @author ruoyi
 */
public class PaymentReceivedEvent extends PaymentEvent
{
    private static final long serialVersionUID = 1L;

    /** 区块高度 */
    private final Long blockNumber;

    /** 确认数 */
    private final Integer confirmations;

    /** 交易状态 */
    private final String status;

    /** 订单号（如果有） */
    private final String orderNo;

    /** 业务类型（充值、购买等） */
    private final String businessType;

    public PaymentReceivedEvent(Object source, Long transactionId, String txHash,
                                 Long networkId, String networkName, Long tokenId,
                                 String tokenSymbol, String fromAddress, String toAddress,
                                 BigDecimal amount, Long userId, Long blockNumber,
                                 Integer confirmations, String status, String orderNo,
                                 String businessType)
    {
        super(source, transactionId, txHash, networkId, networkName, tokenId,
                tokenSymbol, fromAddress, toAddress, amount, userId);
        this.blockNumber = blockNumber;
        this.confirmations = confirmations;
        this.status = status;
        this.orderNo = orderNo;
        this.businessType = businessType;
    }

    public Long getBlockNumber()
    {
        return blockNumber;
    }

    public Integer getConfirmations()
    {
        return confirmations;
    }

    public String getStatus()
    {
        return status;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    /**
     * 判断是否有订单号
     */
    public boolean hasOrder()
    {
        return orderNo != null && !orderNo.isEmpty();
    }

    @Override
    public String toString()
    {
        return "PaymentReceivedEvent{" +
                "transactionId=" + getTransactionId() +
                ", txHash='" + getTxHash() + '\'' +
                ", networkName='" + getNetworkName() + '\'' +
                ", tokenSymbol='" + getTokenSymbol() + '\'' +
                ", amount=" + getAmount() +
                ", userId=" + getUserId() +
                ", blockNumber=" + blockNumber +
                ", confirmations=" + confirmations +
                ", status='" + status + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", businessType='" + businessType + '\'' +
                '}';
    }
}
