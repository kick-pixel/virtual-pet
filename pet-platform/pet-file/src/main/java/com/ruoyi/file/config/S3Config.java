package com.ruoyi.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * S3存储配置
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "pet.file.s3")
public class S3Config
{
    /** 访问密钥ID */
    private String accessKeyId;

    /** 访问密钥Secret */
    private String accessKeySecret;

    /** 端点URL */
    private String endpoint;

    /** 区域 */
    private String region = "auto";

    /** 默认存储桶 */
    private String bucket;

    /** 公开访问域名 */
    private String publicDomain;

    /** 是否启用路径样式访问（用于MinIO等） */
    private boolean pathStyleAccess = true;

    /** 上传分片大小（MB） */
    private int partSize = 10;

    /** 上传超时时间（秒） */
    private int uploadTimeout = 300;

    public String getAccessKeyId()
    {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId)
    {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret()
    {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret)
    {
        this.accessKeySecret = accessKeySecret;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getBucket()
    {
        return bucket;
    }

    public void setBucket(String bucket)
    {
        this.bucket = bucket;
    }

    public String getPublicDomain()
    {
        return publicDomain;
    }

    public void setPublicDomain(String publicDomain)
    {
        this.publicDomain = publicDomain;
    }

    public boolean isPathStyleAccess()
    {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess)
    {
        this.pathStyleAccess = pathStyleAccess;
    }

    public int getPartSize()
    {
        return partSize;
    }

    public void setPartSize(int partSize)
    {
        this.partSize = partSize;
    }

    public int getUploadTimeout()
    {
        return uploadTimeout;
    }

    public void setUploadTimeout(int uploadTimeout)
    {
        this.uploadTimeout = uploadTimeout;
    }
}
