package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 视频点赞记录 virtual_task_like
 *
 * @author ruoyi
 */
public class VirtualTaskLike extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 点赞ID */
    private Long likeId;

    /** 用户ID */
    private Long userId;

    /** 视频任务ID */
    private Long taskId;

    public Long getLikeId()
    {
        return likeId;
    }

    public void setLikeId(Long likeId)
    {
        this.likeId = likeId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }
}
