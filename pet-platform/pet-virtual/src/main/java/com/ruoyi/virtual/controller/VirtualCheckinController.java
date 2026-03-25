package com.ruoyi.virtual.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.IVirtualCheckinService;
import com.ruoyi.virtual.security.VirtualSecurityUtils;

/**
 * 每日签到 Controller
 *
 * GET  /api/virtual/checkin/status  获取签到状态（需登录）
 * POST /api/virtual/checkin/claim   领取今日签到（需登录）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/virtual/checkin")
public class VirtualCheckinController extends BaseController
{
    @Autowired
    private IVirtualCheckinService checkinService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 获取签到所需最小支付金额
     * 从 sys_config 读取 checkin.fee.bnb / checkin.fee.usdt
     */
    @GetMapping("/fee")
    public AjaxResult getFee()
    {
        String bnbFee = configService.selectConfigByKey("checkin.fee.bnb");
        String usdtFee = configService.selectConfigByKey("checkin.fee.usdt");
        if (bnbFee == null || usdtFee == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.checkin.fee.not.configured"));
        }
        return AjaxResult.success(Map.of("bnb", bnbFee, "usdt", usdtFee));
    }

    @GetMapping("/status")
    public AjaxResult getStatus()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        Map<String, Object> status = checkinService.getCheckinStatus(userId);
        return AjaxResult.success(status);
    }

    @PostMapping("/claim")
    public AjaxResult claim()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        try
        {
            int credits = checkinService.claimCheckin(userId);
            return AjaxResult.success(Map.of("creditsAwarded", credits));
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }
}
