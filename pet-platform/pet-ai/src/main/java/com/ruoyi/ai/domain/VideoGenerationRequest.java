package com.ruoyi.ai.domain;

/**
 * 视频生成请求
 *
 * @author ruoyi
 */
public class VideoGenerationRequest
{
    /** 提供商名称（openai/ark，为空则使用默认） */
    private String provider;

    /** 文本提示词 */
    private String prompt;

    private Long fileId;

    /** 图片提示URL（用于图生视频） */
    private String imageUrl;

    /** 模型名称 */
    private String model;

    /** 视频时长（秒） */
    private Integer duration = 10;

    /** 视频分辨率 */
    private String resolution = "720p";

    /** 宽高比 */
    private String aspectRatio = "16:9";

    /** 是否生成音频（仅火山引擎 Seedance 1.5 pro 支持） */
    private Boolean generateAudio = false;

    /** 是否添加水印 */
    private Boolean watermark = false;

    /** 视频比例（火山引擎使用，如 "16:9", "9:16", "adaptive"） */
    private String ratio;

    /** 首尾帧图片 URL（用于首尾帧生视频） */
    private String endFrameImageUrl;

    /** 多参考图 URL 列表（用于多参考图生视频） */
    private java.util.List<String> referenceImageUrls;

    public VideoGenerationRequest()
    {
    }

    public VideoGenerationRequest(String prompt)
    {
        this.prompt = prompt;
    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public Integer getDuration()
    {
        return duration;
    }

    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(String resolution)
    {
        this.resolution = resolution;
    }

    public String getAspectRatio()
    {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio)
    {
        this.aspectRatio = aspectRatio;
    }

    public Boolean getGenerateAudio()
    {
        return generateAudio;
    }

    public void setGenerateAudio(Boolean generateAudio)
    {
        this.generateAudio = generateAudio;
    }

    public Boolean getWatermark()
    {
        return watermark;
    }

    public void setWatermark(Boolean watermark)
    {
        this.watermark = watermark;
    }

    public String getRatio()
    {
        return ratio;
    }

    public void setRatio(String ratio)
    {
        this.ratio = ratio;
    }

    public String getEndFrameImageUrl()
    {
        return endFrameImageUrl;
    }

    public void setEndFrameImageUrl(String endFrameImageUrl)
    {
        this.endFrameImageUrl = endFrameImageUrl;
    }

    public java.util.List<String> getReferenceImageUrls()
    {
        return referenceImageUrls;
    }

    public void setReferenceImageUrls(java.util.List<String> referenceImageUrls)
    {
        this.referenceImageUrls = referenceImageUrls;
    }
}
