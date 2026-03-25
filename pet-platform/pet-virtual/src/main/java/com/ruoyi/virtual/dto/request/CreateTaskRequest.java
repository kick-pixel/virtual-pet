package com.ruoyi.virtual.dto.request;

import jakarta.validation.constraints.Size;
import com.ruoyi.common.utils.StringUtils;

/**
 * 创建视频生成任务请求
 */
public class CreateTaskRequest
{
    /** 提示词（可选，但至少需要提示词或图片之一） */
    @Size(max = 1000, message = "Prompt must be at most 1000 characters")
    private String prompt;

    /** 上传的宠物图片 URL（可选，但至少需要提示词或图片之一） */
    private String imageUrl;

    private Long fileId;

    /** 视频要求（可选） */
    private String requirements;

    /** 视频时长（整数，如 5, 10） */
    private Integer duration = 5;

    /** 分辨率（如 720p, 1080p） */
    private String resolution = "720p";

    /** 宽高比（如 16:9, 9:16, 1:1） */
    private String aspectRatio = "16:9";

    /** 模型名称（可选） */
    private String model;

    /** 风格（可选） */
    private String style;

    /** 生成选项 ID（关联 virtual_generation_option） */
    private Long optionId;

    /**
     * 验证至少有提示词或图片之一
     */
    public boolean isValid()
    {
        return StringUtils.isNotEmpty(prompt) || StringUtils.isNotEmpty(imageUrl);
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
}
