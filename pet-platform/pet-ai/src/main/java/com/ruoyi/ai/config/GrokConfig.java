package com.ruoyi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * xAI Grok 配置
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "pet.ai.grok")
public class GrokConfig
{
    /** API Key */
    private String apiKey;

    /** 基础URL */
    private String baseUrl = "https://api.x.ai/v1";

    /** 默认视频模型 */
    private String defaultVideoModel = "grok-imagine-video";

    /** 请求超时时间（秒） */
    private int timeout = 60;

    /** 轮询间隔（秒） */
    private int pollInterval = 5;

    /** 最大轮询次数 */
    private int maxPollAttempts = 720;

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

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public int getPollInterval()
    {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval)
    {
        this.pollInterval = pollInterval;
    }

    public int getMaxPollAttempts()
    {
        return maxPollAttempts;
    }

    public void setMaxPollAttempts(int maxPollAttempts)
    {
        this.maxPollAttempts = maxPollAttempts;
    }
}
