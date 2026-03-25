package com.ruoyi.virtual.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 签到支付订单请求
 */
public class CheckinOrderRequest
{
    /** 钱包链 ID（用于后台查询对应网络配置） */
    @NotNull(message = "Chain ID cannot be empty")
    private Long chainId;

    /** 支付代币（如 BNB、USDT） */
    @NotBlank(message = "Payment token cannot be empty")
    private String payToken;

    /** 用户钱包地址（发款地址） */
    @NotBlank(message = "Wallet address cannot be empty")
    private String walletAddress;

    public Long getChainId() { return chainId; }
    public void setChainId(Long chainId) { this.chainId = chainId; }

    public String getPayToken() { return payToken; }
    public void setPayToken(String payToken) { this.payToken = payToken; }

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
}
