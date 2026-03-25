package com.ruoyi.virtual.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import okhttp3.OkHttpClient;

/**
 * OkHttp 客户端配置（Virtual 平台专用）
 *
 * @author ruoyi
 */
@Configuration
public class OkHttpConfig
{
    @Bean("virtualOkHttpClient")
    public OkHttpClient virtualOkHttpClient()
    {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
