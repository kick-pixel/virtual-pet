package com.ruoyi.file.service;

import java.io.InputStream;
import com.ruoyi.file.domain.FileUploadResult;

/**
 * 存储服务接口
 *
 * @author ruoyi
 */
public interface IStorageService
{
    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param fileSize 文件大小
     * @return 上传结果
     */
    FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize);

    /**
     * 上传文件到指定目录
     *
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param fileSize 文件大小
     * @param directory 目录前缀
     * @return 上传结果
     */
    FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize, String directory);

    /**
     * 下载文件
     *
     * @param fileKey 文件Key
     * @return 文件输入流
     */
    InputStream download(String fileKey);

    /**
     * 删除文件
     *
     * @param fileKey 文件Key
     * @return 是否成功
     */
    boolean delete(String fileKey);

    /**
     * 检查文件是否存在
     *
     * @param fileKey 文件Key
     * @return 是否存在
     */
    boolean exists(String fileKey);

    /**
     * 获取文件访问URL
     *
     * @param fileKey 文件Key
     * @return 访问URL
     */
    String getFileUrl(String fileKey);

    /**
     * 获取预签名URL（用于临时访问私有文件）
     *
     * @param fileKey 文件Key
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    String getPresignedUrl(String fileKey, int expirationMinutes);
}
