package com.ruoyi.framework.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置（用于分布式锁）
 * 复用现有 spring.data.redis 连接配置
 *
 * @author ruoyi
 */
@Configuration
public class RedissonConfig
{
    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:30s}")
    private String timeout;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient()
    {
        Config config = new Config();

        String address = "redis://" + host + ":" + port;

        config.useSingleServer()
            .setAddress(address)
            .setDatabase(database)
            .setConnectTimeout(parseTimeoutMs(timeout))
            .setConnectionMinimumIdleSize(2)
            .setConnectionPoolSize(10);

        if (password != null && !password.isEmpty())
        {
            config.useSingleServer().setPassword(password);
        }

        log.info("初始化 Redisson 客户端: address={}, database={}", address, database);
        return Redisson.create(config);
    }

    /**
     * 解析 Spring 超时格式（如 "30s"）为毫秒
     */
    private int parseTimeoutMs(String timeout)
    {
        if (timeout == null || timeout.isEmpty())
        {
            return 30000;
        }
        timeout = timeout.trim().toLowerCase();
        if (timeout.endsWith("ms"))
        {
            return Integer.parseInt(timeout.replace("ms", ""));
        }
        if (timeout.endsWith("s"))
        {
            return Integer.parseInt(timeout.replace("s", "")) * 1000;
        }
        return Integer.parseInt(timeout);
    }
}
