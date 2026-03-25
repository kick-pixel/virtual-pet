package com.ruoyi.file.service;

import java.io.InputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.system.domain.SysFileInfo;

/**
 * 文件服务接口
 *
 * @author ruoyi
 */
public interface IFileService
{
    /**
     * 上传文件
     *
     * @param file 文件
     * @return 上传结果
     */
    FileUploadResult upload(MultipartFile file);

    /**
     * 上传文件到指定目录
     *
     * @param file 文件
     * @param directory 目录前缀
     * @return 上传结果
     */
    FileUploadResult upload(MultipartFile file, String directory);

    /**
     * 上传文件并关联业务
     *
     * @param file 文件
     * @param directory 目录前缀
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 上传结果
     */
    FileUploadResult upload(MultipartFile file, String directory, String businessType, Long businessId);

    /**
     * 上传文件流
     *
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @param fileSize 文件大小
     * @return 上传结果
     */
    FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize);

    /**
     * 上传文件流到指定目录
     *
     * @param inputStream 输入流
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
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream download(Long fileId);

    /**
     * 通过文件Key下载
     *
     * @param fileKey 文件Key
     * @return 文件输入流
     */
    InputStream downloadByKey(String fileKey);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean delete(Long fileId);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID数组
     * @return 删除数量
     */
    int deleteByIds(Long[] fileIds);

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    SysFileInfo getFileInfo(Long fileId);

    /**
     * 获取文件列表
     *
     * @param fileInfo 查询条件
     * @return 文件列表
     */
    List<SysFileInfo> selectFileList(SysFileInfo fileInfo);

    /**
     * 根据业务获取文件列表
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 文件列表
     */
    List<SysFileInfo> selectFileByBusiness(String businessType, Long businessId);

    /**
     * 获取预签名URL
     *
     * @param fileId 文件ID
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    String getPresignedUrl(Long fileId, int expirationMinutes);
}
