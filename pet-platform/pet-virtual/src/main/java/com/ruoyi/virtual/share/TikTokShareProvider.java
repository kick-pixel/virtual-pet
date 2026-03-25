package com.ruoyi.virtual.share;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.virtual.config.VirtualConfig;

/**
 * TikTok 分享提供商（暂未启用）
 *
 * @author ruoyi
 */
@Component
public class TikTokShareProvider implements ShareProvider
{
    private static final String SHARE_BASE_URL = "https://www.tiktok.com/share";

    @Autowired
    private VirtualConfig virtualConfig;

    @Override
    public String generateShareUrl(Long taskId, Long userId)
    {
        String baseUrl = virtualConfig.getShare().getBaseUrl();
        String pageUrl = baseUrl + "/generate/result/" + taskId;
        return SHARE_BASE_URL + "?url=" + URLEncoder.encode(pageUrl, StandardCharsets.UTF_8);
    }

    @Override
    public String getPlatformCode()
    {
        return "tiktok";
    }
}
