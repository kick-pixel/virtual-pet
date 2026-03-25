package com.ruoyi.ai.domain;

import java.io.Serializable;

/**
 * 视频处理队列消息
 *
 * @author ruoyi
 */
public class VideoProcessMessage implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 当前重试次数 */
    private int retryCount;

    /** 消息创建时间戳 */
    private long createTime;

    public VideoProcessMessage()
    {
    }

    public VideoProcessMessage(Long taskId)
    {
        this.taskId = taskId;
        this.retryCount = 0;
        this.createTime = System.currentTimeMillis();
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString()
    {
        return "VideoProcessMessage{taskId=" + taskId + ", retryCount=" + retryCount + "}";
    }
}
