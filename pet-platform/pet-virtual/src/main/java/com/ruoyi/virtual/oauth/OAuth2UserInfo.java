package com.ruoyi.virtual.oauth;

/**
 * OAuth2 用户信息（统一 DTO）
 */
public class OAuth2UserInfo
{
    /** 提供商用户 ID */
    private String providerId;

    /** 邮箱 */
    private String email;

    /** 用户名/昵称 */
    private String name;

    /** 头像 URL */
    private String avatar;

    /** 提供商类型 */
    private String provider;

    /** Access Token */
    private String accessToken;

    /** Refresh Token */
    private String refreshToken;

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
