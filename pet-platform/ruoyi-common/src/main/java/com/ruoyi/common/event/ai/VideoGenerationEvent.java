package com.ruoyi.common.event.ai;

import java.util.Date;
import org.springframework.context.ApplicationEvent;

/**
 * AI视频生成事件基类
 *
 * 所有AI视频相关事件的基类，包含任务的基本信息
 * 注意：事件时间戳使用父类 ApplicationEvent 的 getTimestamp() 方法（返回 long 类型）
 *
 * @author ruoyi
 */
public abstract class VideoGenerationEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private final Long taskId;

    /** 任务UUID */
    private final String taskUuid;

    /** 用户ID */
    private final Long userId;

    /** 模型名称 */
    private final String modelName;

    public VideoGenerationEvent(Object source, Long taskId, String taskUuid,
                                 Long userId, String modelName)
    {
        super(source);
        this.taskId = taskId;
        this.taskUuid = taskUuid;
        this.userId = userId;
        this.modelName = modelName;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public String getTaskUuid()
    {
        return taskUuid;
    }

    public Long getUserId()
    {
        return userId;
    }

    public String getModelName()
    {
        return modelName;
    }

    /**
     * 获取事件时间（Date类型）
     *
     * @return 事件时间
     */
    public Date getEventTime()
    {
        return new Date(getTimestamp());
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" +
                "taskId=" + taskId +
                ", taskUuid='" + taskUuid + '\'' +
                ", userId=" + userId +
                ", modelName='" + modelName + '\'' +
                ", timestamp=" + getEventTime() +
                '}';
    }
}
