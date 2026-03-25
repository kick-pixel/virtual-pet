package com.ruoyi.web.controller.test;

import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IFileService;
import com.ruoyi.system.domain.SysFileInfo;
import com.ruoyi.system.service.ISysFileInfoService;

/**
 * 文件管理测试Controller
 *
 * 提供文件上传、下载、删除等功能的测试接口
 * 注意：此Controller仅用于开发测试，生产环境应移除或限制访问权限
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/test/file")
public class FileTestController extends BaseController
{
    @Autowired
    private IFileService fileService;

    @Autowired
    private ISysFileInfoService fileInfoService;

    /**
     * 测试文件上传
     *
     * @param file 文件
     * @param folder 文件夹（可选）
     * @return 上传结果
     */
    @PostMapping("/upload")
    public AjaxResult testUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "test") String folder)
    {
        try
        {
            if (file.isEmpty())
            {
                return error("上传文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            long size = file.getSize();

            logger.info("测试文件上传: filename={}, contentType={}, size={}, folder={}",
                    originalFilename, contentType, size, folder);

            try (InputStream inputStream = file.getInputStream())
            {
                FileUploadResult result = fileService.upload(
                        inputStream,
                        originalFilename,
                        contentType,
                        size,
                        folder
                );

                logger.info("文件上传成功: fileId={}, url={}", result.getFileId(), result.getFileUrl());

                return AjaxResult.success("文件上传成功", result);
            }
        }
        catch (Exception e)
        {
            logger.error("文件上传失败", e);
            return error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 测试文件下载
     *
     * @param fileId 文件ID
     * @return 下载URL或错误信息
     */
    @GetMapping("/download/{fileId}")
    public AjaxResult testDownload(@PathVariable Long fileId)
    {
        try
        {
            SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
            if (fileInfo == null)
            {
                return error("文件不存在");
            }

            String downloadUrl = fileService.getPresignedUrl(fileId, 60);
            logger.info("获取文件下载链接: fileId={}, url={}", fileId, downloadUrl);

            return AjaxResult.success("获取下载链接成功", AjaxResult.success()
                    .put("fileId", fileId)
                    .put("fileName", fileInfo.getFileName())
                    .put("downloadUrl", downloadUrl)
                    .put("fileSize", fileInfo.getFileSize())
                    .put("mimeType", fileInfo.getMimeType())
            );
        }
        catch (Exception e)
        {
            logger.error("获取下载链接失败: fileId={}", fileId, e);
            return error("获取下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 测试文件删除
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    public AjaxResult testDelete(@PathVariable Long fileId)
    {
        try
        {
            SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
            if (fileInfo == null)
            {
                return error("文件不存在");
            }

            logger.info("测试文件删除: fileId={}, fileKey={}", fileId, fileInfo.getFileKey());

            // 从存储中删除
            fileService.delete(fileId);

            // 从数据库删除
            fileInfoService.deleteFileInfoById(fileId);

            logger.info("文件删除成功: fileId={}", fileId);

            return success("文件删除成功");
        }
        catch (Exception e)
        {
            logger.error("文件删除失败: fileId={}", fileId, e);
            return error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 测试批量上传
     *
     * @param files 文件数组
     * @param folder 文件夹（可选）
     * @return 上传结果
     */
    @PostMapping("/batchUpload")
    public AjaxResult testBatchUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false, defaultValue = "test") String folder)
    {
        try
        {
            if (files == null || files.length == 0)
            {
                return error("上传文件不能为空");
            }

            logger.info("测试批量文件上传: count={}, folder={}", files.length, folder);

            AjaxResult result = AjaxResult.success("批量上传成功");
            for (int i = 0; i < files.length; i++)
            {
                MultipartFile file = files[i];
                try (InputStream inputStream = file.getInputStream())
                {
                    FileUploadResult uploadResult = fileService.upload(
                            inputStream,
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getSize(),
                            folder
                    );
                    result.put("file_" + i, uploadResult);
                    logger.info("文件 {} 上传成功: fileId={}", i, uploadResult.getFileId());
                }
            }

            return result;
        }
        catch (Exception e)
        {
            logger.error("批量文件上传失败", e);
            return error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/info/{fileId}")
    public AjaxResult getFileInfo(@PathVariable Long fileId)
    {
        try
        {
            SysFileInfo fileInfo = fileInfoService.selectFileInfoById(fileId);
            if (fileInfo == null)
            {
                return error("文件不存在");
            }

            return AjaxResult.success("获取文件信息成功", fileInfo);
        }
        catch (Exception e)
        {
            logger.error("获取文件信息失败: fileId={}", fileId, e);
            return error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 测试存储服务健康检查
     *
     * @return 健康检查结果
     */
    @GetMapping("/health")
    public AjaxResult testHealth()
    {
        try
        {
            // 简单测试：尝试获取文件列表来验证服务可用
            fileService.selectFileList(new SysFileInfo());
            return success("存储服务正常");
        }
        catch (Exception e)
        {
            logger.error("存储服务健康检查失败", e);
            return error("存储服务健康检查失败: " + e.getMessage());
        }
    }
}
