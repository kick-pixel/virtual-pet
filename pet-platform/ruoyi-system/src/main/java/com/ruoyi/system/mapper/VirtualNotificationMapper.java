package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualNotification;

/**
 * 用户通知Mapper接口
 */
public interface VirtualNotificationMapper {
    public VirtualNotification selectByNotificationId(Long notificationId);

    public List<VirtualNotification> selectByUserId(Long userId);

    public List<VirtualNotification> selectUnreadByUserId(Long userId);

    public List<VirtualNotification> selectVirtualNotificationList(VirtualNotification notification);

    public int selectUnreadCount(Long userId);

    public int insertVirtualNotification(VirtualNotification notification);

    public int updateVirtualNotification(VirtualNotification notification);

    public int markAsRead(Long notificationId);

    public int markAllAsRead(Long userId);
}
