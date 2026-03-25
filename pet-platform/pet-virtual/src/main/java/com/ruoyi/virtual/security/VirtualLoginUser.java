package com.ruoyi.virtual.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import com.alibaba.fastjson2.annotation.JSONField;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 虚拟宠物平台登录用户
 *
 * 与 RuoYi admin 的 LoginUser 独立，使用独立的 Redis 键空间
 *
 * @author ruoyi
 */
public class VirtualLoginUser implements UserDetails, Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID（virtual_user.user_id） */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 邮箱是否已验证 */
    private boolean emailVerified;

    /** 头像 URL */
    private String avatar;

    /** 用户唯一标识（UUID，用于 Redis 缓存键） */
    private String token;

    /** 登录时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;

    /** 登录 IP */
    private String ipaddr;

    /** 登录浏览器 */
    private String browser;

    /** 登录操作系统 */
    private String os;

    // ===== UserDetails 实现 =====

    /**
     * 获取用户权限集合
     *
     * 注意：此方法不序列化到 Redis，避免 Fastjson 反序列化 SimpleGrantedAuthority 时出错
     */
    @Override
    @JSONField(serialize = false)
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        // 平台用户统一角色，无细粒度权限
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_VIRTUAL_USER"));
    }

    @Override
    public String getPassword()
    {
        return null; // 密码不存储在 token 对象中
    }

    @Override
    public String getUsername()
    {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    // ===== Getters and Setters =====

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getLoginTime() { return loginTime; }
    public void setLoginTime(Long loginTime) { this.loginTime = loginTime; }

    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }

    public String getIpaddr() { return ipaddr; }
    public void setIpaddr(String ipaddr) { this.ipaddr = ipaddr; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
}
