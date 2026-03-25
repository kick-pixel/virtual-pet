package com.ruoyi.virtual.oauth.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
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
 * Google OAuth2 提供商
 *
 * @author ruoyi
 */
@Component
public class GoogleOAuth2Provider
{
    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2Provider.class);
    private static final String SCOPE = "openid email profile";

    @Autowired
    private VirtualConfig virtualConfig;

    @Autowired
    @Qualifier("virtualOkHttpClient")
    private OkHttpClient okHttpClient;

    /**
     * 生成 Google 授权 URL
     */
    public String getAuthorizationUrl(String state)
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getGoogle();
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";
    }

    /**
     * 用授权码交换用户信息
     */
    public OAuth2UserInfo handleCallback(String code) throws IOException
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getGoogle();

        // 1. 交换 Access Token
        RequestBody tokenBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("client_id", config.getClientId())
                .add("client_secret", config.getClientSecret())
                .add("redirect_uri", config.getRedirectUri())
                .build();

        Request tokenRequest = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(tokenBody)
                .build();

        JSONObject tokenResp;
        try (Response response = okHttpClient.newCall(tokenRequest).execute())
        {
            tokenResp = JSON.parseObject(response.body().string());
        }

        String accessToken = tokenResp.getString("access_token");
        String refreshToken = tokenResp.getString("refresh_token");

        // 2. 获取用户信息
        Request userRequest = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/userinfo")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        JSONObject userResp;
        try (Response response = okHttpClient.newCall(userRequest).execute())
        {
            userResp = JSON.parseObject(response.body().string());
        }

        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setProvider("google");
        userInfo.setProviderId(userResp.getString("sub"));
        userInfo.setEmail(userResp.getString("email"));
        userInfo.setName(userResp.getString("name"));
        userInfo.setAvatar(userResp.getString("picture"));
        userInfo.setAccessToken(accessToken);
        userInfo.setRefreshToken(refreshToken);

        log.info("Google OAuth 用户信息获取成功: providerId={}, email={}", userInfo.getProviderId(), userInfo.getEmail());
        return userInfo;
    }
}
