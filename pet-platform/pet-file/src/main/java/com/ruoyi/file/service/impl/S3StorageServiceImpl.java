package com.ruoyi.file.service.impl;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.file.config.S3Config;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IStorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import java.net.URI;

/**
 * S3存储服务实现
 *
 * @author ruoyi
 */
@Service
public class S3StorageServiceImpl implements IStorageService
{
    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Config s3Config;

    /**
     * 上传文件
     */
    @Override
    public FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize)
    {
        return upload(inputStream, fileName, contentType, fileSize, null);
    }

    /**
     * 上传文件到指定目录
     */
    @Override
    public FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize, String directory)
    {
        try
        {
            // 生成文件Key
            String fileKey = generateFileKey(fileName, directory);

            // 上传到S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileKey)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, fileSize));

            // 获取文件扩展名
            String fileExt = getFileExtension(fileName);
            // 获取文件类型
            String fileType = getFileType(contentType, fileExt);
            // 获取访问URL（如果未配置公开域名，则使用预签名URL，默认24小时有效期）
            String fileUrl = getAccessUrl(fileKey);

            FileUploadResult result = new FileUploadResult();
            result.setFileName(fileName);
            result.setFileKey(fileKey);
            result.setFileUrl(fileUrl);
            result.setFileSize(fileSize);
            result.setFileType(fileType);
            result.setMimeType(contentType);
            result.setFileExt(fileExt);

            log.info("文件上传成功: {}, fileUrl: {}", fileKey, fileUrl);
            return result;
        }
        catch (Exception e)
        {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     */
    @Override
    public InputStream download(String fileKey)
    {
        try
        {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileKey)
                .build();

            return s3Client.getObject(getRequest);
        }
        catch (Exception e)
        {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    @Override
    public boolean delete(String fileKey)
    {
        try
        {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileKey)
                .build();

            s3Client.deleteObject(deleteRequest);
            log.info("文件删除成功: {}", fileKey);
            return true;
        }
        catch (Exception e)
        {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     */
    @Override
    public boolean exists(String fileKey)
    {
        try
        {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(fileKey)
                .build();

            s3Client.headObject(headRequest);
            return true;
        }
        catch (NoSuchKeyException e)
        {
            return false;
        }
        catch (Exception e)
        {
            log.error("检查文件存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取文件访问URL
     */
    @Override
    public String getFileUrl(String fileKey)
    {
        if (StringUtils.isNotEmpty(s3Config.getPublicDomain()))
        {
            String domain = s3Config.getPublicDomain();
            if (!domain.endsWith("/"))
            {
                domain = domain + "/";
            }
            return domain + fileKey;
        }
        else
        {
            // 使用S3端点构建URL
            String endpoint = s3Config.getEndpoint();
            if (endpoint.endsWith("/"))
            {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint + "/" + s3Config.getBucket() + "/" + fileKey;
        }
    }

    /**
     * 获取文件访问URL（智能选择）
     *
     * 如果配置了公开域名，返回公开URL；否则返回预签名URL
     */
    private String getAccessUrl(String fileKey)
    {
        if (StringUtils.isNotEmpty(s3Config.getPublicDomain()))
        {
            // 有公开域名，返回永久URL
            return getFileUrl(fileKey);
        }
        else
        {
            // 无公开域名，返回预签名URL（24小时有效期）
            log.debug("未配置公开域名，使用预签名URL: {}", fileKey);
            return getPresignedUrl(fileKey, 1440);
        }
    }

    /**
     * 获取预签名URL
     */
    @Override
    public String getPresignedUrl(String fileKey, int expirationMinutes)
    {
        try
        {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Config.getAccessKeyId(),
                s3Config.getAccessKeySecret()
            );

            try (S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(s3Config.getEndpoint()))
                .region(Region.of(s3Config.getRegion()))
                .build())
            {

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucket())
                    .key(fileKey)
                    .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            }
        }
        catch (Exception e)
        {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件Key
     */
    private String generateFileKey(String fileName, String directory)
    {
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = getFileExtension(fileName);

        StringBuilder keyBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(directory))
        {
            keyBuilder.append(directory);
            if (!directory.endsWith("/"))
            {
                keyBuilder.append("/");
            }
        }
        keyBuilder.append(dateDir).append("/").append(uuid);
        if (StringUtils.isNotEmpty(ext))
        {
            keyBuilder.append(".").append(ext);
        }

        return keyBuilder.toString();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName)
    {
        if (StringUtils.isEmpty(fileName))
        {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1)
        {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 根据MIME类型和扩展名判断文件类型
     */
    private String getFileType(String mimeType, String ext)
    {
        if (StringUtils.isEmpty(mimeType))
        {
            return "other";
        }

        if (mimeType.startsWith("image/"))
        {
            return "image";
        }
        else if (mimeType.startsWith("video/"))
        {
            return "video";
        }
        else if (mimeType.startsWith("audio/"))
        {
            return "audio";
        }
        else if (mimeType.contains("pdf") || mimeType.contains("document") ||
                 mimeType.contains("word") || mimeType.contains("excel") ||
                 mimeType.contains("spreadsheet") || mimeType.contains("presentation") ||
                 mimeType.contains("text"))
        {
            return "document";
        }
        else
        {
            return "other";
        }
    }
}
