package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.domain.VirtualRechargeOrder;
import com.ruoyi.system.domain.VirtualRechargePackage;
import com.ruoyi.system.mapper.VirtualDailyCheckinMapper;
import com.ruoyi.system.mapper.VirtualRechargeOrderMapper;
import com.ruoyi.system.mapper.VirtualRechargePackageMapper;
import com.ruoyi.system.service.IVirtualCheckinService;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.system.service.IVirtualRechargeService;

/**
 * Virtual platform recharge service implementation
 */
@Service
public class VirtualRechargeServiceImpl implements IVirtualRechargeService {
    private static final Logger log = LoggerFactory.getLogger(VirtualRechargeServiceImpl.class);

    /** Order expiration time in minutes */
    private static final int ORDER_EXPIRE_MINUTES = 30;

    @Autowired
    private VirtualRechargePackageMapper packageMapper;

    @Autowired
    private VirtualRechargeOrderMapper orderMapper;

    @Autowired
    private IVirtualCreditsService creditsService;

    @Autowired
    private IVirtualCheckinService checkinService;

    @Autowired
    private VirtualDailyCheckinMapper checkinMapper;

    @Override
    public List<VirtualRechargePackage> getActivePackages() {
        return packageMapper.selectActivePackages();
    }

    @Override
    public VirtualRechargePackage getPackageById(Long packageId) {
        return packageMapper.selectByPackageId(packageId);
    }

    @Override
    @Transactional
    public VirtualRechargeOrder createOrder(Long userId, Long packageId, String payNetwork,
            String payToken, String fromAddress, String platformWalletAddress) {
        VirtualRechargePackage pkg = packageMapper.selectByPackageId(packageId);
        if (pkg == null || pkg.getStatus() != 1) {
            throw new ServiceException("Package not available");
        }

        VirtualRechargeOrder order = new VirtualRechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setCreditsAmount(pkg.getCreditsAmount());
        order.setBonusCredits(pkg.getBonusCredits());
        order.setPayNetwork(payNetwork);
        order.setPayToken(payToken);
        order.setPayAmount(pkg.getPriceUsdt());
        order.setPayAmountDisplay(pkg.getPriceUsdt() != null ? pkg.getPriceUsdt().toPlainString() + " USDT" : "");
        order.setWalletAddress(platformWalletAddress);
        order.setFromAddress(fromAddress);
        order.setStatus("pending");
        order.setOrderType("recharge");
        order.setExpireAt(new Date(System.currentTimeMillis() + ORDER_EXPIRE_MINUTES * 60 * 1000L));

        orderMapper.insertVirtualRechargeOrder(order);
        log.info("Created recharge order: {}, user: {}, package: {}", order.getOrderNo(), userId, packageId);
        return order;
    }

    @Override
    @Transactional
    public VirtualRechargeOrder createCheckinOrder(Long userId, String payNetwork, String payToken,
            String payAmount, String fromAddress, String platformWalletAddress) {
        // 1. 检查今日是否已签到
        String today = java.time.LocalDate.now().toString();
        if (checkinMapper.selectByUserIdAndDate(userId, today) != null) {
            throw new ServiceException(MessageUtils.message("virtual.checkin.already.claimed"));
        }
        // 2. 检查是否已有未完成的签到订单（pending/paid），避免重复支付
        List<VirtualRechargeOrder> activeOrders = orderMapper.selectByUserId(userId);
        boolean hasActive = activeOrders.stream().anyMatch(o ->
                "checkin".equals(o.getOrderType())
                && (
                    // "pending".equals(o.getStatus()) || 
                    "paid".equals(o.getStatus())));
        if (hasActive) {
            throw new ServiceException(MessageUtils.message("virtual.checkin.order.exists"));
        }

        VirtualRechargeOrder order = new VirtualRechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPackageId(null);
        order.setCreditsAmount(0L);
        order.setBonusCredits(0L);
        order.setPayNetwork(payNetwork);
        order.setPayToken(payToken);
        order.setPayAmount(new BigDecimal(payAmount));
        order.setPayAmountDisplay(payAmount + " " + payToken.toUpperCase());
        order.setWalletAddress(platformWalletAddress);
        order.setFromAddress(fromAddress);
        order.setStatus("pending");
        order.setOrderType("checkin");
        order.setExpireAt(new Date(System.currentTimeMillis() + ORDER_EXPIRE_MINUTES * 60 * 1000L));

        orderMapper.insertVirtualRechargeOrder(order);
        log.info("Created checkin order: {}, user: {}, token: {} {}", order.getOrderNo(), userId, payAmount, payToken);
        return order;
    }

    @Override
    public VirtualRechargeOrder getOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public VirtualRechargeOrder getOrderByTxHash(String txHash) {
        return orderMapper.selectByTxHash(txHash);
    }

    @Override
    public VirtualRechargeOrder findMatchingPendingOrder(String fromAddress, String payToken,
            String payNetwork, String txHash) {
        return orderMapper.selectMatchingPendingOrder(fromAddress, payToken, payNetwork, txHash);
    }

    @Override
    public List<VirtualRechargeOrder> getUserOrders(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, String txHash, Long paymentTxId) {
        VirtualRechargeOrder order = orderMapper.selectByOrderId(orderId);
        if (order == null) {
            throw new ServiceException("Order not found");
        }
        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            throw new ServiceException("Order cannot be completed in current status: " + order.getStatus());
        }

        // Update order status
        order.setStatus("completed");
        order.setTxHash(txHash);
        order.setPaymentTxId(paymentTxId);
        order.setPaidAt(new Date());
        order.setCompletedAt(new Date());
        orderMapper.updateVirtualRechargeOrder(order);

        if ("checkin".equals(order.getOrderType())) {
            // 签到支付：触发每日签到逻辑（写签到记录 + 按序列发放积分）
            int credits = checkinService.claimCheckin(order.getUserId());
            log.info("Completed checkin order: {}, credits awarded: {}", order.getOrderNo(), credits);
        } else {
            // 普通充值：按套餐面值发放积分
            Long creditsAmt = order.getCreditsAmount();
            Long bonusAmt = order.getBonusCredits();
            long totalCredits = (creditsAmt != null ? creditsAmt : 0L)
                    + (bonusAmt != null ? bonusAmt : 0L);
            creditsService.addCredits(order.getUserId(), totalCredits, "recharge",
                    "recharge_order", order.getOrderId(), "Recharge: " + order.getOrderNo());
            log.info("Completed recharge order: {}, credits: {}", order.getOrderNo(), totalCredits);
        }
    }

    @Override
    @Transactional
    public void updateOrderTxHash(Long orderId, String txHash) {
        VirtualRechargeOrder order = orderMapper.selectByOrderId(orderId);
        // pending 或 paid 均允许更新 txHash（paid 说明前端之前已提交过，此次覆盖）
        if (order != null && ("pending".equals(order.getStatus()) || "paid".equals(order.getStatus()))) {
            order.setTxHash(txHash);
            order.setStatus("paid"); // Mark as paid (waiting for blockchain confirmation)
            orderMapper.updateVirtualRechargeOrder(order);
            log.info("Updated order txHash: orderId={}, orderNo={}, txHash={}", orderId, order.getOrderNo(), txHash);
        }
    }

    @Override
    public int cancelOrder(Long orderId) {
        return orderMapper.updateOrderStatus(orderId, "cancelled");
    }

    @Override
    public int expirePendingOrders() {
        List<VirtualRechargeOrder> expired = orderMapper.selectExpiredPendingOrders();
        int count = 0;
        for (VirtualRechargeOrder order : expired) {
            orderMapper.updateOrderStatus(order.getOrderId(), "expired");
            count++;
        }
        if (count > 0) {
            log.info("Expired {} pending orders", count);
        }
        return count;
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
