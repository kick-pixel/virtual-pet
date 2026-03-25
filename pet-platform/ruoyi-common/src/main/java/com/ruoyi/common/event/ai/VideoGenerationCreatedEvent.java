package com.ruoyi.common.event.ai;

/**
 * 视频生成任务创建事件
 *
 * 当用户创建视频生成任务时发布此事件
 * 业务系统可以监听此事件进行：
 * - 发送任务创建通知
 * - 记录用户行为
 * - 预扣费用
 * - 统计分析
 *
 * @author ruoyi
 */
public class VideoGenerationCreatedEvent extends VideoGenerationEvent
{
    private static final long serialVersionUID = 1L;

    /** 文本提示词 */
    private final String promptText;

    /** 图片提示URL */
    private final String promptImageUrl;

    /** 提供商任务ID */
    private final String providerTaskId;

    /** 视频时长（秒） */
    private final Integer videoDuration;

    /** 视频分辨率 */
    private final String videoResolution;

    public VideoGenerationCreatedEvent(Object source, Long taskId, String taskUuid,
                                        Long userId, String modelName, String promptText,
                                        String promptImageUrl, String providerTaskId,
                                        Integer videoDuration, String videoResolution)
    {
        super(source, taskId, taskUuid, userId, modelName);
        this.promptText = promptText;
        this.promptImageUrl = promptImageUrl;
        this.providerTaskId = providerTaskId;
        this.videoDuration = videoDuration;
        this.videoResolution = videoResolution;
    }

    public String getPromptText()
    {
        return promptText;
    }

    public String getPromptImageUrl()
    {
        return promptImageUrl;
    }

    public String getProviderTaskId()
    {
        return providerTaskId;
    }

    public Integer getVideoDuration()
    {
        return videoDuration;
    }

    public String getVideoResolution()
    {
        return videoResolution;
    }

    @Override
    public String toString()
    {
        return "VideoGenerationCreatedEvent{" +
                "taskId=" + getTaskId() +
                ", userId=" + getUserId() +
                ", modelName='" + getModelName() + '\'' +
                ", promptText='" + promptText + '\'' +
                ", hasImage=" + (promptImageUrl != null) +
                ", providerTaskId='" + providerTaskId + '\'' +
                ", duration=" + videoDuration +
                ", resolution='" + videoResolution + '\'' +
                '}';
    }
}
