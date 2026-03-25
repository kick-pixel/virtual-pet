package com.ruoyi.virtual.oauth.provider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
 * Twitter/X OAuth2 提供商（使用 OAuth 2.0 with PKCE）
 *
 * @author ruoyi
 */
@Component
public class TwitterOAuth2Provider
{
    private static final Logger log = LoggerFactory.getLogger(TwitterOAuth2Provider.class);
    private static final String SCOPE = "tweet.read users.read offline.access";

    @Autowired
    private VirtualConfig virtualConfig;

    @Autowired
    @Qualifier("virtualOkHttpClient")
    private OkHttpClient okHttpClient;

    /**
     * 生成 Twitter 授权 URL
     */
    public String getAuthorizationUrl(String state, String codeChallenge)
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getTwitter();
        return "https://twitter.com/i/oauth2/authorize"
                + "?client_id=" + URLEncoder.encode(config.getClientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&code_challenge=" + URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8)
                + "&code_challenge_method=S256";
    }

    /**
     * 用授权码交换用户信息
     */
    public OAuth2UserInfo handleCallback(String code, String codeVerifier) throws IOException
    {
        VirtualConfig.ProviderConfig config = virtualConfig.getOauth2().getTwitter();

        // 1. 交换 Access Token（Basic Auth = base64(client_id:client_secret)）
        String credentials = Base64.getEncoder().encodeToString(
                (config.getClientId() + ":" + config.getClientSecret()).getBytes(StandardCharsets.UTF_8));

        RequestBody tokenBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", config.getRedirectUri())
                .add("code_verifier", codeVerifier)
                .build();

        Request tokenRequest = new Request.Builder()
                .url("https://api.twitter.com/2/oauth2/token")
                .addHeader("Authorization", "Basic " + credentials)
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
                .url("https://api.twitter.com/2/users/me?user.fields=id,name,username,profile_image_url")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        JSONObject userResp;
        try (Response response = okHttpClient.newCall(userRequest).execute())
        {
            userResp = JSON.parseObject(response.body().string());
        }

        JSONObject data = userResp.getJSONObject("data");

        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setProvider("twitter");
        userInfo.setProviderId(data.getString("id"));
        userInfo.setName(data.getString("name"));
        userInfo.setAvatar(data.getString("profile_image_url"));
        // Twitter 不返回邮箱，用户需后续绑定
        userInfo.setAccessToken(accessToken);
        userInfo.setRefreshToken(refreshToken);

        log.info("Twitter OAuth 用户信息获取成功: providerId={}, name={}", userInfo.getProviderId(), userInfo.getName());
        return userInfo;
    }
}
