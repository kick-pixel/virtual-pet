package com.ruoyi.ai.service.impl;

import com.ruoyi.ai.config.ArkConfig;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.service.IVideoProviderService;
import com.ruoyi.common.utils.StringUtils;
import com.volcengine.ark.runtime.model.content.generation.*;
import com.volcengine.ark.runtime.model.content.generation.CreateContentGenerationTaskRequest.Content;
import com.volcengine.ark.runtime.model.content.generation.CreateContentGenerationTaskRequest.ImageUrl;
import com.volcengine.ark.runtime.service.ArkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * 火山引擎 Ark 视频提供商服务实现
 * 支持 Seedance 系列模型（1.5 pro, 1.0 pro, 1.0 pro fast, 1.0 lite）
 *
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "pet.ai.ark", name = "api-key")
public class ArkVideoProviderServiceImpl implements IVideoProviderService
{
    private static final Logger log = LoggerFactory.getLogger(ArkVideoProviderServiceImpl.class);

    private static final String PROVIDER_NAME = "ark";

    @Autowired
    private ArkConfig arkConfig;

    private ArkService arkService;

    @PostConstruct
    public void init()
    {
        log.info("初始化火山引擎 Ark 服务，Base URL: {}", arkConfig.getBaseUrl());
        this.arkService = ArkService.builder()
            .apiKey(arkConfig.getApiKey())
            .baseUrl(arkConfig.getBaseUrl())
            .build();
    }

    @Override
    public String getModelName() {
        return arkConfig.getDefaultVideoModel();
    }

    @PreDestroy
    public void destroy()
    {
        if (arkService != null)
        {
            log.info("关闭火山引擎 Ark 服务");
            arkService.shutdownExecutor();
        }
    }

    @Override
    public String getProviderName()
    {
        return PROVIDER_NAME;
    }

    @Override
    public String createTask(VideoGenerationRequest request)
    {
        try
        {
            String model = StringUtils.isNotEmpty(request.getModel())
                ? request.getModel()
                : arkConfig.getDefaultVideoModel();

            log.info("创建火山引擎视频生成任务，模型: {}, 提示词: {}", model, request.getPrompt());

            // 构建内容列表
            List<Content> contents = new ArrayList<>();

            // 添加文本提示词
            if (StringUtils.isNotEmpty(request.getPrompt()))
            {
                contents.add(Content.builder()
                    .type("text")
                    .text(request.getPrompt())
                    .build());
            }

            // 添加首帧图片（图生视频）
            if (StringUtils.isNotEmpty(request.getImageUrl()))
            {
                contents.add(Content.builder()
                    .type("image_url")
                    .imageUrl(ImageUrl.builder()
                        .url(request.getImageUrl())
                        .build())
                    .build());
            }

            // 添加尾帧图片（首尾帧生视频）
            if (StringUtils.isNotEmpty(request.getEndFrameImageUrl()))
            {
                contents.add(Content.builder()
                    .type("image_url")
                    .imageUrl(ImageUrl.builder()
                        .url(request.getEndFrameImageUrl())
                        .build())
                    .build());
            }

            // 添加多参考图（仅 1.0 lite-i2v 支持）
            if (request.getReferenceImageUrls() != null && !request.getReferenceImageUrls().isEmpty())
            {
                for (String imageUrl : request.getReferenceImageUrls())
                {
                    if (StringUtils.isNotEmpty(imageUrl))
                    {
                        contents.add(Content.builder()
                            .type("image_url")
                            .imageUrl(ImageUrl.builder()
                                .url(imageUrl)
                                .build())
                            .build());
                    }
                }
            }

            // 构建请求
            CreateContentGenerationTaskRequest.Builder builder = CreateContentGenerationTaskRequest.builder()
                .model(model)
                .content(contents)
                .duration(request.getDuration().longValue())
                .watermark(request.getWatermark() != null ? request.getWatermark() : arkConfig.getWatermarkEnabled());

            // 设置视频比例（ratio 优先于 aspectRatio）
            if (StringUtils.isNotEmpty(request.getRatio()))
            {
                builder.ratio(request.getRatio());
            }
            else if (StringUtils.isNotEmpty(request.getAspectRatio()))
            {
                builder.ratio(request.getAspectRatio());
            }

            // 设置音频生成（仅 1.5 pro 支持）
            if (request.getGenerateAudio() != null && request.getGenerateAudio())
            {
                builder.generateAudio(true);
            }
            else if (arkConfig.getAudioEnabled())
            {
                builder.generateAudio(true);
            }

            CreateContentGenerationTaskRequest createRequest = builder.build();

            // 调用 API 创建任务
            CreateContentGenerationTaskResult result = arkService.createContentGenerationTask(createRequest);

            String taskId = result.getId();
            log.info("火山引擎视频生成任务创建成功，任务ID: {}", taskId);

            return taskId;
        }
        catch (Exception e)
        {
            log.error("创建火山引擎视频生成任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建视频生成任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskStatus queryTaskStatus(String providerTaskId)
    {
        try
        {
            log.debug("开始查询火山引擎任务状态，taskId: {}", providerTaskId);

            GetContentGenerationTaskRequest request = GetContentGenerationTaskRequest.builder()
                .taskId(providerTaskId)
                .build();

            GetContentGenerationTaskResponse response = arkService.getContentGenerationTask(request);

            String status = response.getStatus();
            log.debug("查询火山引擎任务状态成功: taskId={}, status={}", providerTaskId, status);

            switch (status)
            {
                case "queued":
                case "pending":
                    return new TaskStatus("pending", 0);

                case "running":
                case "processing":
                    // 火山引擎 API 可能不返回具体进度，使用估算值
                    return TaskStatus.processing(50);

                case "succeeded":
                case "completed":
                    String videoUrl = null;
                    if (response.getContent() != null)
                    {
                        videoUrl = response.getContent().getVideoUrl();
                    }
                    if (StringUtils.isEmpty(videoUrl))
                    {
                        log.error("任务成功但未获取到视频URL: {}", providerTaskId);
                        return TaskStatus.failed("NO_VIDEO_URL", "任务完成但未获取到视频URL");
                    }
                    log.info("火山引擎视频生成任务成功: {}, URL: {}", providerTaskId, videoUrl);
                    return TaskStatus.completed(videoUrl);

                case "failed":
                case "error":
                    String errorCode = "UNKNOWN";
                    String errorMessage = "未知错误";
                    if (response.getError() != null)
                    {
                        errorCode = response.getError().getCode();
                        errorMessage = response.getError().getMessage();
                    }
                    log.error("火山引擎视频生成任务失败: {}, 错误: {} - {}", providerTaskId, errorCode, errorMessage);
                    return TaskStatus.failed(errorCode, errorMessage);

                case "cancelled":
                    return TaskStatus.failed("CANCELLED", "任务已取消");

                default:
                    log.warn("未知的任务状态: {}", status);
                    return new TaskStatus("pending", 0);
            }
        }
        catch (Exception e)
        {
            // 记录详细的错误信息
            String errorMsg = e.getMessage();
            String errorDetail = String.format("查询任务状态失败: taskId=%s, error=%s", providerTaskId, errorMsg);

            log.error("查询火山引擎任务状态异常: taskId={}, errorType={}, message={}",
                providerTaskId, e.getClass().getSimpleName(), errorMsg, e);

            // 检查是否是认证错误
            if (errorMsg != null && (errorMsg.contains("401") || errorMsg.contains("403") || errorMsg.contains("Unauthorized")))
            {
                return TaskStatus.failed("AUTH_ERROR", "API Key 认证失败，请检查配置");
            }

            // 检查是否是 400 错误
            if (errorMsg != null && errorMsg.contains("400"))
            {
                return TaskStatus.failed("INVALID_REQUEST", "请求参数错误，可能是任务ID不存在或格式错误: " + errorMsg);
            }

            return TaskStatus.failed("QUERY_ERROR", errorDetail);
        }
    }

    @Override
    public boolean cancelTask(String providerTaskId)
    {
        log.warn("火山引擎 Ark API 暂不支持取消任务: {}", providerTaskId);
        return false;
    }
}
