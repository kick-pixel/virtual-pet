package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualUserOAuth;
import org.apache.ibatis.annotations.Param;

/**
 * 用户OAuth绑定Mapper接口
 */
public interface VirtualUserOAuthMapper {
    public VirtualUserOAuth selectById(Long id);

    public VirtualUserOAuth selectByProviderAndUserId(@Param("provider") String provider,
            @Param("providerUserId") String providerUserId);

    public List<VirtualUserOAuth> selectByUserId(Long userId);

    public int insertVirtualUserOAuth(VirtualUserOAuth oAuth);

    public int updateVirtualUserOAuth(VirtualUserOAuth oAuth);

    public int deleteById(Long id);

    public int deleteByUserId(Long userId);
}
