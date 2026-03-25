package com.ruoyi.virtual.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.utils.ip.IpUtils;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IFileService;
import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserWallet;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.service.IEmailService;
import com.ruoyi.system.service.IVirtualUserService;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.virtual.config.VirtualConfig;
import com.ruoyi.virtual.dto.response.UserInfoResponse;
import com.ruoyi.virtual.oauth.OAuth2UserInfo;
import com.ruoyi.virtual.oauth.provider.GoogleOAuth2Provider;
import com.ruoyi.virtual.security.VirtualLoginUser;
import com.ruoyi.virtual.security.VirtualSecurityUtils;
import com.ruoyi.virtual.security.VirtualTokenService;
import com.ruoyi.virtual.security.WalletSignatureVerifier;

@RestController
@RequestMapping("/api/virtual/auth")
public class VirtualAuthController
{
    @Autowired
    private IVirtualUserService virtualUserService;

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Autowired
    private VirtualTokenService virtualTokenService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private WalletSignatureVerifier walletSignatureVerifier;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private GoogleOAuth2Provider googleOAuth2Provider;

    @Autowired
    private VirtualConfig virtualConfig;

    // ===== 邮箱验证码登录 =====

    @PostMapping("/email/send-code")
    public AjaxResult sendEmailCode(@RequestBody Map<String, String> body)
    {
        String email = body.get("email");
        if (email == null || !email.contains("@"))
        {
            return AjaxResult.error("请输入有效的邮箱地址");
        }
        // 发送频率限制
        String intervalKey = "email:code:interval:" + email.toLowerCase();
        if (redisCache.getCacheObject(intervalKey) != null)
        {
            int interval = virtualConfig.getEmail().getSendIntervalSeconds();
            return AjaxResult.error("发送过于频繁，请 " + interval + " 秒后重试");
        }
        // 生成6位随机验证码
        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1000000));
        // Redis 存储验证码
        int expireMinutes = virtualConfig.getEmail().getCodeExpireMinutes();
        redisCache.setCacheObject("email:code:" + email.toLowerCase(), code, expireMinutes, TimeUnit.MINUTES);
        // 设置发送间隔
        int intervalSeconds = virtualConfig.getEmail().getSendIntervalSeconds();
        redisCache.setCacheObject(intervalKey, "1", intervalSeconds, TimeUnit.SECONDS);
        // 发送邮件
        emailService.sendVerificationCode(email, code, "en", null);
        return AjaxResult.success("验证码已发送");
    }

    @PostMapping("/email/login")
    public AjaxResult emailLogin(@RequestBody Map<String, String> body, HttpServletRequest request)
    {
        String email = body.get("email");
        String code  = body.get("code");
        if (email == null || code == null)
        {
            return AjaxResult.error("邮箱或验证码不能为空");
        }
        // 从 Redis 取验证码
        String storedCode = redisCache.getCacheObject("email:code:" + email.toLowerCase());
        if (storedCode == null)
        {
            return AjaxResult.error("验证码已过期，请重新发送");
        }
        if (!storedCode.equals(code.trim()))
        {
            return AjaxResult.error("验证码不正确");
        }
        // 防重放：验证后立即删除
        redisCache.deleteObject("email:code:" + email.toLowerCase());
        // 查找或自动注册用户
        VirtualUser user = virtualUserService.selectVirtualUserByEmail(email);
        if (user == null)
        {
            user = virtualUserService.registerByEmail(email, null, null);
            virtualCreditsService.initCredits(user.getUserId());
        }
        virtualUserService.recordLoginSuccess(user.getUserId(), IpUtils.getIpAddr(request), null);
        return AjaxResult.success(createLoginResult(user));
    }

    // ===== Google OAuth 登录 =====

    @GetMapping("/oauth/{provider}/url")
    public AjaxResult getOAuthUrl(@PathVariable String provider,
                                   @RequestParam(required = false) String redirectUri)
    {
        if (!"google".equals(provider))
        {
            return AjaxResult.error("暂不支持该 OAuth 提供商: " + provider);
        }
        String state = UUID.randomUUID().toString();
        redisCache.setCacheObject("oauth:state:" + state, state, 10, TimeUnit.MINUTES);
        String authUrl = googleOAuth2Provider.getAuthorizationUrl(state);
        Map<String, Object> data = new HashMap<>();
        data.put("url", authUrl);
        return AjaxResult.success(data);
    }

    @PostMapping("/oauth/{provider}/callback")
    public AjaxResult oauthCallback(@PathVariable String provider,
                                     @RequestBody Map<String, String> body,
                                     HttpServletRequest request)
    {
        String code  = body.get("code");
        String state = body.get("state");
        // 验证 state 防 CSRF
        String storedState = redisCache.getCacheObject("oauth:state:" + state);
        if (storedState == null)
        {
            return AjaxResult.error("OAuth state 无效或已过期");
        }
        redisCache.deleteObject("oauth:state:" + state);
        if (!"google".equals(provider))
        {
            return AjaxResult.error("暂不支持该 OAuth 提供商: " + provider);
        }
        try
        {
            OAuth2UserInfo userInfo = googleOAuth2Provider.handleCallback(code);
            VirtualUser user = virtualUserService.registerOrLoginByOAuth(
                    provider, userInfo.getProviderId(), userInfo.getEmail(),
                    userInfo.getName(), userInfo.getAvatar());
            virtualCreditsService.initCredits(user.getUserId());
            virtualUserService.recordLoginSuccess(user.getUserId(), IpUtils.getIpAddr(request), null);
            return AjaxResult.success(createLoginResult(user));
        }
        catch (IOException e)
        {
            return AjaxResult.error("Google 登录失败: " + e.getMessage());
        }
    }

    // ===== 用户资料更新 =====

    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody Map<String, String> body)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        VirtualUser user = new VirtualUser();
        user.setUserId(userId);
        if (body.containsKey("nickname") && body.get("nickname") != null)
        {
            user.setNickname(body.get("nickname"));
        }
        virtualUserService.updateVirtualUser(user);
        return AjaxResult.success();
    }

    @PostMapping("/avatar")
    public AjaxResult uploadAvatar(@RequestParam("file") MultipartFile file)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        FileUploadResult result = fileService.upload(file, "avatar");
        String avatarUrl = result.getFileUrl();
        VirtualUser user = new VirtualUser();
        user.setUserId(userId);
        user.setAvatarUrl(avatarUrl);
        virtualUserService.updateVirtualUser(user);
        Map<String, Object> data = new HashMap<>();
        data.put("avatarUrl", avatarUrl);
        return AjaxResult.success(data);
    }

    @GetMapping("/wallet/nonce")
    public AjaxResult getWalletNonce(@RequestParam String address)
    {
        String nonce = UUID.randomUUID().toString();
        redisCache.setCacheObject("virtual_wallet_nonce:" + address.toLowerCase(), nonce, 5, TimeUnit.MINUTES);

        Map<String, Object> data = new HashMap<>();
        data.put("nonce", nonce);
        data.put("message", walletSignatureVerifier.buildLoginMessage(nonce));
        return AjaxResult.success(data);
    }

    @PostMapping("/wallet/login")
    public AjaxResult walletLogin(@RequestParam String address, @RequestParam String signature,
            @RequestParam(defaultValue = "metamask") String walletType,
            @RequestParam(defaultValue = "1") Integer chainId,
            HttpServletRequest request)
    {
        String nonce = redisCache.getCacheObject("virtual_wallet_nonce:" + address.toLowerCase());
        if (nonce == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.auth.nonce.expired"));
        }

        String message = walletSignatureVerifier.buildLoginMessage(nonce);
        if (!walletSignatureVerifier.verifySignature(message, signature, address))
        {
            return AjaxResult.error(MessageUtils.message("virtual.auth.signature.invalid"));
        }

        redisCache.deleteObject("virtual_wallet_nonce:" + address.toLowerCase());

        VirtualUser user = virtualUserService.registerOrLoginByWallet(address, walletType, chainId);
        virtualCreditsService.initCredits(user.getUserId());
        virtualUserService.recordLoginSuccess(user.getUserId(), IpUtils.getIpAddr(request), null);

        return AjaxResult.success(createLoginResult(user));
    }

    @PostMapping("/bind/wallet")
    public AjaxResult bindWallet(@RequestParam String address, @RequestParam String signature,
            @RequestParam(defaultValue = "metamask") String walletType,
            @RequestParam(defaultValue = "1") Integer chainId)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();

        String nonce = redisCache.getCacheObject("virtual_wallet_nonce:" + address.toLowerCase());
        if (nonce == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.auth.nonce.expired.short"));
        }

        String message = walletSignatureVerifier.buildLoginMessage(nonce);
        if (!walletSignatureVerifier.verifySignature(message, signature, address))
        {
            return AjaxResult.error(MessageUtils.message("virtual.auth.signature.invalid"));
        }

        redisCache.deleteObject("virtual_wallet_nonce:" + address.toLowerCase());
        virtualUserService.bindWallet(userId, address, walletType, chainId, signature);
        return AjaxResult.success(MessageUtils.message("virtual.auth.wallet.bind.success"));
    }

    @GetMapping("/profile")
    public AjaxResult getProfile()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        VirtualUser user = virtualUserService.selectVirtualUserById(userId);
        if (user == null)
        {
            return AjaxResult.error(MessageUtils.message("virtual.auth.user.not.found"));
        }

        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1);
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());

        List<VirtualUserWallet> wallets = virtualUserService.selectWalletsByUserId(userId);
        if (wallets != null)
        {
            response.setWalletBindings(wallets.stream().map(wallet -> {
                UserInfoResponse.WalletBinding binding = new UserInfoResponse.WalletBinding();
                binding.setWalletAddress(wallet.getWalletAddress());
                binding.setWalletType(wallet.getWalletType());
                binding.setChainId(wallet.getChainId());
                binding.setBound(true);
                return binding;
            }).collect(Collectors.toList()));
        }

        VirtualUserCredits credits = virtualCreditsService.getBalance(userId);
        if (credits != null)
        {
            response.setTotalCredits(credits.getBalance() + credits.getFrozen());
            response.setAvailableCredits(credits.getBalance());
        }

        return AjaxResult.success(response);
    }

    @PostMapping("/refresh")
    public AjaxResult refreshToken()
    {
        VirtualLoginUser loginUser = VirtualSecurityUtils.getCurrentUser();
        virtualTokenService.refreshToken(loginUser);
        return AjaxResult.success(MessageUtils.message("virtual.auth.token.refreshed"));
    }

    @PostMapping("/logout")
    public AjaxResult logout()
    {
        VirtualLoginUser loginUser = VirtualSecurityUtils.getCurrentUser();
        if (loginUser != null)
        {
            virtualTokenService.delLoginUser(loginUser.getToken());
        }
        return AjaxResult.success(MessageUtils.message("virtual.auth.logout.success"));
    }

    private Map<String, Object> createLoginResult(VirtualUser user)
    {
        VirtualLoginUser loginUser = new VirtualLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setEmail(user.getEmail());
        loginUser.setEmailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1);

        String token = virtualTokenService.createToken(loginUser);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("emailVerified", user.getEmailVerified() != null && user.getEmailVerified() == 1);
        result.put("avatarUrl", user.getAvatarUrl());
        return result;
    }
}
