package com.ruoyi.ai.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.ai.config.GrokConfig;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.service.IVideoProviderService;
import com.ruoyi.common.utils.StringUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * xAI Grok Imagine 视频提供商服务实现
 * 支持 Grok Imagine 1.0 视频生成模型
 *
 * API 文档: https://docs.x.ai/docs/api-reference
 *
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "pet.ai.grok", name = "api-key")
public class GrokVideoProviderServiceImpl implements IVideoProviderService
{
    private static final Logger log = LoggerFactory.getLogger(GrokVideoProviderServiceImpl.class);

    private static final String PROVIDER_NAME = "grok";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Autowired
    private GrokConfig grokConfig;

    @Autowired
    private OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 专门用于 xAI Grok 的 Client，强制使用 HTTP/1.1，避免 HTTP/2 指纹特征
     */
    private OkHttpClient getGrokClient() {
        return okHttpClient.newBuilder()
            .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
            .build();
    }

    @Override
    public String getProviderName()
    {
        return PROVIDER_NAME;
    }

    @Override
    public String getModelName()
    {
        return grokConfig.getDefaultVideoModel();
    }

    @Override
    public String createTask(VideoGenerationRequest request)
    {
        try
        {
            String model = StringUtils.isNotEmpty(request.getModel())
                ? request.getModel()
                : grokConfig.getDefaultVideoModel();

            log.info("创建 xAI Grok 视频生成任务，模型: {}, 提示词: {}", model, request.getPrompt());

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("prompt", request.getPrompt());

            // 图生视频：官方 HTTP API 使用嵌套对象 {"image": {"url": "..."}}
            // 注意：image_url 是 xAI SDK 的封装参数，原始 HTTP 请求必须用嵌套格式
            if (StringUtils.isNotEmpty(request.getImageUrl()))
            {
                ObjectNode imageNode = objectMapper.createObjectNode();
                imageNode.put("url", request.getImageUrl());
                requestBody.set("image", imageNode);
                log.info("图生视频模式，图片URL: {}", request.getImageUrl());
            }

            if (request.getDuration() != null)
            {
                requestBody.put("duration", request.getDuration());
            }

            if (StringUtils.isNotEmpty(request.getResolution()))
            {
                requestBody.put("resolution", request.getResolution());
            }

            if (StringUtils.isNotEmpty(request.getAspectRatio()))
            {
                requestBody.put("aspect_ratio", request.getAspectRatio());
            }

            String url = grokConfig.getBaseUrl() + "/videos/generations";

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + grokConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Mimic browser
                .addHeader("Referer", "https://grok.x.ai/")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON))
                .build();

            try (Response response = getGrokClient().newCall(httpRequest).execute())
            {
                String responseBody = response.body() != null ? response.body().string() : "";

                log.info("xAI Grok API响应: status={}, body前200字符={}",
                    response.code(),
                    responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);

                if (!response.isSuccessful())
                {
                    log.error("xAI Grok API调用失败: {} - {}", response.code(), responseBody);
                    throw new RuntimeException("xAI Grok API调用失败: " + response.code());
                }

                if (responseBody.trim().startsWith("<"))
                {
                    log.error("API返回了HTML而不是JSON，可能是URL错误或API不可用。URL: {}, Response: {}",
                        url, responseBody.substring(0, Math.min(500, responseBody.length())));
                    throw new RuntimeException("API端点返回了HTML页面，请检查base-url配置和API密钥是否正确");
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String taskId = null;

                // 优先检查 request_id (官方文档格式)
                if (jsonResponse.has("request_id"))
                {
                    taskId = jsonResponse.get("request_id").asText();
                }
                // 其次检查 id (兼容性)
                else if (jsonResponse.has("id"))
                {
                    taskId = jsonResponse.get("id").asText();
                }

                if (StringUtils.isEmpty(taskId))
                {
                    log.error("API响应中未找到request_id: {}", responseBody);
                    throw new RuntimeException("未获取到任务ID");
                }

                log.info("xAI Grok 视频生成任务创建成功: {}", taskId);
                return taskId;
            }
        }
        catch (Exception e)
        {
            log.error("创建 xAI Grok 视频生成任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建视频生成任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskStatus queryTaskStatus(String providerTaskId)
    {
        try
        {
            String url = grokConfig.getBaseUrl() + "/videos/" + providerTaskId;

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + grokConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Mimic browser
                .get()
                .build();

            try (Response response = getGrokClient().newCall(httpRequest).execute())
            {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful())
                {
                    log.error("查询 xAI Grok 任务状态失败: {} - {}", response.code(), responseBody);
                    if (response.code() == 403)
                    {
                        return TaskStatus.processing(0);
                    }
                    else
                    {
                        return TaskStatus.failed("API_ERROR", "查询任务状态失败: " + response.code());
                    }

                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                log.info("xAI Grok 任务状态查询成功: {}", jsonResponse.toPrettyString());
                // 转小写统一处理，API 实际返回 "done"/"pending"/"expired"（可能大写）
                String status = jsonResponse.has("status")
                    ? jsonResponse.get("status").asText().toLowerCase()
                    : "unknown";

                switch (status)
                {
                    case "pending":
                    case "queued":
                    case "processing":
                    case "in_progress":
                    case "running":
                        int progress = jsonResponse.has("progress")
                            ? jsonResponse.get("progress").asInt()
                            : 50;
                        return TaskStatus.processing(progress);

                    // xAI Grok 官方状态为 "done"，兼容其他格式
                    case "done":
                    case "completed":
                    case "succeeded":
                    case "success":
                        String videoUrl = null;

                        // 官方格式：{ "video": { "url": "..." } }
                        if (jsonResponse.has("video"))
                        {
                            JsonNode videoNode = jsonResponse.get("video");
                            if (videoNode.has("url"))
                            {
                                videoUrl = videoNode.get("url").asText();
                            }
                        }
                        // 兼容其他格式
                        if (StringUtils.isEmpty(videoUrl) && jsonResponse.has("video_url"))
                        {
                            videoUrl = jsonResponse.get("video_url").asText();
                        }
                        if (StringUtils.isEmpty(videoUrl) && jsonResponse.has("output"))
                        {
                            JsonNode output = jsonResponse.get("output");
                            if (output.isTextual())
                            {
                                videoUrl = output.asText();
                            }
                            else if (output.has("video_url"))
                            {
                                videoUrl = output.get("video_url").asText();
                            }
                            else if (output.has("url"))
                            {
                                videoUrl = output.get("url").asText();
                            }
                        }
                        if (StringUtils.isEmpty(videoUrl) && jsonResponse.has("url"))
                        {
                            videoUrl = jsonResponse.get("url").asText();
                        }

                        if (StringUtils.isEmpty(videoUrl))
                        {
                            log.warn("任务完成但未找到视频URL: {}", responseBody);
                            return TaskStatus.failed("NO_VIDEO_URL", "任务完成但未获取到视频URL");
                        }

                        return TaskStatus.completed(videoUrl);

                    // xAI Grok 特有：任务过期
                    case "expired":
                        log.warn("xAI Grok 任务已过期: {}", providerTaskId);
                        return TaskStatus.failed("EXPIRED", "视频生成任务已过期，请重新提交");

                    case "failed":
                    case "error":
                        String errorCode = "UNKNOWN";
                        String errorMessage = "未知错误";

                        if (jsonResponse.has("error_code"))
                        {
                            errorCode = jsonResponse.get("error_code").asText();
                        }
                        if (jsonResponse.has("error_message"))
                        {
                            errorMessage = jsonResponse.get("error_message").asText();
                        }
                        if (jsonResponse.has("error"))
                        {
                            JsonNode errorNode = jsonResponse.get("error");
                            if (errorNode.isObject())
                            {
                                if (errorNode.has("code"))
                                {
                                    errorCode = errorNode.get("code").asText();
                                }
                                if (errorNode.has("message"))
                                {
                                    errorMessage = errorNode.get("message").asText();
                                }
                            }
                            else if (errorNode.isTextual())
                            {
                                errorMessage = errorNode.asText();
                            }
                        }

                        return TaskStatus.failed(errorCode, errorMessage);

                    case "cancelled":
                        return TaskStatus.failed("CANCELLED", "任务已取消");

                    default:
                        log.warn("未知的任务状态: {}", status);
                        return new TaskStatus("failed", 0);
                }
            }
        }
        catch (Exception e)
        {
            log.error("查询 xAI Grok 任务状态异常: {}", e.getMessage(), e);
            return TaskStatus.failed("QUERY_ERROR", e.getMessage());
        }
    }

    @Override
    public boolean cancelTask(String providerTaskId)
    {
        try
        {
            String url = grokConfig.getBaseUrl() + "/videos/" + providerTaskId + "/cancel";

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + grokConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // Mimic browser
                .post(RequestBody.create("", JSON))
                .build();

            try (Response response = getGrokClient().newCall(httpRequest).execute())
            {
                if (response.isSuccessful())
                {
                    log.info("xAI Grok 任务取消成功: {}", providerTaskId);
                    return true;
                }
                else
                {
                    log.error("取消 xAI Grok 任务失败: {}", response.code());
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            log.error("取消 xAI Grok 任务异常: {}", e.getMessage(), e);
            return false;
        }
    }
}
