package com.ruoyi.virtual.dto.response;

import java.util.List;

/**
 * 用户信息响应
 */
public class UserInfoResponse
{
    private Long userId;
    private String username;
    private String email;
    private boolean emailVerified;
    private String avatarUrl;
    private String nickname;
    private String locale;
    private Long totalCredits;
    private Long availableCredits;
    private List<OAuthBinding> oauthBindings;
    private List<WalletBinding> walletBindings;

    // ===== Getters and Setters =====

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public Long getTotalCredits() { return totalCredits; }
    public void setTotalCredits(Long totalCredits) { this.totalCredits = totalCredits; }

    public Long getAvailableCredits() { return availableCredits; }
    public void setAvailableCredits(Long availableCredits) { this.availableCredits = availableCredits; }

    public List<OAuthBinding> getOauthBindings() { return oauthBindings; }
    public void setOauthBindings(List<OAuthBinding> oauthBindings) { this.oauthBindings = oauthBindings; }

    public List<WalletBinding> getWalletBindings() { return walletBindings; }
    public void setWalletBindings(List<WalletBinding> walletBindings) { this.walletBindings = walletBindings; }

    // ===== Inner Classes =====

    public static class OAuthBinding
    {
        private String provider;
        private String providerName;
        private String providerAvatar;
        private boolean bound;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public String getProviderAvatar() { return providerAvatar; }
        public void setProviderAvatar(String providerAvatar) { this.providerAvatar = providerAvatar; }
        public boolean isBound() { return bound; }
        public void setBound(boolean bound) { this.bound = bound; }
    }

    public static class WalletBinding
    {
        private String walletAddress;
        private String walletType;
        private Integer chainId;
        private boolean bound;

        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
        public String getWalletType() { return walletType; }
        public void setWalletType(String walletType) { this.walletType = walletType; }
        public Integer getChainId() { return chainId; }
        public void setChainId(Integer chainId) { this.chainId = chainId; }
        public boolean isBound() { return bound; }
        public void setBound(boolean bound) { this.bound = bound; }
    }
}
