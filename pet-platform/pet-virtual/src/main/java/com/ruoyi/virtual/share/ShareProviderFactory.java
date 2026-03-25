package com.ruoyi.virtual.share;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分享提供商工厂
 *
 * @author ruoyi
 */
@Component
public class ShareProviderFactory
{
    private final Map<String, ShareProvider> providerMap = new HashMap<>();

    @Autowired
    public ShareProviderFactory(List<ShareProvider> providers)
    {
        for (ShareProvider provider : providers)
        {
            providerMap.put(provider.getPlatformCode(), provider);
        }
    }

    /**
     * 根据平台代码获取分享提供商
     */
    public ShareProvider getProvider(String platformCode)
    {
        ShareProvider provider = providerMap.get(platformCode.toLowerCase());
        if (provider == null)
        {
            throw new IllegalArgumentException("不支持的分享平台: " + platformCode);
        }
        return provider;
    }
}
