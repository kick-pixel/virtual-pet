package com.ruoyi.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ai.config.OpenAiConfig;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.service.IVideoProviderService;
import com.ruoyi.common.utils.StringUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OpenAI视频提供商服务实现
 *
 * 注意：此实现基于OpenAI Sora API的预期接口，实际API可能有所不同
 *
 * @author ruoyi
 */
@Service
public class OpenAiVideoProviderServiceImpl implements IVideoProviderService
{
    private static final Logger log = LoggerFactory.getLogger(OpenAiVideoProviderServiceImpl.class);

    private static final String PROVIDER_NAME = "openai";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Autowired
    private OpenAiConfig openAiConfig;

    @Autowired
    private OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getProviderName()
    {
        return PROVIDER_NAME;
    }

    @Override
    public String getModelName() {
        return openAiConfig.getDefaultVideoModel();
    }

    @Override
    public String createTask(VideoGenerationRequest request)
    {
        try
        {
            String model = StringUtils.isNotEmpty(request.getModel())
                ? request.getModel()
                : openAiConfig.getDefaultVideoModel();

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("prompt", request.getPrompt());

            if (StringUtils.isNotEmpty(request.getImageUrl()))
            {
                requestBody.put("image_url", request.getImageUrl());
            }

            // 视频参数
            ObjectNode videoParams = objectMapper.createObjectNode();
            videoParams.put("duration", request.getDuration());
            videoParams.put("resolution", request.getResolution());
            videoParams.put("aspect_ratio", request.getAspectRatio());
            requestBody.set("video_params", videoParams);

            String url = openAiConfig.getBaseUrl() + "/video/generations";

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + openAiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON))
                .build();

            try (Response response = okHttpClient.newCall(httpRequest).execute())
            {
                String responseBody = response.body() != null ? response.body().string() : "";

                log.info("OpenAI API响应: status={}, body前200字符={}",
                    response.code(),
                    responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);

                if (!response.isSuccessful())
                {
                    log.error("OpenAI API调用失败: {} - {}", response.code(), responseBody);
                    throw new RuntimeException("OpenAI API调用失败: " + response.code());
                }

                // 检查响应是否为 HTML（错误页面）
                if (responseBody.trim().startsWith("<"))
                {
                    log.error("API返回了HTML而不是JSON，可能是URL错误或API不可用。URL: {}, Response: {}",
                        url, responseBody.substring(0, Math.min(500, responseBody.length())));
                    throw new RuntimeException("API端点返回了HTML页面，请检查base-url配置和API密钥是否正确");
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String taskId = jsonResponse.has("id") ? jsonResponse.get("id").asText() : null;

                if (StringUtils.isEmpty(taskId))
                {
                    throw new RuntimeException("未获取到任务ID");
                }

                log.info("OpenAI视频生成任务创建成功: {}", taskId);
                return taskId;
            }
        }
        catch (Exception e)
        {
            log.error("创建OpenAI视频生成任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建视频生成任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskStatus queryTaskStatus(String providerTaskId)
    {
        try
        {
            String url = openAiConfig.getBaseUrl() + "/video/generations/" + providerTaskId;

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + openAiConfig.getApiKey())
                .get()
                .build();

            try (Response response = okHttpClient.newCall(httpRequest).execute())
            {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful())
                {
                    log.error("查询OpenAI任务状态失败: {} - {}", response.code(), responseBody);
                    return TaskStatus.failed("API_ERROR", "查询任务状态失败: " + response.code());
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String status = jsonResponse.has("status") ? jsonResponse.get("status").asText() : "unknown";

                switch (status)
                {
                    case "pending":
                    case "queued":
                        return new TaskStatus("pending", 0);

                    case "processing":
                    case "in_progress":
                        int progress = jsonResponse.has("progress")
                            ? jsonResponse.get("progress").asInt()
                            : 50;
                        return TaskStatus.processing(progress);

                    case "completed":
                    case "succeeded":
                        String videoUrl = jsonResponse.has("video_url")
                            ? jsonResponse.get("video_url").asText()
                            : null;
                        if (StringUtils.isEmpty(videoUrl) && jsonResponse.has("output"))
                        {
                            JsonNode output = jsonResponse.get("output");
                            if (output.has("video_url"))
                            {
                                videoUrl = output.get("video_url").asText();
                            }
                        }
                        return TaskStatus.completed(videoUrl);

                    case "failed":
                    case "error":
                        String errorCode = jsonResponse.has("error_code")
                            ? jsonResponse.get("error_code").asText()
                            : "UNKNOWN";
                        String errorMessage = jsonResponse.has("error_message")
                            ? jsonResponse.get("error_message").asText()
                            : "未知错误";
                        if (jsonResponse.has("error"))
                        {
                            JsonNode errorNode = jsonResponse.get("error");
                            if (errorNode.has("code"))
                            {
                                errorCode = errorNode.get("code").asText();
                            }
                            if (errorNode.has("message"))
                            {
                                errorMessage = errorNode.get("message").asText();
                            }
                        }
                        return TaskStatus.failed(errorCode, errorMessage);

                    case "cancelled":
                        return TaskStatus.failed("CANCELLED", "任务已取消");

                    default:
                        return new TaskStatus("pending", 0);
                }
            }
        }
        catch (Exception e)
        {
            log.error("查询OpenAI任务状态异常: {}", e.getMessage(), e);
            return TaskStatus.failed("QUERY_ERROR", e.getMessage());
        }
    }

    @Override
    public boolean cancelTask(String providerTaskId)
    {
        try
        {
            String url = openAiConfig.getBaseUrl() + "/video/generations/" + providerTaskId + "/cancel";

            Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + openAiConfig.getApiKey())
                .post(RequestBody.create("", JSON))
                .build();

            try (Response response = okHttpClient.newCall(httpRequest).execute())
            {
                if (response.isSuccessful())
                {
                    log.info("OpenAI任务取消成功: {}", providerTaskId);
                    return true;
                }
                else
                {
                    log.error("取消OpenAI任务失败: {}", response.code());
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            log.error("取消OpenAI任务异常: {}", e.getMessage(), e);
            return false;
        }
    }
}
