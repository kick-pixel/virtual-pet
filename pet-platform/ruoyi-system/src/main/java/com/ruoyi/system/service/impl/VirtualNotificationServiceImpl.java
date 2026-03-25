package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.VirtualNotification;
import com.ruoyi.system.mapper.VirtualNotificationMapper;
import com.ruoyi.system.service.IVirtualNotificationService;

/**
 * Virtual platform notification service implementation
 */
@Service
public class VirtualNotificationServiceImpl implements IVirtualNotificationService {
    @Autowired
    private VirtualNotificationMapper notificationMapper;

    @Override
    public VirtualNotification sendNotification(Long userId, String type, String title, String content,
            String extraData) {
        VirtualNotification notification = new VirtualNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setExtraData(extraData);
        notification.setIsRead(0);
        notification.setChannel("web");
        notificationMapper.insertVirtualNotification(notification);
        return notification;
    }

    @Override
    public List<VirtualNotification> getUserNotifications(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public List<VirtualNotification> getUnreadNotifications(Long userId) {
        return notificationMapper.selectUnreadByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.selectUnreadCount(userId);
    }

    @Override
    public int markAsRead(Long notificationId) {
        return notificationMapper.markAsRead(notificationId);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId);
    }
}
