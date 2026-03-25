package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserOAuth;
import com.ruoyi.system.domain.VirtualUserWallet;

/**
 * Virtual platform user service
 */
public interface IVirtualUserService {
    /** Find user by ID */
    public VirtualUser selectVirtualUserById(Long userId);

    /** Find user by username */
    public VirtualUser selectVirtualUserByUsername(String username);

    /** Find user by email */
    public VirtualUser selectVirtualUserByEmail(String email);

    /** List users with filters */
    public List<VirtualUser> selectVirtualUserList(VirtualUser virtualUser);

    /** Register via email */
    public VirtualUser registerByEmail(String email, String password, String nickname);

    /** Register via OAuth (creates user if not exists) */
    public VirtualUser registerOrLoginByOAuth(String provider, String providerUserId, String providerEmail,
            String providerName, String providerAvatar);

    /** Register via wallet (creates user if not exists) */
    public VirtualUser registerOrLoginByWallet(String walletAddress, String walletType, Integer chainId);

    /** Update user profile */
    public int updateVirtualUser(VirtualUser virtualUser);

    /** Bind email to existing user */
    public int bindEmail(Long userId, String email);

    /** Verify email */
    public int verifyEmail(Long userId);

    /** Record successful login */
    public void recordLoginSuccess(Long userId, String ip, String device);

    /** Record failed login attempt */
    public void recordLoginFailure(Long userId);

    /** Check if user account is locked */
    public boolean isAccountLocked(Long userId);

    /** Get OAuth bindings for user */
    public List<VirtualUserOAuth> selectOAuthByUserId(Long userId);

    /** Get wallet bindings for user */
    public List<VirtualUserWallet> selectWalletsByUserId(Long userId);

    /** Bind wallet to user */
    public int bindWallet(Long userId, String walletAddress, String walletType, Integer chainId, String signature);

    /** Find user by Telegram ID */
    public VirtualUser selectVirtualUserByTelegramId(String telegramId);
}
