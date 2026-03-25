package com.ruoyi.ai.service.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.domain.VideoGenerationResult;
import com.ruoyi.ai.service.IAiVideoService;
import com.ruoyi.ai.service.IVideoProviderService;
import com.ruoyi.ai.service.IVideoProviderService.TaskStatus;
import com.ruoyi.common.event.ai.VideoGenerationCompletedEvent;
import com.ruoyi.common.event.ai.VideoGenerationCreatedEvent;
import com.ruoyi.common.event.ai.VideoGenerationFailedEvent;
import com.ruoyi.common.event.ai.VideoGenerationStatusChangedEvent;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IFileService;
import com.ruoyi.system.domain.AiVideoTask;
import com.ruoyi.system.service.IAiVideoTaskService;

/**
 * AI视频服务实现
 *
 * @author ruoyi
 */
@Service
public class AiVideoServiceImpl implements IAiVideoService
{
    private static final Logger log = LoggerFactory.getLogger(AiVideoServiceImpl.class);

    @Autowired(required = false)
    private List<IVideoProviderService> videoProviderServices;

    @Autowired
    private IAiVideoTaskService videoTaskService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${pet.ai.provider}")
    private String petAiProvider;

    /**
     * 根据提供商名称获取服务
     */
    private IVideoProviderService getProviderService(String providerName)
    {
        if (videoProviderServices == null || videoProviderServices.isEmpty())
        {
            throw new RuntimeException("未找到可用的视频提供商服务");
        }

        // 如果指定了提供商，查找对应服务
        if (StringUtils.isNotEmpty(providerName))
        {
            for (IVideoProviderService service : videoProviderServices)
            {
                if (providerName.equalsIgnoreCase(service.getProviderName()))
                {
                    return service;
                }
            }
            throw new RuntimeException("未找到指定的提供商服务: " + providerName);
        }

        // 默认返回第一个可用服务
        return videoProviderServices.get(0);
    }

    /**
     * 创建视频生成任务
     */
    @Override
    @Transactional
    public VideoGenerationResult createVideoTask(VideoGenerationRequest request, Long userId)
    {
        try
        {
            // 获取视频提供商服务
            IVideoProviderService videoProviderService = getProviderService(petAiProvider);

            // 生成任务UUID
            String taskUuid = UUID.randomUUID().toString();

            // 创建任务记录
            AiVideoTask task = new AiVideoTask();
            task.setTaskUuid(taskUuid);
            task.setProvider(videoProviderService.getProviderName());
            task.setUserId(userId);
            task.setPromptText(request.getPrompt());
            task.setPromptImageUrl(request.getImageUrl());
            task.setPromptImageFileId(request.getFileId());
            task.setFileId(null);
            task.setOssVideoUrl(null);
            task.setModelName(videoProviderService.getModelName());
            task.setVideoDuration(request.getDuration());
            task.setVideoResolution(request.getResolution());
            task.setVideoAspectRatio(request.getAspectRatio());
            task.setStatus("pending");
            task.setProgress(0);
            task.setRetryCount(0);
            task.setMaxRetry(3);

            videoTaskService.insertVideoTask(task);

            // 调用提供商创建任务
            String providerTaskId = videoProviderService.createTask(request);

            // 更新任务状态
            task.setProviderTaskId(providerTaskId);
            task.setStatus("processing");
            task.setStartedAt(new Date());
            videoTaskService.updateVideoTask(task);

            log.info("视频生成任务创建成功: taskId={}, providerTaskId={}", task.getTaskId(), providerTaskId);

            // 发布任务创建事件
            eventPublisher.publishEvent(new VideoGenerationCreatedEvent(
                this,
                task.getTaskId(),
                task.getTaskUuid(),
                task.getUserId(),
                task.getModelName(),
                task.getPromptText(),
                task.getPromptImageUrl(),
                providerTaskId,
                task.getVideoDuration(),
                task.getVideoResolution()
            ));
            log.debug("已发布视频生成任务创建事件: taskId={}", task.getTaskId());

            return VideoGenerationResult.success(task.getTaskId(), taskUuid, providerTaskId);
        }
        catch (Exception e)
        {
            log.error("创建视频生成任务失败: {}", e.getMessage(), e);
            return VideoGenerationResult.error("CREATE_FAILED", e.getMessage());
        }
    }

    /**
     * 查询任务状态
     */
    @Override
    public AiVideoTask queryTaskStatus(Long taskId)
    {
        return videoTaskService.selectVideoTaskById(taskId);
    }

    /**
     * 通过UUID查询任务状态
     */
    @Override
    public AiVideoTask queryTaskByUuid(String taskUuid)
    {
        return videoTaskService.selectVideoTaskByUuid(taskUuid);
    }

    /**
     * 取消任务
     */
    @Override
    @Transactional
    public boolean cancelTask(Long taskId)
    {
        AiVideoTask task = videoTaskService.selectVideoTaskById(taskId);
        if (task == null)
        {
            return false;
        }

        // 如果有提供商任务ID，尝试取消
        if (StringUtils.isNotEmpty(task.getProviderTaskId()))
        {
            try
            {
                IVideoProviderService videoProviderService = getProviderService(task.getProvider());
                videoProviderService.cancelTask(task.getProviderTaskId());
            }
            catch (Exception e)
            {
                log.warn("取消提供商任务失败: {}", e.getMessage());
            }
        }

        // 更新任务状态
        videoTaskService.updateVideoTaskStatus(taskId, "cancelled", task.getProgress());
        log.info("视频生成任务已取消: {}", taskId);
        return true;
    }

    /**
     * 获取用户任务列表
     */
    @Override
    public List<AiVideoTask> getUserTasks(Long userId)
    {
        return videoTaskService.selectVideoTaskByUserId(userId);
    }

    /**
     * 获取任务列表
     */
    @Override
    public List<AiVideoTask> selectTaskList(AiVideoTask task)
    {
        return videoTaskService.selectVideoTaskList(task);
    }

    /**
     * 删除任务
     */
    @Override
    public int deleteTask(Long taskId)
    {
        return videoTaskService.deleteVideoTaskById(taskId);
    }

    /**
     * 批量删除任务
     */
    @Override
    public int deleteTaskByIds(Long[] taskIds)
    {
        return videoTaskService.deleteVideoTaskByIds(taskIds);
    }

    /**
     * 同步任务状态
     */
    @Override
    @Transactional
    public AiVideoTask syncTaskStatus(Long taskId)
    {
        log.info("【syncTaskStatus】进入方法: taskId={}", taskId);

        AiVideoTask task = videoTaskService.selectVideoTaskById(taskId);
        if (task == null)
        {
            log.warn("【syncTaskStatus】任务不存在: taskId={}", taskId);
            return task;
        }

        if (StringUtils.isEmpty(task.getProviderTaskId()))
        {
            log.warn("【syncTaskStatus】任务没有providerTaskId，跳过: taskId={}, status={}", taskId, task.getStatus());
            return task;
        }

        log.info("【syncTaskStatus】查询到任务: taskId={}, status={}, progress={}, providerTaskId={}, hasOssUrl={}, hasProviderUrl={}",
            taskId, task.getStatus(), task.getProgress(), task.getProviderTaskId(),
            (task.getOssVideoUrl() != null && !task.getOssVideoUrl().isEmpty()),
            (task.getProviderVideoUrl() != null && !task.getProviderVideoUrl().isEmpty()));

        // 如果任务已经有 OSS URL，说明已完全处理完毕，不应该再同步
        if (StringUtils.isNotEmpty(task.getOssVideoUrl()))
        {
            log.warn("【syncTaskStatus】任务已有OSS URL，跳过同步: taskId={}, status={}, ossUrl={}",
                taskId, task.getStatus(), task.getOssVideoUrl());
            // 确保状态是 completed
            if (!"completed".equals(task.getStatus()))
            {
                log.error("【syncTaskStatus】数据异常：任务已上传但状态不是completed，修复中: taskId={}, currentStatus={}",
                    taskId, task.getStatus());
                task.setStatus("completed");
                task.setProgress(100);
                videoTaskService.updateVideoTask(task);
            }
            return task;
        }

        // 如果任务已完成或失败，不需要同步
        if ("completed".equals(task.getStatus()) || "failed".equals(task.getStatus()) || "cancelled".equals(task.getStatus()))
        {
            log.warn("【syncTaskStatus】任务已处于终态，跳过同步: taskId={}, status={}, errorCode={}, errorMessage={}",
                taskId, task.getStatus(), task.getErrorCode(), task.getErrorMessage());
            return task;
        }

        try
        {
            // 获取提供商服务
            IVideoProviderService videoProviderService = getProviderService(task.getProvider());

            log.info("开始同步任务状态: taskId={}, provider={}, providerTaskId={}, currentStatus={}",
                taskId, task.getProvider(), task.getProviderTaskId(), task.getStatus());

            String oldStatus = task.getStatus();
            Integer oldProgress = task.getProgress();

            TaskStatus status = videoProviderService.queryTaskStatus(task.getProviderTaskId());

            log.info("查询任务状态返回: taskId={}, newStatus={}, errorCode={}, errorMessage={}",
                taskId, status.getStatus(), status.getErrorCode(), status.getErrorMessage());

            // 如果查询返回 failed，检查是否是暂时性错误
            if ("failed".equals(status.getStatus()))
            {
                String errorCode = status.getErrorCode();
                // 对于某些错误码，不立即标记为失败
                if ("QUERY_ERROR".equals(errorCode) || "INVALID_REQUEST".equals(errorCode))
                {
                    // 增加重试计数
                    if (task.getRetryCount() < task.getMaxRetry())
                    {
                        log.warn("查询任务状态失败，将在下次重试: taskId={}, retryCount={}, error={}",
                            taskId, task.getRetryCount(), status.getErrorMessage());
                        videoTaskService.incrementRetryCount(taskId);
                        return task; // 不更新状态，等待下次重试
                    }
                    else
                    {
                        log.error("查询任务状态失败次数过多，标记为失败: taskId={}, error={}",
                            taskId, status.getErrorMessage());
                        // 继续执行，标记为失败
                    }
                }
            }

            task.setStatus(status.getStatus());
            task.setProgress(status.getProgress());

            if ("completed".equals(status.getStatus()))
            {
                task.setProviderVideoUrl(status.getVideoUrl());
                task.setCompletedAt(new Date());
            }
            else if ("failed".equals(status.getStatus()))
            {
                task.setErrorCode(status.getErrorCode());
                task.setErrorMessage(status.getErrorMessage());
            }

            videoTaskService.updateVideoTask(task);
            log.info("任务状态同步成功: taskId={}, oldStatus={}, newStatus={}, progress={}%",
                taskId, oldStatus, status.getStatus(), status.getProgress());

            // 如果状态发生变化，发布状态变化事件
            if (!oldStatus.equals(status.getStatus()) || !oldProgress.equals(status.getProgress()))
            {
                eventPublisher.publishEvent(new VideoGenerationStatusChangedEvent(
                    this,
                    task.getTaskId(),
                    task.getTaskUuid(),
                    task.getUserId(),
                    task.getModelName(),
                    oldStatus,
                    status.getStatus(),
                    status.getProgress(),
                    null  // 预计剩余时间暂时为null
                ));
                log.debug("已发布状态变化事件: taskId={}, {} -> {}, progress={}%",
                    taskId, oldStatus, status.getStatus(), status.getProgress());
            }

            // 如果任务失败，发布失败事件
            if ("failed".equals(status.getStatus()) && !"failed".equals(oldStatus))
            {
                eventPublisher.publishEvent(new VideoGenerationFailedEvent(
                    this,
                    task.getTaskId(),
                    task.getTaskUuid(),
                    task.getUserId(),
                    task.getModelName(),
                    task.getErrorCode(),
                    task.getErrorMessage(),
                    task.getRetryCount(),
                    task.getMaxRetry(),
                    task.getPromptText()
                ));
                log.info("已发布视频生成失败事件: taskId={}, error={}", taskId, task.getErrorMessage());
            }
        }
        catch (Exception e)
        {
            log.error("同步任务状态失败: taskId={}, error={}", taskId, e.getMessage());
        }

        return task;
    }

    /**
     * 处理完成的任务
     * 注意：此方法不添加 @Transactional，避免上传失败时回滚任务的 completed 状态
     */
    @Override
    public boolean processCompletedTask(Long taskId)
    {
        AiVideoTask task = videoTaskService.selectVideoTaskById(taskId);
        if (task == null)
        {
            log.error("任务不存在: taskId={}", taskId);
            return false;
        }

        if (!"completed".equals(task.getStatus()))
        {
            log.warn("任务状态不是completed: taskId={}, status={}", taskId, task.getStatus());
            return false;
        }

        if (StringUtils.isEmpty(task.getProviderVideoUrl()))
        {
            log.error("任务没有视频URL: taskId={}, status={}", taskId, task.getStatus());
            return false;
        }

        // 如果已经上传过，跳过
        if (StringUtils.isNotEmpty(task.getOssVideoUrl()))
        {
            log.info("任务已上传过，跳过: taskId={}", taskId);
            return true;
        }

        Path tempVideoFile = null;
        try
        {
            // 下载视频到临时文件（流式写入，不占用堆内存）
            log.info("【视频处理】开始下载视频: taskId={}, url={}", taskId, task.getProviderVideoUrl());
            URL videoUrl = new URL(task.getProviderVideoUrl());

            tempVideoFile = Files.createTempFile("video_" + task.getTaskUuid(), ".mp4");
            try (InputStream inputStream = videoUrl.openStream();
                 OutputStream outputStream = Files.newOutputStream(tempVideoFile))
            {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            long fileSize = Files.size(tempVideoFile);
            log.info("视频下载完成: taskId={}, size={} bytes", taskId, fileSize);

            // 从临时文件流式上传到OSS
            String fileName = "video_" + task.getTaskUuid() + ".mp4";
            try (InputStream uploadStream = Files.newInputStream(tempVideoFile))
            {
                FileUploadResult uploadResult = fileService.upload(
                    uploadStream,
                    fileName,
                    "video/mp4",
                    fileSize,
                    "ai-videos"
                );

                task.setOssVideoUrl(uploadResult.getFileUrl());
                task.setFileId(uploadResult.getFileId());

                log.info("视频上传成功: taskId={}, fileId={}, size={} bytes",
                    taskId, uploadResult.getFileId(), fileSize);
            }

            // 从同一临时文件提取首帧（复用，无需再写一次）
            extractAndUploadFirstFrame(task, tempVideoFile);

            videoTaskService.updateVideoTask(task);

            // 发布任务完成事件
            eventPublisher.publishEvent(new VideoGenerationCompletedEvent(
                this,
                task.getTaskId(),
                task.getTaskUuid(),
                task.getUserId(),
                task.getModelName(),
                task.getProviderVideoUrl(),
                task.getOssVideoUrl(),
                task.getFileId(),
                task.getVideoDuration(),
                task.getVideoResolution(),
                fileSize
            ));
            log.info("已发布视频生成完成事件: taskId={}, fileId={}, size={} bytes",
                taskId, task.getFileId(), fileSize);

            return true;
        }
        catch (Exception e)
        {
            log.error("处理完成任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return false;
        }
        finally
        {
            // 清理视频临时文件
            try
            {
                if (tempVideoFile != null) Files.deleteIfExists(tempVideoFile);
            }
            catch (Exception e)
            {
                log.debug("清理视频临时文件失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 使用FFmpeg从临时视频文件提取首帧并上传到OSS
     * 如果FFmpeg不可用或提取失败，不影响主流程
     */
    private void extractAndUploadFirstFrame(AiVideoTask task, Path videoFile)
    {
        Path tempImageFile = null;
        try
        {
            tempImageFile = Files.createTempFile("thumb_" + task.getTaskUuid(), ".jpg");

            // 调用FFmpeg提取首帧
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", videoFile.toAbsolutePath().toString(),
                "-vframes", "1",
                "-q:v", "2",
                tempImageFile.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 消费输出流防止阻塞
            try (InputStream is = process.getInputStream())
            {
                byte[] buf = new byte[1024];
                while (is.read(buf) != -1) { }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0)
            {
                log.warn("FFmpeg首帧提取失败，exitCode={}, taskId={}", exitCode, task.getTaskId());
                return;
            }

            // 流式上传首帧图片到OSS
            long imageSize = Files.size(tempImageFile);
            if (imageSize == 0)
            {
                log.warn("FFmpeg生成的首帧图片为空: taskId={}", task.getTaskId());
                return;
            }

            String imageFileName = "thumb_" + task.getTaskUuid() + ".jpg";
            try (InputStream imageStream = Files.newInputStream(tempImageFile))
            {
                FileUploadResult imageResult = fileService.upload(
                    imageStream,
                    imageFileName,
                    "image/jpeg",
                    imageSize,
                    "ai-thumbnails"
                );

                task.setVideoPicFileId(imageResult.getFileId());
                task.setVideoPicUrl(imageResult.getFileUrl());

                log.info("视频首帧提取并上传成功: taskId={}, picFileId={}, size={} bytes",
                    task.getTaskId(), imageResult.getFileId(), imageSize);
            }
        }
        catch (Exception e)
        {
            log.warn("视频首帧提取失败（不影响主流程）: taskId={}, error={}", task.getTaskId(), e.getMessage());
        }
        finally
        {
            // 清理首帧临时文件
            try
            {
                if (tempImageFile != null) Files.deleteIfExists(tempImageFile);
            }
            catch (Exception e)
            {
                log.debug("清理首帧临时文件失败: {}", e.getMessage());
            }
        }
    }
}
