package com.ruoyi.file.service.impl;

import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.file.config.S3Config;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IFileService;
import com.ruoyi.file.service.IStorageService;
import com.ruoyi.system.domain.SysFileInfo;
import com.ruoyi.system.service.ISysFileInfoService;

/**
 * 文件服务实现
 *
 * @author ruoyi
 */
@Service
public class FileServiceImpl implements IFileService
{
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private IStorageService storageService;

    @Autowired
    private ISysFileInfoService fileInfoService;

    @Autowired
    private S3Config s3Config;

    /**
     * 上传文件
     */
    @Override
    @Transactional
    public FileUploadResult upload(MultipartFile file)
    {
        return upload(file, null);
    }

    /**
     * 上传文件到指定目录
     */
    @Override
    @Transactional
    public FileUploadResult upload(MultipartFile file, String directory)
    {
        return upload(file, directory, null, null);
    }

    /**
     * 上传文件并关联业务
     */
    @Override
    @Transactional
    public FileUploadResult upload(MultipartFile file, String directory, String businessType, Long businessId)
    {
        try
        {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            long fileSize = file.getSize();

            // 上传到存储服务
            FileUploadResult result = storageService.upload(file.getInputStream(), fileName, contentType, fileSize, directory);

            // 保存文件信息到数据库
            SysFileInfo fileInfo = new SysFileInfo();
            fileInfo.setFileName(fileName);
            fileInfo.setFileKey(result.getFileKey());
            fileInfo.setFileUrl(result.getFileUrl());
            fileInfo.setFileSize(fileSize);
            fileInfo.setFileType(result.getFileType());
            fileInfo.setMimeType(contentType);
            fileInfo.setFileExt(result.getFileExt());
            fileInfo.setStorageProvider("s3");
            fileInfo.setBucketName(s3Config.getBucket());
            fileInfo.setBusinessType(businessType);
            fileInfo.setBusinessId(businessId);
            fileInfo.setStatus("0");

            // 设置上传用户信息
            try
            {
                fileInfo.setUploadUserId(SecurityUtils.getUserId());
                fileInfo.setUploadUserType("admin");
                fileInfo.setCreateBy(SecurityUtils.getUsername());
            }
            catch (Exception e)
            {
                // 未登录情况下不设置用户信息
                log.debug("无法获取当前用户信息");
            }

            fileInfoService.insertFileInfo(fileInfo);
            result.setFileId(fileInfo.getFileId());

            return result;
        }
        catch (Exception e)
        {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件流
     */
    @Override
    @Transactional
    public FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize)
    {
        return upload(inputStream, fileName, contentType, fileSize, null);
    }

    /**
     * 上传文件流到指定目录
     */
    @Override
    @Transactional
    public FileUploadResult upload(InputStream inputStream, String fileName, String contentType, long fileSize, String directory)
    {
        try
        {
            // 上传到存储服务
            FileUploadResult result = storageService.upload(inputStream, fileName, contentType, fileSize, directory);

            // 保存文件信息到数据库
            SysFileInfo fileInfo = new SysFileInfo();
            fileInfo.setFileName(fileName);
            fileInfo.setFileKey(result.getFileKey());
            fileInfo.setFileUrl(result.getFileUrl());
            fileInfo.setFileSize(fileSize);
            fileInfo.setFileType(result.getFileType());
            fileInfo.setMimeType(contentType);
            fileInfo.setFileExt(result.getFileExt());
            fileInfo.setStorageProvider("s3");
            fileInfo.setBucketName(s3Config.getBucket());
            fileInfo.setStatus("0");

            fileInfoService.insertFileInfo(fileInfo);
            result.setFileId(fileInfo.getFileId());

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
    public InputStream download(Long fileId)
    {
        SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
        if (fileInfo == null)
        {
            throw new RuntimeException("文件不存在");
        }
        return storageService.download(fileInfo.getFileKey());
    }

    /**
     * 通过文件Key下载
     */
    @Override
    public InputStream downloadByKey(String fileKey)
    {
        return storageService.download(fileKey);
    }

    /**
     * 删除文件
     */
    @Override
    @Transactional
    public boolean delete(Long fileId)
    {
        SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
        if (fileInfo == null)
        {
            return false;
        }

        // 从存储服务删除
        boolean deleted = storageService.delete(fileInfo.getFileKey());
        if (deleted)
        {
            // 从数据库删除
            fileInfoService.deleteFileInfoById(fileId);
        }
        return deleted;
    }

    /**
     * 批量删除文件
     */
    @Override
    @Transactional
    public int deleteByIds(Long[] fileIds)
    {
        int count = 0;
        for (Long fileId : fileIds)
        {
            if (delete(fileId))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取文件信息
     */
    @Override
    public SysFileInfo getFileInfo(Long fileId)
    {
        return fileInfoService.selectFileInfoById(fileId);
    }

    /**
     * 获取文件列表
     */
    @Override
    public List<SysFileInfo> selectFileList(SysFileInfo fileInfo)
    {
        return fileInfoService.selectFileInfoList(fileInfo);
    }

    /**
     * 根据业务获取文件列表
     */
    @Override
    public List<SysFileInfo> selectFileByBusiness(String businessType, Long businessId)
    {
        return fileInfoService.selectFileInfoByBusiness(businessType, businessId);
    }

    /**
     * 获取预签名URL
     */
    @Override
    public String getPresignedUrl(Long fileId, int expirationMinutes)
    {
        SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
        if (fileInfo == null)
        {
            throw new RuntimeException("文件不存在");
        }
        return storageService.getPresignedUrl(fileInfo.getFileKey(), expirationMinutes);
    }
}
