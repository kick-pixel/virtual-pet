package com.ruoyi.virtual.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.VirtualNotification;
import com.ruoyi.system.service.IVirtualNotificationService;
import com.ruoyi.virtual.security.VirtualSecurityUtils;

/**
 * 虚拟宠物平台通知控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/virtual/notifications")
public class VirtualNotificationController
{
    @Autowired
    private IVirtualNotificationService virtualNotificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        List<VirtualNotification> list = virtualNotificationService.getUserNotifications(userId);
        return AjaxResult.success(list);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        int count = virtualNotificationService.getUnreadCount(userId);
        return AjaxResult.success("count", count);
    }

    /**
     * 标记为已读
     */
    @PutMapping("/{notificationId}/read")
    public AjaxResult markAsRead(@PathVariable Long notificationId)
    {
        virtualNotificationService.markAsRead(notificationId);
        return AjaxResult.success();
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public AjaxResult markAllAsRead()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        virtualNotificationService.markAllAsRead(userId);
        return AjaxResult.success();
    }
}
