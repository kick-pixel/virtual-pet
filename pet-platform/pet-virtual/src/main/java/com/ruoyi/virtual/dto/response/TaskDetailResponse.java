package com.ruoyi.virtual.dto.response;

import java.util.Date;

/**
 * 任务详情响应
 */
public class TaskDetailResponse
{
    private Long taskId;
    private Long fileId;
    private String ossVideoUrl;
    private String taskUuid;
    private String status;
    private Integer progress;
    private String videoUrl;
    private String promptText;
    private String promptImageUrl;
    private String modelName;
    private Integer videoDuration;
    private String videoResolution;
    private Long creditsCost;
    private String errorMessage;
    private Date createTime;
    private Integer likeCount;
    private String videoAspectRatio;

    public String getVideoAspectRatio() {
		return videoAspectRatio;
	}

    public void setVideoAspectRatio(String videoAspectRatio) {
		this.videoAspectRatio = videoAspectRatio;
	}

    public Integer getLikeCount() {
		return likeCount;
	}

    public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getOssVideoUrl() {
        return ossVideoUrl;
    }

    public void setOssVideoUrl(String ossVideoUrl) {
        this.ossVideoUrl = ossVideoUrl;
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTaskUuid() { return taskUuid; }
    public void setTaskUuid(String taskUuid) { this.taskUuid = taskUuid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public String getPromptImageUrl() { return promptImageUrl; }
    public void setPromptImageUrl(String promptImageUrl) { this.promptImageUrl = promptImageUrl; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }

    public String getVideoResolution() { return videoResolution; }
    public void setVideoResolution(String videoResolution) { this.videoResolution = videoResolution; }

    public Long getCreditsCost() { return creditsCost; }
    public void setCreditsCost(Long creditsCost) { this.creditsCost = creditsCost; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
