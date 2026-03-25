package com.ruoyi.virtual.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 虚拟宠物平台安全工具类
 *
 * @author ruoyi
 */
public class VirtualSecurityUtils
{
    private VirtualSecurityUtils() {}

    /**
     * 获取当前登录的虚拟平台用户
     *
     * @return VirtualLoginUser 或 null
     */
    public static VirtualLoginUser getCurrentUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof VirtualLoginUser)
        {
            return (VirtualLoginUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     * @throws RuntimeException 未登录时抛出
     */
    public static Long getCurrentUserId()
    {
        VirtualLoginUser user = getCurrentUser();
        if (user == null)
        {
            throw new RuntimeException("未获取到虚拟平台登录用户");
        }
        return user.getUserId();
    }

    /**
     * 当前用户邮箱是否已验证
     */
    public static boolean isEmailVerified()
    {
        VirtualLoginUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }
}
