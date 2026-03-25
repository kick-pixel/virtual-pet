package com.ruoyi.virtual.listener;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.web3.PaymentReceivedEvent;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.service.IVirtualCreditsService;

@Component
public class PaymentCreditsListener
{
    private static final Logger log = LoggerFactory.getLogger(PaymentCreditsListener.class);

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @EventListener
    @Async("eventExecutor")
    public void onPaymentReceived(PaymentReceivedEvent event)
    {
        try
        {
            Long userId = event.getUserId();
            BigDecimal amount = event.getAmount();
            String txHash = event.getTxHash();
            String tokenSymbol = event.getTokenSymbol();
            String networkName = event.getNetworkName();
            String businessType = event.getBusinessType();

            log.info("Processing payment received event: userId={}, amount={} {}, txHash={}, network={}, type={}",
                userId, amount, tokenSymbol, txHash, networkName, businessType);

            Long credits = calculateCreditsWithFallback(amount, tokenSymbol);

            String txHashShort = txHash.length() > 10 ? txHash.substring(0, 10) + "..." : txHash;
            String description = MessageUtils.message("virtual.credits.web3.recharge",
                amount.toPlainString(), tokenSymbol, txHashShort);

            virtualCreditsService.addCredits(
                userId,
                credits,
                "recharge",
                "web3_payment",
                event.getTransactionId(),
                description
            );

            log.info("Payment received, credits added: userId={}, credits={}, amount={} {}, txHash={}",
                userId, credits, amount, tokenSymbol, txHash);
        }
        catch (Exception e)
        {
            log.error("Failed to process payment event: txHash={}, userId={}",
                event.getTxHash(), event.getUserId(), e);
        }
    }

    private Long calculateCreditsWithFallback(BigDecimal amount, String tokenSymbol)
    {
        BigDecimal rate;
        switch (tokenSymbol.toUpperCase())
        {
            case "BNB":
                rate = new BigDecimal("10000");
                break;
            case "USDT":
            case "USDC":
            case "BUSD":
                rate = new BigDecimal("100");
                break;
            case "ETH":
                rate = new BigDecimal("20000");
                break;
            default:
                rate = new BigDecimal("100");
                log.warn("Unknown token type: {}, using default conversion rate 1:100", tokenSymbol);
        }

        Long credits = amount.multiply(rate).longValue();
        log.info("Credits calculation: {} {} * {} = {} credits", amount, tokenSymbol, rate, credits);
        return credits;
    }
}
