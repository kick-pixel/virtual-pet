package com.ruoyi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎 Ark 配置
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "pet.ai.ark")
public class ArkConfig
{
    /** API密钥 */
    private String apiKey;

    /** API基础URL */
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";

    /** 默认视频生成模型 */
    private String defaultVideoModel = "doubao-seedance-1-5-pro-251215";

    /** 请求超时时间（秒） */
    private Integer timeout = 60;

    /** 任务状态轮询间隔（秒） */
    private Integer pollInterval = 10;

    /** 最大轮询次数 */
    private Integer maxPollAttempts = 360;

    /** 是否启用水印（默认关闭） */
    private Boolean watermarkEnabled = false;

    /** 是否启用音频生成（默认关闭，仅 1.5 pro 支持） */
    private Boolean audioEnabled = false;

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getDefaultVideoModel()
    {
        return defaultVideoModel;
    }

    public void setDefaultVideoModel(String defaultVideoModel)
    {
        this.defaultVideoModel = defaultVideoModel;
    }

    public Integer getTimeout()
    {
        return timeout;
    }

    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    public Integer getPollInterval()
    {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval)
    {
        this.pollInterval = pollInterval;
    }

    public Integer getMaxPollAttempts()
    {
        return maxPollAttempts;
    }

    public void setMaxPollAttempts(Integer maxPollAttempts)
    {
        this.maxPollAttempts = maxPollAttempts;
    }

    public Boolean getWatermarkEnabled()
    {
        return watermarkEnabled;
    }

    public void setWatermarkEnabled(Boolean watermarkEnabled)
    {
        this.watermarkEnabled = watermarkEnabled;
    }

    public Boolean getAudioEnabled()
    {
        return audioEnabled;
    }

    public void setAudioEnabled(Boolean audioEnabled)
    {
        this.audioEnabled = audioEnabled;
    }
}
