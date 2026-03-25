package com.ruoyi.common.event.ai;

/**
 * 视频生成失败事件
 *
 * 当视频生成任务失败时发布此事件
 * 业务系统可以监听此事件进行：
 * - 发送失败通知
 * - 退款处理（如果已扣费）
 * - 记录失败统计
 * - 触发告警（失败率过高时）
 * - 自动重试判断
 *
 * @author ruoyi
 */
public class VideoGenerationFailedEvent extends VideoGenerationEvent
{
    private static final long serialVersionUID = 1L;

    /** 错误代码 */
    private final String errorCode;

    /** 错误信息 */
    private final String errorMessage;

    /** 当前重试次数 */
    private final Integer retryCount;

    /** 最大重试次数 */
    private final Integer maxRetry;

    /** 原始提示词（用于重试） */
    private final String promptText;

    public VideoGenerationFailedEvent(Object source, Long taskId, String taskUuid,
                                       Long userId, String modelName, String errorCode,
                                       String errorMessage, Integer retryCount, Integer maxRetry,
                                       String promptText)
    {
        super(source, taskId, taskUuid, userId, modelName);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
        this.promptText = promptText;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public Integer getMaxRetry()
    {
        return maxRetry;
    }

    public String getPromptText()
    {
        return promptText;
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry()
    {
        return retryCount < maxRetry;
    }

    @Override
    public String toString()
    {
        return "VideoGenerationFailedEvent{" +
                "taskId=" + getTaskId() +
                ", userId=" + getUserId() +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", retryCount=" + retryCount +
                ", maxRetry=" + maxRetry +
                ", canRetry=" + canRetry() +
                '}';
    }
}
