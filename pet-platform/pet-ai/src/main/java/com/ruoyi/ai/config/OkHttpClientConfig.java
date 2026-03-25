package com.ruoyi.ai.config;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import okhttp3.OkHttpClient;

/**
 * OkHttp客户端配置
 *
 * @author ruoyi
 */
@Configuration
public class OkHttpClientConfig
{
    @Autowired
    private OpenAiConfig openAiConfig;

    @Bean
    public OkHttpClient okHttpClient()
    {
        return new OkHttpClient.Builder()
            .connectTimeout(openAiConfig.getTimeout(), TimeUnit.SECONDS)
            .readTimeout(openAiConfig.getTimeout(), TimeUnit.SECONDS)
            .writeTimeout(openAiConfig.getTimeout(), TimeUnit.SECONDS)
            .build();
    }
}
