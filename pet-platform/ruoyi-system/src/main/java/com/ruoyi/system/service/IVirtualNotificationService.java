package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualNotification;

/**
 * Virtual platform notification service
 */
public interface IVirtualNotificationService {
    /** Send notification */
    public VirtualNotification sendNotification(Long userId, String type, String title, String content,
            String extraData);

    /** Get user notifications */
    public List<VirtualNotification> getUserNotifications(Long userId);

    /** Get unread notifications */
    public List<VirtualNotification> getUnreadNotifications(Long userId);

    /** Get unread count */
    public int getUnreadCount(Long userId);

    /** Mark as read */
    public int markAsRead(Long notificationId);

    /** Mark all as read */
    public int markAllAsRead(Long userId);
}
