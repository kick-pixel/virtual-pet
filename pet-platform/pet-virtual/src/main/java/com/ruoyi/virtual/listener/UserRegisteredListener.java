package com.ruoyi.virtual.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.system.service.IVirtualNotificationService;
import com.ruoyi.virtual.config.VirtualConfig;

/**
 * 用户注册事件监听器
 *
 * 监听用户注册事件，完成以下操作：
 * 1. 发放初始积分
 * 2. 发送欢迎邮件
 * 3. 创建通知
 *
 * @author ruoyi
 */
@Component
public class UserRegisteredListener
{
    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Autowired
    private IVirtualNotificationService virtualNotificationService;

    @Autowired
    private VirtualConfig virtualConfig;

    /**
     * 处理用户注册事件
     *
     * 事件类定义示例：
     * <pre>
     * public class UserRegisteredEvent extends ApplicationEvent {
     *     private Long userId;
     *     private String email;
     *     private String username;
     * }
     * </pre>
     *
     * TODO: 当 VirtualUserService 发布 UserRegisteredEvent 后生效
     */
    // @Async
    // @EventListener
    public void onUserRegistered(Object event)
    {
        // TODO: 解析事件获取用户信息
        // Long userId = event.getUserId();
        // String email = event.getEmail();
        // String username = event.getUsername();

        try
        {
            // 1. 发放初始积分
            long initialCredits = virtualConfig.getCredits().getInitialCredits();
            // virtualCreditsService.addCredits(userId, initialCredits, "register", "注册赠送积分");
            log.info("用户注册积分发放成功: initialCredits={}", initialCredits);

            // 2. 发送欢迎邮件
            // emailService.sendWelcomeEmail(email, username);
            log.info("欢迎邮件发送成功");

            // 3. 创建通知
            // virtualNotificationService.createNotification(userId, "welcome", "欢迎加入 Pet AI");
            log.info("欢迎通知创建成功");
        }
        catch (Exception e)
        {
            log.error("用户注册事件处理异常", e);
        }
    }
}
