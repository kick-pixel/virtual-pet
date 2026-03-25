package com.ruoyi.web3.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.web3.PaymentReceivedEvent;
import com.ruoyi.common.event.web3.TransactionConfirmedEvent;
import com.ruoyi.common.event.web3.TransactionDetectedEvent;
import com.ruoyi.common.event.web3.TransactionFailedEvent;

/**
 * Web3支付事件监听器（示例）
 *
 * 展示如何监听Web3支付相关事件并执行业务逻辑
 * 业务系统可以参考此示例创建自己的监听器
 *
 * @author ruoyi
 */
@Component
public class PaymentEventListener
{
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    /**
     * 监听交易检测事件
     *
     * 当扫描到新交易时触发（此时交易可能未确认）
     * 可用于：发送待确认通知、记录初步信息、风控检查等
     */
    @EventListener
    @Async("eventExecutor")
    public void handleTransactionDetected(TransactionDetectedEvent event)
    {
        log.info("检测到新交易: txHash={}, amount={} {}, confirmations={}/{}",
            event.getTxHash(),
            event.getAmount(),
            event.getTokenSymbol(),
            event.getConfirmations(),
            event.getRequiredConfirmations()
        );
    }

    /**
     * 监听交易确认事件
     *
     * 当交易达到所需确认数时触发
     * 可用于：更新业务状态、触发后续流程等
     */
    @EventListener
    @Async("eventExecutor")
    public void handleTransactionConfirmed(TransactionConfirmedEvent event)
    {
        log.info("交易已确认: txHash={}, amount={} {}, userId={}, confirmations={}",
            event.getTxHash(),
            event.getAmount(),
            event.getTokenSymbol(),
            event.getUserId(),
            event.getConfirmations()
        );
    }

    /**
     * 监听交易失败事件
     *
     * 当交易状态变为失败时触发
     * 可用于：发送失败通知、记录失败原因、触发退款等
     */
    @EventListener
    @Async("eventExecutor")
    public void handleTransactionFailed(TransactionFailedEvent event)
    {
        log.warn("交易失败: txHash={}, amount={} {}, userId={}, reason={}",
            event.getTxHash(),
            event.getAmount(),
            event.getTokenSymbol(),
            event.getUserId(),
            event.getFailureReason()
        );
    }

    /**
     * 监听支付到账事件（最重要的事件）
     *
     * 当支付交易确认并成功匹配到用户时触发
     * 这是最重要的业务事件，表示用户支付已完成
     */
    @EventListener
    @Async("eventExecutor")
    public void handlePaymentReceived(PaymentReceivedEvent event)
    {
        log.info("支付到账: txHash={}, userId={}, amount={} {}, network={}, businessType={}",
            event.getTxHash(),
            event.getUserId(),
            event.getAmount(),
            event.getTokenSymbol(),
            event.getNetworkName(),
            event.getBusinessType()
        );

        // 在这里实现业务逻辑，例如：
        // 1. 用户余额充值
        // 2. 发送到账通知
        // 3. 订单状态更新
        // 4. 触发会员升级
        // 5. 赠送积分/优惠券
    }
}
