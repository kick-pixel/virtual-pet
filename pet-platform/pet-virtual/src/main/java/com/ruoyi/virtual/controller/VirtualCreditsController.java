package com.ruoyi.virtual.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.domain.VirtualCreditsTransaction;
import com.ruoyi.system.domain.VirtualCreditsRule;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.virtual.dto.response.CreditsBalanceResponse;
import com.ruoyi.virtual.security.VirtualSecurityUtils;

/**
 * 虚拟宠物平台积分控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/virtual/credits")
public class VirtualCreditsController extends BaseController {
    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    /**
     * 获取积分余额
     */
    @GetMapping("/balance")
    public AjaxResult getBalance() {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        VirtualUserCredits credits = virtualCreditsService.getBalance(userId);
        if (credits == null) {
            credits = virtualCreditsService.initCredits(userId);
        }

        CreditsBalanceResponse response = new CreditsBalanceResponse();
        response.setTotalCredits(credits.getBalance() + credits.getFrozen());
        response.setAvailableCredits(credits.getBalance());
        response.setFrozenCredits(credits.getFrozen());
        response.setTotalSpent(credits.getTotalSpent());
        return AjaxResult.success(response);
    }

    /**
     * 获取积分规则
     */
    @GetMapping("/rules")
    public AjaxResult getRules() {
        List<VirtualCreditsRule> rules = virtualCreditsService.getActiveRules();
        return AjaxResult.success(rules);
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/transactions")
    public TableDataInfo getTransactions(VirtualCreditsTransaction virtualCreditsTransaction) {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        virtualCreditsTransaction.setUserId(userId);
        startPage();
        List<VirtualCreditsTransaction> list = virtualCreditsService.selectTransactionList(virtualCreditsTransaction);
        return getDataTable(list);
    }

    /**
     * 预估任务消耗积分
     */
    @GetMapping("/estimate")
    public AjaxResult estimateCost(
            @RequestParam(defaultValue = "5") int duration,
            @RequestParam(defaultValue = "720p") String resolution,
            @RequestParam(required = false) String aspectRatio) {
        Long cost = virtualCreditsService.estimateCost(duration, resolution);

        // aspectRatio 暂时不影响积分计算，未来可以根据需要调整

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("cost", cost);
        result.put("duration", duration);
        result.put("resolution", resolution);
        result.put("aspectRatio", aspectRatio);
        return AjaxResult.success(result);
    }
}
