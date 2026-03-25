package com.ruoyi.virtual.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 虚拟宠物平台配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "pet.virtual")
public class VirtualConfig {
    /** OAuth2 配置 */
    private OAuth2Config oauth2 = new OAuth2Config();

    /** 邮件验证码配置 */
    private EmailConfig email = new EmailConfig();

    /** 积分配置 */
    private CreditsConfig credits = new CreditsConfig();

    /** 充值配置 */
    private RechargeConfig recharge = new RechargeConfig();

    /** Telegram Bot 配置 */
    private TelegramConfig telegram = new TelegramConfig();

    /** 分享功能配置 */
    private ShareConfig share = new ShareConfig();

    /** 开屏动画文件ID */
    private Long splashFileId;

    // ===== Getters and Setters =====

    public OAuth2Config getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2Config oauth2) {
        this.oauth2 = oauth2;
    }

    public EmailConfig getEmail() {
        return email;
    }

    public void setEmail(EmailConfig email) {
        this.email = email;
    }

    public CreditsConfig getCredits() {
        return credits;
    }

    public void setCredits(CreditsConfig credits) {
        this.credits = credits;
    }

    public RechargeConfig getRecharge() {
        return recharge;
    }

    public void setRecharge(RechargeConfig recharge) {
        this.recharge = recharge;
    }

    public TelegramConfig getTelegram() {
        return telegram;
    }

    public void setTelegram(TelegramConfig telegram) {
        this.telegram = telegram;
    }

    public ShareConfig getShare() {
        return share;
    }

    public void setShare(ShareConfig share) {
        this.share = share;
    }

    public Long getSplashFileId() {
        return splashFileId;
    }

    public void setSplashFileId(Long splashFileId) {
        this.splashFileId = splashFileId;
    }

    // ===== Inner Classes =====

    public static class OAuth2Config {
        private ProviderConfig google = new ProviderConfig();
        private ProviderConfig microsoft = new ProviderConfig();
        private ProviderConfig twitter = new ProviderConfig();

        public ProviderConfig getGoogle() {
            return google;
        }

        public void setGoogle(ProviderConfig google) {
            this.google = google;
        }

        public ProviderConfig getMicrosoft() {
            return microsoft;
        }

        public void setMicrosoft(ProviderConfig microsoft) {
            this.microsoft = microsoft;
        }

        public ProviderConfig getTwitter() {
            return twitter;
        }

        public void setTwitter(ProviderConfig twitter) {
            this.twitter = twitter;
        }
    }

    public static class ProviderConfig {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }

    public static class EmailConfig {
        /** 验证码过期时间（分钟） */
        private int codeExpireMinutes = 5;
        /** 验证码长度 */
        private int codeLength = 6;
        /** 发送间隔（秒） */
        private int sendIntervalSeconds = 60;

        public int getCodeExpireMinutes() {
            return codeExpireMinutes;
        }

        public void setCodeExpireMinutes(int codeExpireMinutes) {
            this.codeExpireMinutes = codeExpireMinutes;
        }

        public int getCodeLength() {
            return codeLength;
        }

        public void setCodeLength(int codeLength) {
            this.codeLength = codeLength;
        }

        public int getSendIntervalSeconds() {
            return sendIntervalSeconds;
        }

        public void setSendIntervalSeconds(int sendIntervalSeconds) {
            this.sendIntervalSeconds = sendIntervalSeconds;
        }
    }

    public static class CreditsConfig {
        /** 新用户注册赠送积分 */
        private long initialCredits = 30;
        /** 每日登录奖励积分 */
        private long dailyLoginCredits = 10;

        public long getInitialCredits() {
            return initialCredits;
        }

        public void setInitialCredits(long initialCredits) {
            this.initialCredits = initialCredits;
        }

        public long getDailyLoginCredits() {
            return dailyLoginCredits;
        }

        public void setDailyLoginCredits(long dailyLoginCredits) {
            this.dailyLoginCredits = dailyLoginCredits;
        }
    }

    public static class RechargeConfig {
        /** 订单过期时间（分钟） */
        private int orderExpireMinutes = 30;

        public int getOrderExpireMinutes() {
            return orderExpireMinutes;
        }

        public void setOrderExpireMinutes(int orderExpireMinutes) {
            this.orderExpireMinutes = orderExpireMinutes;
        }
    }

    public static class ShareConfig {
        /** 分享链接的前端基础URL */
        private String baseUrl = "http://localhost:3000";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class TelegramConfig {
        private String botToken = "";
        private String botUsername = "";
        private String webhookUrl = "";
        private boolean useWebhook = false;
        private Boolean enabled = false;

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getBotUsername() {
            return botUsername;
        }

        public void setBotUsername(String botUsername) {
            this.botUsername = botUsername;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public boolean isUseWebhook() {
            return useWebhook;
        }

        public void setUseWebhook(boolean useWebhook) {
            this.useWebhook = useWebhook;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}
