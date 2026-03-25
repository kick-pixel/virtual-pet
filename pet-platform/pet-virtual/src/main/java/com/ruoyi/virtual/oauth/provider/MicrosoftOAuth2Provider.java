package com.ruoyi.virtual.oauth.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.virtual.config.VirtualConfig;
import com.ruoyi.virtual.oauth.OAuth2UserInfo;
import okhttp3.*;

/**
 * Microsoft OAuth2 提供商（使用 Microsoft Graph API）
 *
 * @author ruoyi
 */
@Component
public class MicrosoftOAuth2Provider
{
    private static final Logger log = LoggerFactory.getLogger(MicrosoftOAuth2Provider.class);
    private static final String SCOPE = "openid email profile User.Read";

    @Autowired
    private VirtualConfig virtualConfig;

    @Autowired
    @Qualifier("virtualOkHttpClient")
    private OkHttpClient okHttpClient;

    /**
     * 生成 Microsoft 授权 URL
     */
    public String getAuthorizationUrl(String state)
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getMicrosoft();
        return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                + "?client_id=" + URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&response_mode=query";
    }

    /**
     * 用授权码交换用户信息
     */
    public OAuth2UserInfo handleCallback(String code) throws IOException
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getMicrosoft();

        // 1. 交换 Access Token
        RequestBody tokenBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("client_id", config.getClientId())
                .add("client_secret", config.getClientSecret())
                .add("redirect_uri", config.getRedirectUri())
                .add("scope", SCOPE)
                .build();

        Request tokenRequest = new Request.Builder()
                .url("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .post(tokenBody)
                .build();

        JSONObject tokenResp;
        try (Response response = okHttpClient.newCall(tokenRequest).execute())
        {
            tokenResp = JSON.parseObject(response.body().string());
        }

        String accessToken = tokenResp.getString("access_token");
        String refreshToken = tokenResp.getString("refresh_token");

        // 2. 获取用户信息（Microsoft Graph）
        Request userRequest = new Request.Builder()
                .url("https://graph.microsoft.com/v1.0/me")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        JSONObject userResp;
        try (Response response = okHttpClient.newCall(userRequest).execute())
        {
            userResp = JSON.parseObject(response.body().string());
        }

        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setProvider("microsoft");
        userInfo.setProviderId(userResp.getString("id"));
        userInfo.setEmail(userResp.getString("mail") != null
                ? userResp.getString("mail") : userResp.getString("userPrincipalName"));
        userInfo.setName(userResp.getString("displayName"));
        userInfo.setAccessToken(accessToken);
        userInfo.setRefreshToken(refreshToken);

        log.info("Microsoft OAuth 用户信息获取成功: providerId={}, email={}", userInfo.getProviderId(), userInfo.getEmail());
        return userInfo;
    }
}
