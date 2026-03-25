package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualUserWallet;

/**
 * 用户钱包绑定Mapper接口
 */
public interface VirtualUserWalletMapper {
    public VirtualUserWallet selectById(Long id);

    public VirtualUserWallet selectByWalletAddress(String walletAddress);

    public List<VirtualUserWallet> selectByUserId(Long userId);

    public int insertVirtualUserWallet(VirtualUserWallet wallet);

    public int updateVirtualUserWallet(VirtualUserWallet wallet);

    public int deleteById(Long id);
}
