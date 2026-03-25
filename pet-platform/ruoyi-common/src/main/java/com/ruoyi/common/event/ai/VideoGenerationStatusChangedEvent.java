package com.ruoyi.common.event.ai;

/**
 * 视频生成状态变化事件
 *
 * 当视频生成任务状态发生变化时发布此事件
 * 业务系统可以监听此事件进行：
 * - 发送进度通知（如每25%通知一次）
 * - 更新前端实时状态
 * - 记录状态变化历史
 * - 统计分析
 *
 * @author ruoyi
 */
public class VideoGenerationStatusChangedEvent extends VideoGenerationEvent
{
    private static final long serialVersionUID = 1L;

    /** 旧状态 */
    private final String oldStatus;

    /** 新状态 */
    private final String newStatus;

    /** 当前进度（0-100） */
    private final Integer progress;

    /** 预计剩余时间（秒） */
    private final Integer estimatedTime;

    public VideoGenerationStatusChangedEvent(Object source, Long taskId, String taskUuid,
                                               Long userId, String modelName, String oldStatus,
                                               String newStatus, Integer progress, Integer estimatedTime)
    {
        super(source, taskId, taskUuid, userId, modelName);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.progress = progress;
        this.estimatedTime = estimatedTime;
    }

    public String getOldStatus()
    {
        return oldStatus;
    }

    public String getNewStatus()
    {
        return newStatus;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public Integer getEstimatedTime()
    {
        return estimatedTime;
    }

    /**
     * 是否是重要的进度节点（每25%）
     */
    public boolean isSignificantProgress()
    {
        return progress != null && progress % 25 == 0;
    }

    /**
     * 是否状态变为处理中
     */
    public boolean isStarted()
    {
        return "processing".equals(newStatus) && !"processing".equals(oldStatus);
    }

    /**
     * 是否状态变为已完成
     */
    public boolean isCompleted()
    {
        return "completed".equals(newStatus);
    }

    /**
     * 是否状态变为失败
     */
    public boolean isFailed()
    {
        return "failed".equals(newStatus);
    }

    @Override
    public String toString()
    {
        return "VideoGenerationStatusChangedEvent{" +
                "taskId=" + getTaskId() +
                ", userId=" + getUserId() +
                ", statusChange='" + oldStatus + "'->" + newStatus + '\'' +
                ", progress=" + progress + "%" +
                ", estimatedTime=" + estimatedTime + "s" +
                '}';
    }
}
