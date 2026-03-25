package com.ruoyi.file.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * S3客户端配置
 *
 * @author ruoyi
 */
@Configuration
public class S3ClientConfig
{
    @Autowired
    private S3Config s3Config;

    @Bean
    public S3Client s3Client()
    {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            s3Config.getAccessKeyId(),
            s3Config.getAccessKeySecret()
        );

        S3Configuration s3Configuration = S3Configuration.builder()
            .pathStyleAccessEnabled(s3Config.isPathStyleAccess())
            .build();

        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(s3Config.getEndpoint()))
            .region(Region.of(s3Config.getRegion()))
            .serviceConfiguration(s3Configuration)
            .build();
    }
}
