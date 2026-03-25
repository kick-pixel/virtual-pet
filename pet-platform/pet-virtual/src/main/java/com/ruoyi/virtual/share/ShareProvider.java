package com.ruoyi.virtual.share;

/**
 * 分享提供商接口
 */
public interface ShareProvider
{
    /**
     * 生成分享链接
     *
     * @param taskId 视频任务 ID
     * @param userId 用户 ID
     * @return 分享链接（第三方平台的 intent URL）
     */
    String generateShareUrl(Long taskId, Long userId);

    /**
     * 获取平台代码
     */
    String getPlatformCode();
}
