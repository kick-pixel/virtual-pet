package com.ruoyi.ai.service;

import com.ruoyi.ai.domain.VideoGenerationRequest;

/**
 * 视频提供商服务接口
 *
 * @author ruoyi
 */
public interface IVideoProviderService
{
    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProviderName();

    String getModelName();

    /**
     * 创建视频生成任务
     *
     * @param request 生成请求
     * @return 提供商任务ID
     */
    String createTask(VideoGenerationRequest request);

    /**
     * 查询任务状态
     *
     * @param providerTaskId 提供商任务ID
     * @return 任务状态信息（JSON字符串）
     */
    TaskStatus queryTaskStatus(String providerTaskId);

    /**
     * 取消任务
     *
     * @param providerTaskId 提供商任务ID
     * @return 是否成功
     */
    boolean cancelTask(String providerTaskId);

    /**
     * 任务状态
     */
    class TaskStatus
    {
        /** 状态（pending/processing/completed/failed） */
        private String status;

        /** 进度（0-100） */
        private Integer progress;

        /** 视频URL */
        private String videoUrl;

        /** 错误代码 */
        private String errorCode;

        /** 错误信息 */
        private String errorMessage;

        public TaskStatus()
        {
        }

        public TaskStatus(String status, Integer progress)
        {
            this.status = status;
            this.progress = progress;
        }

        public static TaskStatus processing(int progress)
        {
            return new TaskStatus("processing", progress);
        }

        public static TaskStatus completed(String videoUrl)
        {
            TaskStatus status = new TaskStatus("completed", 100);
            status.setVideoUrl(videoUrl);
            return status;
        }

        public static TaskStatus failed(String errorCode, String errorMessage)
        {
            TaskStatus status = new TaskStatus("failed", 0);
            status.setErrorCode(errorCode);
            status.setErrorMessage(errorMessage);
            return status;
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
}
