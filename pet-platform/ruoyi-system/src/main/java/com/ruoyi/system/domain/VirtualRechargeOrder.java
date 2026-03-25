package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 充值订单对象 virtual_recharge_order
 */
public class VirtualRechargeOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long packageId;
    private Long creditsAmount;
    private Long bonusCredits;
    private String payNetwork;
    private String payToken;
    private BigDecimal payAmount;
    private String payAmountDisplay;
    private String walletAddress;
    private String fromAddress;
    private String status;
    /** 订单类型：recharge-充值积分，checkin-签到支付 */
    private String orderType;
    private String txHash;
    private Long paymentTxId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paidAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireAt;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getCreditsAmount() {
        return creditsAmount;
    }

    public void setCreditsAmount(Long creditsAmount) {
        this.creditsAmount = creditsAmount;
    }

    public Long getBonusCredits() {
        return bonusCredits;
    }

    public void setBonusCredits(Long bonusCredits) {
        this.bonusCredits = bonusCredits;
    }

    public String getPayNetwork() {
        return payNetwork;
    }

    public void setPayNetwork(String payNetwork) {
        this.payNetwork = payNetwork;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getPayAmountDisplay() {
        return payAmountDisplay;
    }

    public void setPayAmountDisplay(String payAmountDisplay) {
        this.payAmountDisplay = payAmountDisplay;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getPaymentTxId() {
        return paymentTxId;
    }

    public void setPaymentTxId(Long paymentTxId) {
        this.paymentTxId = paymentTxId;
    }

    public Date getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Date paidAt) {
        this.paidAt = paidAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    /** 套餐名称（JOIN virtual_recharge_package，管理后台列表使用） */
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** 查询条件：时间范围 */
    private String beginTime;
    private String endTime;

    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
