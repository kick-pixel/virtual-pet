package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI视频生成任务对象 ai_video_task
 *
 * @author ruoyi
 */
public class AiVideoTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @Excel(name = "Task ID", cellType = ColumnType.NUMERIC)
    private Long taskId;

    /** 任务UUID */
    @Excel(name = "Task UUID")
    private String taskUuid;

    /** AI提供商（openai/runway/stable/aliyun） */
    @Excel(name = "AI Provider")
    private String provider;

    /** 提供商任务ID */
    private String providerTaskId;

    /** 用户ID */
    private Long userId;

    /** 文本提示词 */
    @Excel(name = "Prompt")
    private String promptText;

    /** 图片提示URL */
    private String promptImageUrl;

    /** 图片提示文件ID */
    private Long promptImageFileId;

    /** 模型名称 */
    @Excel(name = "Model Name")
    private String modelName;

    /** 视频时长（秒） */
    @Excel(name = "Video Duration", cellType = ColumnType.NUMERIC)
    private Integer videoDuration;

    /** 视频分辨率 */
    @Excel(name = "Video Resolution")
    private String videoResolution;

    /** 视频宽高比 */
    private String videoAspectRatio;

    /** 任务状态（pending/processing/completed/failed/cancelled） */
    @Excel(name = "Status", readConverterExp = "pending=Pending,processing=Processing,completed=Completed,failed=Failed,cancelled=Cancelled")
    private String status;

    /** 生成进度（0-100） */
    @Excel(name = "Progress", cellType = ColumnType.NUMERIC)
    private Integer progress;

    /** 提供商视频URL */
    private String providerVideoUrl;

    /** OSS视频URL */
    @Excel(name = "Video URL")
    private String ossVideoUrl;

    /** 关联文件ID */
    private Long fileId;

    /** 视频首帧图片文件ID */
    private Long videoPicFileId;

    /** 视频首帧图片URL */
    private String videoPicUrl;

    /** 错误代码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 预计完成时间（秒） */
    private Integer estimatedTime;

    /** 消费金额（USD） */
    private BigDecimal costAmount;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedAt;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedAt;

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

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getProviderTaskId()
    {
        return providerTaskId;
    }

    public void setProviderTaskId(String providerTaskId)
    {
        this.providerTaskId = providerTaskId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getPromptText()
    {
        return promptText;
    }

    public void setPromptText(String promptText)
    {
        this.promptText = promptText;
    }

    public String getPromptImageUrl()
    {
        return promptImageUrl;
    }

    public void setPromptImageUrl(String promptImageUrl)
    {
        this.promptImageUrl = promptImageUrl;
    }

    public Long getPromptImageFileId()
    {
        return promptImageFileId;
    }

    public void setPromptImageFileId(Long promptImageFileId)
    {
        this.promptImageFileId = promptImageFileId;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public Integer getVideoDuration()
    {
        return videoDuration;
    }

    public void setVideoDuration(Integer videoDuration)
    {
        this.videoDuration = videoDuration;
    }

    public String getVideoResolution()
    {
        return videoResolution;
    }

    public void setVideoResolution(String videoResolution)
    {
        this.videoResolution = videoResolution;
    }

    public String getVideoAspectRatio()
    {
        return videoAspectRatio;
    }

    public void setVideoAspectRatio(String videoAspectRatio)
    {
        this.videoAspectRatio = videoAspectRatio;
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

    public String getProviderVideoUrl()
    {
        return providerVideoUrl;
    }

    public void setProviderVideoUrl(String providerVideoUrl)
    {
        this.providerVideoUrl = providerVideoUrl;
    }

    public String getOssVideoUrl()
    {
        return ossVideoUrl;
    }

    public void setOssVideoUrl(String ossVideoUrl)
    {
        this.ossVideoUrl = ossVideoUrl;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public Long getVideoPicFileId()
    {
        return videoPicFileId;
    }

    public void setVideoPicFileId(Long videoPicFileId)
    {
        this.videoPicFileId = videoPicFileId;
    }

    public String getVideoPicUrl()
    {
        return videoPicUrl;
    }

    public void setVideoPicUrl(String videoPicUrl)
    {
        this.videoPicUrl = videoPicUrl;
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

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetry()
    {
        return maxRetry;
    }

    public void setMaxRetry(Integer maxRetry)
    {
        this.maxRetry = maxRetry;
    }

    public Integer getEstimatedTime()
    {
        return estimatedTime;
    }

    public void setEstimatedTime(Integer estimatedTime)
    {
        this.estimatedTime = estimatedTime;
    }

    public BigDecimal getCostAmount()
    {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount)
    {
        this.costAmount = costAmount;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(Date startedAt)
    {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt)
    {
        this.completedAt = completedAt;
    }

    /** 点赞数 */
    private Integer likeCount;

    public Integer getLikeCount()
    {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount)
    {
        this.likeCount = likeCount;
    }

    /** 浏览量 */
    private Integer viewCount;

    /** 管理状态 0-正常 1-封禁 */
    private Integer adminStatus;

    /** 虚拟平台用户ID（ai_video_task.user_id 对应 virtual_user.user_id） */
    private Long virtualUserId;

    public Integer getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(Integer viewCount)
    {
        this.viewCount = viewCount;
    }

    public Integer getAdminStatus()
    {
        return adminStatus;
    }

    public void setAdminStatus(Integer adminStatus)
    {
        this.adminStatus = adminStatus;
    }

    public Long getVirtualUserId()
    {
        return virtualUserId;
    }

    public void setVirtualUserId(Long virtualUserId)
    {
        this.virtualUserId = virtualUserId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskUuid", getTaskUuid())
            .append("provider", getProvider())
            .append("modelName", getModelName())
            .append("userId", getUserId())
            .append("promptText", getPromptText())
            .append("videoDuration", getVideoDuration())
            .append("videoResolution", getVideoResolution())
            .append("status", getStatus())
            .append("progress", getProgress())
            .append("ossVideoUrl", getOssVideoUrl())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
