package com.ruoyi.ai.domain;

/**
 * 视频生成结果
 *
 * @author ruoyi
 */
public class VideoGenerationResult
{
    /** 任务ID */
    private Long taskId;

    /** 任务UUID */
    private String taskUuid;

    /** 提供商任务ID */
    private String providerTaskId;

    /** 状态 */
    private String status;

    /** 进度（0-100） */
    private Integer progress;

    /** 视频URL（生成完成后） */
    private String videoUrl;

    /** 错误代码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    public VideoGenerationResult()
    {
    }

    public static VideoGenerationResult success(Long taskId, String taskUuid, String providerTaskId)
    {
        VideoGenerationResult result = new VideoGenerationResult();
        result.setTaskId(taskId);
        result.setTaskUuid(taskUuid);
        result.setProviderTaskId(providerTaskId);
        result.setStatus("pending");
        result.setProgress(0);
        return result;
    }

    public static VideoGenerationResult error(String errorCode, String errorMessage)
    {
        VideoGenerationResult result = new VideoGenerationResult();
        result.setStatus("failed");
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskUuid()
    {
        return taskUuid;
    }

    public void setTaskUuid(String taskUuid)
    {
        this.taskUuid = taskUuid;
    }

    public String getProviderTaskId()
    {
        return providerTaskId;
    }

    public void setProviderTaskId(String providerTaskId)
    {
        this.providerTaskId = providerTaskId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getProgress()
    {
        return progress;
    }

    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
