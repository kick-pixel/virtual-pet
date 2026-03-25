package com.ruoyi.virtual.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.domain.VirtualRechargePackage;
import com.ruoyi.system.domain.VirtualRechargeOrder;
import com.ruoyi.system.domain.VirtualTokenExchangeRate;
import com.ruoyi.system.mapper.SysBlockchainConfigMapper;
import com.ruoyi.system.mapper.SysTokenConfigMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.IVirtualRechargeService;
import com.ruoyi.system.service.ITokenExchangeRateService;
import com.ruoyi.virtual.dto.request.CheckinOrderRequest;
import com.ruoyi.virtual.dto.request.RechargeRequest;
import com.ruoyi.virtual.dto.response.PaymentInfoResponse;
import com.ruoyi.virtual.security.VirtualSecurityUtils;

@RestController
@RequestMapping("/api/virtual/payment")
public class VirtualPaymentController
{
    @Autowired
    private IVirtualRechargeService virtualRechargeService;

    @Autowired
    private SysBlockchainConfigMapper blockchainConfigMapper;

    @Autowired
    private SysTokenConfigMapper tokenConfigMapper;

    @Autowired
    private ITokenExchangeRateService tokenExchangeRateService;

    @Autowired
    private ISysConfigService configService;

    @GetMapping("/wallet-address")
    public AjaxResult getWalletAddress(@RequestParam(required = false) String network)
    {
        List<SysBlockchainConfig> configs = blockchainConfigMapper.selectEnabledBlockchainConfigList();
        List<Map<String, Object>> result = configs.stream()
            .filter(c -> c.getWalletAddress() != null && !c.getWalletAddress().isEmpty())
            .filter(c -> network == null || network.isEmpty() || network.equalsIgnoreCase(c.getNetworkType()))
            .map(c -> {
                Map<String, Object> item = new HashMap<>();
                item.put("networkType", c.getNetworkType());
                item.put("networkName", c.getNetworkName());
                item.put("chainId", c.getChainId());
                item.put("walletAddress", c.getWalletAddress());
                return item;
            })
            .collect(Collectors.toList());
        return AjaxResult.success(result);
    }

    /**
     * 获取支持的区块链网络列表
     * 从 sys_blockchain_config 表读取已启用的网络配置
     */
    @GetMapping("/networks")
    public AjaxResult getNetworks()
    {
        List<SysBlockchainConfig> configs = blockchainConfigMapper.selectEnabledBlockchainConfigList();

        // 转换为前端需要的格式
        List<Map<String, Object>> networks = configs.stream()
            .map(c -> {
                Map<String, Object> network = new HashMap<>();
                network.put("networkType", c.getNetworkType());
                network.put("networkName", c.getNetworkName());
                network.put("chainId", c.getChainId());
                network.put("explorerUrl", c.getExplorerUrl());
                network.put("walletAddress", c.getWalletAddress());
                return network;
            })
            .collect(Collectors.toList());

        return AjaxResult.success(networks);
    }

    /**
     * 获取支持的代币列表
     * 从 sys_token_config 表读取已启用的代币配置
     *
     * @param networkType 网络类型（可选），用于过滤特定网络的代币
     */
    @GetMapping("/tokens")
    public AjaxResult getTokens(@RequestParam(required = false) String networkType)
    {
        List<SysTokenConfig> tokens = tokenConfigMapper.selectEnabledTokenConfigList();

        // 如果指定了网络类型，则过滤
        if (networkType != null && !networkType.isEmpty())
        {
            tokens = tokens.stream()
                .filter(t -> networkType.equalsIgnoreCase(t.getNetworkType()))
                .collect(Collectors.toList());
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> result = tokens.stream()
            .map(t -> {
                Map<String, Object> token = new HashMap<>();
                token.put("tokenSymbol", t.getTokenSymbol());
                token.put("tokenName", t.getTokenName());
                token.put("networkType", t.getNetworkType());
                token.put("contractAddress", t.getContractAddress());
                token.put("decimals", t.getDecimals());
                token.put("isNative", "1".equals(t.getIsNative()));
                token.put("logoUrl", t.getLogoUrl());
                return token;
            })
            .collect(Collectors.toList());

        return AjaxResult.success(result);
    }

    @GetMapping("/packages")
    public AjaxResult getPackages()
    {
        List<VirtualRechargePackage> packages = virtualRechargeService.getActivePackages();
        return AjaxResult.success(packages);
    }

    /**
     * 创建签到支付订单
     * POST /api/virtual/payment/checkin-order
     */
    @PostMapping("/checkin-order")
    public AjaxResult createCheckinOrder(@Valid @RequestBody CheckinOrderRequest request)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();

        SysBlockchainConfig chainConfig = blockchainConfigMapper.selectEnabledBlockchainConfigByChainId(request.getChainId());
        if (chainConfig == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.unsupported.network"));
        }
        if (chainConfig.getWalletAddress() == null || chainConfig.getWalletAddress().isEmpty())
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.no.platform.address"));
        }

        // 从 SysConfig 读取签到最小支付金额
        String feeKey = "BNB".equalsIgnoreCase(request.getPayToken()) ? "checkin.fee.bnb" : "checkin.fee.usdt";
        String feeAmount = configService.selectConfigByKey(feeKey);
        if (feeAmount == null || feeAmount.isEmpty())
        {
            return AjaxResult.error(MessageUtils.message("virtual.checkin.fee.not.configured"));
        }

        VirtualRechargeOrder order = virtualRechargeService.createCheckinOrder(
                userId, chainConfig.getNetworkType(), request.getPayToken(),
                feeAmount, request.getWalletAddress(), chainConfig.getWalletAddress());

        PaymentInfoResponse response = new PaymentInfoResponse();
        response.setOrderNo(order.getOrderNo());
        response.setPayAmount(order.getPayAmount());
        response.setPayNetwork(order.getPayNetwork());
        response.setPayToken(order.getPayToken());
        response.setReceiveAddress(order.getWalletAddress());
        response.setCreditsAmount(order.getCreditsAmount());
        response.setStatus(order.getStatus());
        return AjaxResult.success(response);
    }

    @PostMapping("/order")
    public AjaxResult createOrder(@Valid @RequestBody RechargeRequest request)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();

        VirtualRechargePackage pkg = virtualRechargeService.getPackageById(request.getPackageId());
        if (pkg == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.package.not.found"));
        }

        // 根据 chainId 查询已启用的网络配置（收款地址、网络类型等）
        SysBlockchainConfig chainConfig = blockchainConfigMapper.selectEnabledBlockchainConfigByChainId(request.getChainId());
        if (chainConfig == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.unsupported.network"));
        }
        if (chainConfig.getWalletAddress() == null || chainConfig.getWalletAddress().isEmpty())
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.no.platform.address"));
        }

        VirtualRechargeOrder order = virtualRechargeService.createOrder(
                userId, request.getPackageId(), chainConfig.getNetworkType(),
                request.getPayToken(), request.getWalletAddress(), chainConfig.getWalletAddress());

        PaymentInfoResponse response = new PaymentInfoResponse();
        response.setOrderNo(order.getOrderNo());
        response.setPayAmount(order.getPayAmount());
        response.setPayNetwork(order.getPayNetwork());
        response.setPayToken(order.getPayToken());
        response.setReceiveAddress(order.getWalletAddress());
        response.setCreditsAmount(order.getCreditsAmount());
        response.setStatus(order.getStatus());
        return AjaxResult.success(response);
    }

    @GetMapping("/orders")
    public AjaxResult getOrders()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        List<VirtualRechargeOrder> list = virtualRechargeService.getUserOrders(userId);
        return AjaxResult.success(list);
    }

    @GetMapping("/order/{orderNo}")
    public AjaxResult getOrder(@PathVariable String orderNo)
    {
        VirtualRechargeOrder order = virtualRechargeService.getOrderByNo(orderNo);
        if (order == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.order.not.found"));
        }
        return AjaxResult.success(order);
    }

    @PutMapping("/order/{orderNo}/cancel")
    public AjaxResult cancelOrder(@PathVariable String orderNo)
    {
        VirtualRechargeOrder order = virtualRechargeService.getOrderByNo(orderNo);
        if (order == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.order.not.found"));
        }
        virtualRechargeService.cancelOrder(order.getOrderId());
        return AjaxResult.success(MessageUtils.message("virtual.payment.order.cancel.success"));
    }

    /**
     * 提交交易哈希
     * 前端发送链上交易后，将 txHash 提交给后端，加速订单匹配
     */
    @PostMapping("/order/{orderNo}/tx-hash")
    public AjaxResult submitTxHash(@PathVariable String orderNo, @RequestBody Map<String, String> body)
    {
        String txHash = body.get("txHash");
        if (txHash == null || txHash.isEmpty())
        {
            return AjaxResult.error("Transaction hash is required");
        }

        Long userId = VirtualSecurityUtils.getCurrentUserId();
        VirtualRechargeOrder order = virtualRechargeService.getOrderByNo(orderNo);
        if (order == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.payment.order.not.found"));
        }

        // 安全校验：只能操作自己的订单
        if (!order.getUserId().equals(userId))
        {
            return AjaxResult.error("Unauthorized");
        }

        // pending（未提交 txHash）或 paid（已提交过 txHash，允许覆盖）均可提交
        // completed/cancelled/expired 状态则拒绝
        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus()))
        {
            return AjaxResult.error("Order status does not allow transaction hash submission");
        }

        // 更新订单的 txHash
        virtualRechargeService.updateOrderTxHash(order.getOrderId(), txHash);
        return AjaxResult.success("Transaction hash submitted successfully");
    }

    /**
     * 获取代币汇率列表
     * 返回所有启用的代币汇率配置
     */
    @GetMapping("/exchange-rates")
    public AjaxResult getExchangeRates(@RequestParam(required = false) String tokenSymbol)
    {
        List<VirtualTokenExchangeRate> rates = tokenExchangeRateService.listEnabledRates();

        // 如果指定了代币符号，则过滤
        if (tokenSymbol != null && !tokenSymbol.isEmpty())
        {
            rates = rates.stream()
                .filter(r -> tokenSymbol.equalsIgnoreCase(r.getTokenSymbol()))
                .collect(Collectors.toList());
        }

        return AjaxResult.success(rates);
    }
}
