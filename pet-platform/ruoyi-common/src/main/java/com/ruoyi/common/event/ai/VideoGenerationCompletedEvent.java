package com.ruoyi.common.event.ai;

/**
 * 视频生成完成事件
 *
 * 当视频生成任务完成并成功上传到OSS后发布此事件
 * 业务系统可以监听此事件进行：
 * - 发送完成通知（站内信、邮件、短信、微信推送）
 * - 扣费处理
 * - 更新用户积分/等级
 * - 数据统计分析
 * - Webhook回调
 *
 * @author ruoyi
 */
public class VideoGenerationCompletedEvent extends VideoGenerationEvent
{
    private static final long serialVersionUID = 1L;

    /** 提供商视频URL */
    private final String providerVideoUrl;

    /** OSS视频URL（用户访问URL） */
    private final String ossVideoUrl;

    /** 文件ID */
    private final Long fileId;

    /** 视频时长（秒） */
    private final Integer videoDuration;

    /** 视频分辨率 */
    private final String videoResolution;

    /** 文件大小（字节） */
    private final Long fileSize;

    public VideoGenerationCompletedEvent(Object source, Long taskId, String taskUuid,
                                          Long userId, String modelName, String providerVideoUrl,
                                          String ossVideoUrl, Long fileId, Integer videoDuration,
                                          String videoResolution, Long fileSize)
    {
        super(source, taskId, taskUuid, userId, modelName);
        this.providerVideoUrl = providerVideoUrl;
        this.ossVideoUrl = ossVideoUrl;
        this.fileId = fileId;
        this.videoDuration = videoDuration;
        this.videoResolution = videoResolution;
        this.fileSize = fileSize;
    }

    public String getProviderVideoUrl()
    {
        return providerVideoUrl;
    }

    public String getOssVideoUrl()
    {
        return ossVideoUrl;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public Integer getVideoDuration()
    {
        return videoDuration;
    }

    public String getVideoResolution()
    {
        return videoResolution;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    @Override
    public String toString()
    {
        return "VideoGenerationCompletedEvent{" +
                "taskId=" + getTaskId() +
                ", userId=" + getUserId() +
                ", modelName='" + getModelName() + '\'' +
                ", ossVideoUrl='" + ossVideoUrl + '\'' +
                ", fileId=" + fileId +
                ", duration=" + videoDuration +
                ", resolution='" + videoResolution + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
