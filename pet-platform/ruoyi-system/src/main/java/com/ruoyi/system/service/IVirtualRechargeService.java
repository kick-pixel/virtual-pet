package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualRechargePackage;
import com.ruoyi.system.domain.VirtualRechargeOrder;

/**
 * Virtual platform recharge service
 */
public interface IVirtualRechargeService {
    /** Get active recharge packages */
    public List<VirtualRechargePackage> getActivePackages();

    /** Get package by ID */
    public VirtualRechargePackage getPackageById(Long packageId);

    /** Create recharge order */
    public VirtualRechargeOrder createOrder(Long userId, Long packageId, String payNetwork,
            String payToken, String fromAddress, String platformWalletAddress);

    /**
     * 创建签到支付订单（orderType=checkin，无套餐，支付固定小额费用产生链上数据）
     * @param userId              用户ID
     * @param payNetwork          支付网络（BSC / ETH 等）
     * @param payToken            支付代币（BNB / USDT）
     * @param payAmount           最小支付金额（从 SysConfig 获取后传入）
     * @param fromAddress         用户钱包地址
     * @param platformWalletAddress 平台收款地址
     */
    public VirtualRechargeOrder createCheckinOrder(Long userId, String payNetwork, String payToken,
            String payAmount, String fromAddress, String platformWalletAddress);

    /** Get order by order number */
    public VirtualRechargeOrder getOrderByNo(String orderNo);

    /** Get order by tx hash */
    public VirtualRechargeOrder getOrderByTxHash(String txHash);

    /**
     * 兜底匹配：按发款地址+代币+网络查找最近的待处理订单
     * 用于前端 submitTxHash 失败时，扫描任务依然能关联订单
     */
    public VirtualRechargeOrder findMatchingPendingOrder(String fromAddress, String payToken,
            String payNetwork, String txHash);

    /** Get user order list */
    public List<VirtualRechargeOrder> getUserOrders(Long userId);

    /** Complete order (payment confirmed, credits added) */
    public void completeOrder(Long orderId, String txHash, Long paymentTxId);

    /** Update order tx hash (user submitted) */
    public void updateOrderTxHash(Long orderId, String txHash);

    /** Cancel order */
    public int cancelOrder(Long orderId);

    /** Expire pending orders that have passed their deadline */
    public int expirePendingOrders();
}
