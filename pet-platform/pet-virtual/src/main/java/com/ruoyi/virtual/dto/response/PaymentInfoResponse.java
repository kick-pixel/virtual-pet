package com.ruoyi.virtual.dto.response;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付信息响应
 */
public class PaymentInfoResponse
{
    private String orderNo;
    private Long creditsAmount;
    private Long bonusCredits;
    private String payNetwork;
    private String payToken;
    private BigDecimal payAmount;
    private String receiveAddress;
    private String status;
    private Date createdTime;
    private Date expireTime;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getCreditsAmount() { return creditsAmount; }
    public void setCreditsAmount(Long creditsAmount) { this.creditsAmount = creditsAmount; }

    public Long getBonusCredits() { return bonusCredits; }
    public void setBonusCredits(Long bonusCredits) { this.bonusCredits = bonusCredits; }

    public String getPayNetwork() { return payNetwork; }
    public void setPayNetwork(String payNetwork) { this.payNetwork = payNetwork; }

    public String getPayToken() { return payToken; }
    public void setPayToken(String payToken) { this.payToken = payToken; }

    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }

    public String getReceiveAddress() { return receiveAddress; }
    public void setReceiveAddress(String receiveAddress) { this.receiveAddress = receiveAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
}
