package com.ruoyi.virtual.share;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.virtual.config.VirtualConfig;

/**
 * Twitter/X 分享提供商
 *
 * @author ruoyi
 */
@Component
public class TwitterShareProvider implements ShareProvider
{
    private static final String SHARE_BASE_URL = "https://x.com/intent/tweet";

    @Autowired
    private VirtualConfig virtualConfig;

    @Override
    public String generateShareUrl(Long taskId, Long userId)
    {
        String baseUrl = virtualConfig.getShare().getBaseUrl();
        String pageUrl = baseUrl + "/showcase/post/" + taskId;
        String text = "Check out my AI-generated pet video! \uD83D\uDC3E✨";
        return SHARE_BASE_URL
                + "?url=" + URLEncoder.encode(pageUrl, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&hashtags=" + URLEncoder.encode("PetVirtual,AI", StandardCharsets.UTF_8);
    }

    @Override
    public String getPlatformCode()
    {
        return "twitter";
    }
}
