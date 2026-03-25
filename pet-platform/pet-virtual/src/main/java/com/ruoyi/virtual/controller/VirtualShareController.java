package com.ruoyi.virtual.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.VirtualShareRecord;
import com.ruoyi.system.service.IVirtualShareService;
import com.ruoyi.virtual.security.VirtualSecurityUtils;
import com.ruoyi.virtual.share.ShareProviderFactory;

/**
 * 视频分享 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/virtual/share")
public class VirtualShareController
{
    @Autowired
    private IVirtualShareService virtualShareService;

    @Autowired
    private ShareProviderFactory shareProviderFactory;

    /**
     * 获取支持的分享平台列表
     */
    @GetMapping("/platforms")
    public AjaxResult getPlatforms()
    {
        List<String> platforms = Arrays.asList("twitter", "telegram");
        return AjaxResult.success(platforms);
    }

    /**
     * 创建分享记录并返回分享链接
     *
     * @param taskId 视频任务 ID
     * @param body   包含 platform 字段
     */
    @PostMapping("/{taskId}")
    public AjaxResult createShare(@PathVariable Long taskId, @RequestBody Map<String, String> body)
    {
        String platform = body.get("platform");
        if (platform == null || platform.isBlank())
        {
            return AjaxResult.error("platform is required");
        }

        Long userId = VirtualSecurityUtils.getCurrentUserId();

        // 生成分享链接（通过 ShareProvider）
        String shareUrl = "";
        try
        {
            shareUrl = shareProviderFactory.getProvider(platform).generateShareUrl(taskId, userId);
        }
        catch (IllegalArgumentException e)
        {
            // 未注册的平台（如 telegram），使用默认空链接
        }

        // 创建或获取已有分享记录
        VirtualShareRecord record = virtualShareService.createShareRecord(userId, taskId, platform, shareUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("shareId", record.getShareId());
        result.put("shareUrl", shareUrl);
        result.put("platform", platform);
        return AjaxResult.success(result);
    }

    /**
     * 获取当前用户的分享记录
     */
    @GetMapping("/records")
    public AjaxResult getRecords()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        List<VirtualShareRecord> list = virtualShareService.getUserShareRecords(userId);
        return AjaxResult.success(list);
    }
}
