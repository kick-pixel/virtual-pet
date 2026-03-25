package com.ruoyi.virtual.oauth;

/**
 * OAuth2 提供商枚举
 */
public enum OAuth2Provider
{
    GOOGLE("google", "Google", "https://accounts.google.com/o/oauth2/v2/auth",
           "https://oauth2.googleapis.com/token", "https://www.googleapis.com/oauth2/v3/userinfo"),

    MICROSOFT("microsoft", "Microsoft", "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
              "https://login.microsoftonline.com/common/oauth2/v2.0/token", "https://graph.microsoft.com/v1.0/me"),

    TWITTER("twitter", "Twitter/X", "https://twitter.com/i/oauth2/authorize",
            "https://api.twitter.com/2/oauth2/token", "https://api.twitter.com/2/users/me");

    private final String code;
    private final String displayName;
    private final String authorizeUrl;
    private final String tokenUrl;
    private final String userInfoUrl;

    OAuth2Provider(String code, String displayName, String authorizeUrl, String tokenUrl, String userInfoUrl)
    {
        this.code = code;
        this.displayName = displayName;
        this.authorizeUrl = authorizeUrl;
        this.tokenUrl = tokenUrl;
        this.userInfoUrl = userInfoUrl;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getAuthorizeUrl() { return authorizeUrl; }
    public String getTokenUrl() { return tokenUrl; }
    public String getUserInfoUrl() { return userInfoUrl; }

    /**
     * 根据提供商代码查找枚举
     */
    public static OAuth2Provider fromCode(String code)
    {
        for (OAuth2Provider provider : values())
        {
            if (provider.code.equalsIgnoreCase(code))
            {
                return provider;
            }
        }
        throw new IllegalArgumentException("不支持的 OAuth 提供商: " + code);
    }
}
