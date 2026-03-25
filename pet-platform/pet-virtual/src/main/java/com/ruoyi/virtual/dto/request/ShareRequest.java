package com.ruoyi.virtual.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 分享请求
 */
public class ShareRequest
{
    /** 分享平台（tiktok, twitter） */
    @NotBlank(message = "Share platform cannot be empty")
    private String platform;

    /** 分享链接（用于确认分享时提交） */
    private String shareUrl;

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
}
