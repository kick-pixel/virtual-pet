package com.ruoyi.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 *
 * 用于发送 HTTP 请求（如调用外部 API）
 *
 * @author ruoyi
 */
@Configuration
public class RestTemplateConfig
{
    /**
     * 创建 RestTemplate Bean
     *
     * 配置超时时间：
     * - 连接超时：5秒
     * - 读取超时：10秒
     */
    @Bean
    public RestTemplate restTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 连接超时时间（毫秒）
        factory.setConnectTimeout(5000);

        // 读取超时时间（毫秒）
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }
}
