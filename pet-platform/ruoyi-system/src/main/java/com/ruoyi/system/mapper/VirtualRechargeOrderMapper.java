package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualRechargeOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 充值订单Mapper接口
 */
public interface VirtualRechargeOrderMapper {
    public VirtualRechargeOrder selectByOrderId(Long orderId);

    public VirtualRechargeOrder selectByOrderNo(String orderNo);

    public VirtualRechargeOrder selectByTxHash(String txHash);

    public VirtualRechargeOrder selectMatchingPendingOrder(@Param("fromAddress") String fromAddress,
            @Param("payToken") String payToken, @Param("payNetwork") String payNetwork,
            @Param("txHash") String txHash);

    public List<VirtualRechargeOrder> selectByUserId(Long userId);

    public List<VirtualRechargeOrder> selectVirtualRechargeOrderList(VirtualRechargeOrder order);

    public List<VirtualRechargeOrder> selectExpiredPendingOrders();

    public int insertVirtualRechargeOrder(VirtualRechargeOrder order);

    public int updateVirtualRechargeOrder(VirtualRechargeOrder order);

    public int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /** 管理后台订单列表（JOIN 套餐表获取套餐名） */
    List<VirtualRechargeOrder> selectAdminOrderList(VirtualRechargeOrder query);
}
