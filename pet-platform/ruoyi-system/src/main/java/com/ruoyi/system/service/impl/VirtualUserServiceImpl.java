package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ip.IpRegionService;
import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserOAuth;
import com.ruoyi.system.domain.VirtualUserWallet;
import com.ruoyi.system.mapper.VirtualUserMapper;
import com.ruoyi.system.mapper.VirtualUserOAuthMapper;
import com.ruoyi.system.mapper.VirtualUserWalletMapper;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.system.service.IVirtualUserService;

/**
 * Virtual platform user service implementation
 */
@Service
public class VirtualUserServiceImpl implements IVirtualUserService {
    private static final Logger log = LoggerFactory.getLogger(VirtualUserServiceImpl.class);

    /** Max failed login attempts before locking */
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    /** Lock duration in minutes */
    private static final int LOCK_MINUTES = 30;

    @Autowired
    private VirtualUserMapper userMapper;

    @Autowired
    private VirtualUserOAuthMapper oauthMapper;

    @Autowired
    private VirtualUserWalletMapper walletMapper;

    @Autowired
    private IVirtualCreditsService creditsService;

    @Autowired(required = false)
    private IpRegionService ipRegionService;

    @Override
    public VirtualUser selectVirtualUserById(Long userId) {
        return userMapper.selectVirtualUserById(userId);
    }

    @Override
    public VirtualUser selectVirtualUserByUsername(String username) {
        return userMapper.selectVirtualUserByUsername(username);
    }

    @Override
    public VirtualUser selectVirtualUserByEmail(String email) {
        return userMapper.selectVirtualUserByEmail(email);
    }

    @Override
    public List<VirtualUser> selectVirtualUserList(VirtualUser virtualUser) {
        return userMapper.selectVirtualUserList(virtualUser);
    }

    @Override
    @Transactional
    public VirtualUser registerByEmail(String email, String password, String nickname) {
        // Check duplicate email
        VirtualUser existing = userMapper.selectVirtualUserByEmail(email);
        if (existing != null) {
            throw new ServiceException("Email already registered");
        }

        VirtualUser user = new VirtualUser();
        user.setEmail(email);
        user.setPasswordHash(password); // Should be hashed before calling this method
        user.setNickname(nickname);
        user.setUsername(email); // Default username to email
        user.setStatus(1);
        user.setEmailVerified(0);
        userMapper.insertVirtualUser(user);

        // Initialize credits
        creditsService.initCredits(user.getUserId());

        log.info("User registered via email: {}", email);
        return user;
    }

    @Override
    @Transactional
    public VirtualUser registerOrLoginByOAuth(String provider, String providerUserId, String providerEmail,
            String providerName, String providerAvatar) {
        // Check if OAuth binding exists
        VirtualUserOAuth existing = oauthMapper.selectByProviderAndUserId(provider, providerUserId);
        if (existing != null) {
            // Update token info
            existing.setProviderEmail(providerEmail);
            existing.setProviderName(providerName);
            existing.setProviderAvatar(providerAvatar);
            oauthMapper.updateVirtualUserOAuth(existing);
            return userMapper.selectVirtualUserById(existing.getUserId());
        }

        // Check if user exists by email
        VirtualUser user = null;
        if (providerEmail != null && !providerEmail.isEmpty()) {
            user = userMapper.selectVirtualUserByEmail(providerEmail);
        }

        // Create new user if not found
        if (user == null) {
            user = new VirtualUser();
            user.setEmail(providerEmail);
            user.setNickname(providerName);
            user.setAvatarUrl(providerAvatar);
            user.setUsername(provider + "_" + providerUserId);
            user.setStatus(1);
            user.setEmailVerified(0);
            userMapper.insertVirtualUser(user);

            // Initialize credits
            creditsService.initCredits(user.getUserId());
        }

        // Create OAuth binding
        VirtualUserOAuth oauth = new VirtualUserOAuth();
        oauth.setUserId(user.getUserId());
        oauth.setProvider(provider);
        oauth.setProviderUserId(providerUserId);
        oauth.setProviderEmail(providerEmail);
        oauth.setProviderName(providerName);
        oauth.setProviderAvatar(providerAvatar);
        oauthMapper.insertVirtualUserOAuth(oauth);

        log.info("User registered/logged in via OAuth: {} - {}", provider, providerUserId);
        return user;
    }

    @Override
    @Transactional
    public VirtualUser registerOrLoginByWallet(String walletAddress, String walletType, Integer chainId) {
        // Check if wallet is already bound
        VirtualUserWallet existingWallet = walletMapper.selectByWalletAddress(walletAddress);
        if (existingWallet != null) {
            return userMapper.selectVirtualUserById(existingWallet.getUserId());
        }

        // Create new user
        VirtualUser user = new VirtualUser();
        user.setUsername("wallet_" + walletAddress.substring(0, Math.min(10, walletAddress.length())));
        user.setNickname(walletAddress.substring(0, 6) + "..." + walletAddress.substring(walletAddress.length() - 4));
        user.setStatus(1);
        user.setEmailVerified(0);
        userMapper.insertVirtualUser(user);

        // Bind wallet
        VirtualUserWallet wallet = new VirtualUserWallet();
        wallet.setUserId(user.getUserId());
        wallet.setWalletAddress(walletAddress);
        wallet.setWalletType(walletType);
        wallet.setChainId(chainId);
        wallet.setIsPrimary(1);
        walletMapper.insertVirtualUserWallet(wallet);

        // Initialize credits
        creditsService.initCredits(user.getUserId());

        log.info("User registered via wallet: {}", walletAddress);
        return user;
    }

    @Override
    public int updateVirtualUser(VirtualUser virtualUser) {
        return userMapper.updateVirtualUser(virtualUser);
    }

    @Override
    @Transactional
    public int bindEmail(Long userId, String email) {
        // Check if email is already taken
        VirtualUser existing = userMapper.selectVirtualUserByEmail(email);
        if (existing != null && !existing.getUserId().equals(userId)) {
            throw new ServiceException("Email already bound to another account");
        }

        VirtualUser user = new VirtualUser();
        user.setUserId(userId);
        user.setEmail(email);
        user.setEmailVerified(0);
        return userMapper.updateVirtualUser(user);
    }

    @Override
    public int verifyEmail(Long userId) {
        VirtualUser user = new VirtualUser();
        user.setUserId(userId);
        user.setEmailVerified(1);
        return userMapper.updateVirtualUser(user);
    }

    @Override
    public void recordLoginSuccess(Long userId, String ip, String device) {
        VirtualUser user = new VirtualUser();
        user.setUserId(userId);
        user.setLastLoginIp(ip);
        user.setLastLoginDevice(device);
        userMapper.updateLoginSuccess(user);
        // 首次登录时写入注册IP（XML已加 AND register_ip IS NULL，天然幂等）
        if (ip != null && !ip.isBlank()) {
            userMapper.updateRegisterIp(userId, ip);
            // 同时写入 IP 归属地（AND country_region IS NULL 保证幂等，只记录首次）
            if (ipRegionService != null) {
                try {
                    String region = ipRegionService.getRegion(ip);
                    if (region != null) {
                        userMapper.updateCountryRegion(userId, region);
                    }
                } catch (Exception e) {
                    log.warn("IP 归属地写入失败 userId={}: {}", userId, e.getMessage());
                }
            }
        }
    }

    @Override
    public void recordLoginFailure(Long userId) {
        VirtualUser user = userMapper.selectVirtualUserById(userId);
        if (user == null) {
            return;
        }
        int attempts = (user.getLoginAttempts() != null ? user.getLoginAttempts() : 0) + 1;
        VirtualUser update = new VirtualUser();
        update.setUserId(userId);
        update.setLoginAttempts(attempts);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            update.setLockedUntil(new Date(System.currentTimeMillis() + LOCK_MINUTES * 60 * 1000L));
        }
        userMapper.updateLoginAttempts(update);
    }

    @Override
    public boolean isAccountLocked(Long userId) {
        VirtualUser user = userMapper.selectVirtualUserById(userId);
        if (user == null || user.getLockedUntil() == null) {
            return false;
        }
        return user.getLockedUntil().after(new Date());
    }

    @Override
    public List<VirtualUserOAuth> selectOAuthByUserId(Long userId) {
        return oauthMapper.selectByUserId(userId);
    }

    @Override
    public List<VirtualUserWallet> selectWalletsByUserId(Long userId) {
        return walletMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public int bindWallet(Long userId, String walletAddress, String walletType, Integer chainId, String signature) {
        // ... implementation ...
        // (Note: sticking to minimal changes to avoid breaking existing logic in
        // prompt, but here I should just append the new method)
        // Check if wallet is already bound
        VirtualUserWallet existing = walletMapper.selectByWalletAddress(walletAddress);
        if (existing != null) {
            throw new ServiceException("Wallet address already bound to another account");
        }

        VirtualUserWallet wallet = new VirtualUserWallet();
        wallet.setUserId(userId);
        wallet.setWalletAddress(walletAddress);
        wallet.setWalletType(walletType);
        wallet.setChainId(chainId);
        wallet.setSignature(signature);
        wallet.setVerifiedAt(new Date());

        // If user has no primary wallet, set this as primary
        List<VirtualUserWallet> wallets = walletMapper.selectByUserId(userId);
        wallet.setIsPrimary(wallets.isEmpty() ? 1 : 0);

        return walletMapper.insertVirtualUserWallet(wallet);
    }

    @Override
    public VirtualUser selectVirtualUserByTelegramId(String telegramId) {
        return userMapper.selectVirtualUserByTelegramId(telegramId);
    }
}
