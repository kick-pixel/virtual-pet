package com.ruoyi.web.controller.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.domain.VideoGenerationResult;
import com.ruoyi.ai.service.IAiVideoService;
import com.ruoyi.ai.service.IVideoProviderService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.AiVideoTask;

/**
 * AI视频生成测试Controller
 *
 * 提供AI视频生成功能的测试接口
 * 注意：此Controller仅用于开发测试，生产环境应移除或限制访问权限
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/test/ai")
public class AiTestController extends BaseController
{
    @Autowired
    private IAiVideoService aiVideoService;

    @Autowired(required = false)
    private List<IVideoProviderService> videoProviderServices;

    /**
     * 测试创建视频生成任务（文本提示词）
     *
     * @param request 视频生成请求
     * @return 任务创建结果
     */
    @PostMapping("/createTask")
    public AjaxResult testCreateTask(@RequestBody VideoGenerationRequest request)
    {
        try
        {
            Long userId = SecurityUtils.getUserId();
            logger.info("测试创建视频生成任务: userId={}, prompt={}, model={}",
                    userId, request.getPrompt(), request.getModel());

            VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);

            if (!"failed".equals(result.getStatus()))
            {
                logger.info("视频生成任务创建成功: taskId={}, taskUuid={}",
                        result.getTaskId(), result.getTaskUuid());
                return AjaxResult.success("任务创建成功", result);
            }
            else
            {
                logger.error("视频生成任务创建失败: {}", result.getErrorMessage());
                return error("任务创建失败: " + result.getErrorMessage());
            }
        }
        catch (Exception e)
        {
            logger.error("创建视频生成任务失败", e);
            return error("创建视频生成任务失败: " + e.getMessage());
        }
    }

    /**
     * 测试创建视频生成任务（图片+提示词）
     *
     * @param request 视频生成请求（包含imageUrl）
     * @return 任务创建结果
     */
    @PostMapping("/createTaskWithImage")
    public AjaxResult testCreateTaskWithImage(@RequestBody VideoGenerationRequest request)
    {
        try
        {
            if (request.getImageUrl() == null || request.getImageUrl().isEmpty())
            {
                return error("图片URL不能为空");
            }

            Long userId = SecurityUtils.getUserId();
            logger.info("测试创建视频生成任务(图片): userId={}, imageUrl={}, prompt={}",
                    userId, request.getImageUrl(), request.getPrompt());

            VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);

            if (!"failed".equals(result.getStatus()))
            {
                logger.info("视频生成任务创建成功: taskId={}, taskUuid={}",
                        result.getTaskId(), result.getTaskUuid());
                return AjaxResult.success("任务创建成功", result);
            }
            else
            {
                logger.error("视频生成任务创建失败: {}", result.getErrorMessage());
                return error("任务创建失败: " + result.getErrorMessage());
            }
        }
        catch (Exception e)
        {
            logger.error("创建视频生成任务失败", e);
            return error("创建视频生成任务失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    @GetMapping("/queryTask/{taskId}")
    public AjaxResult testQueryTask(@PathVariable Long taskId)
    {
        try
        {
            AiVideoTask task = aiVideoService.queryTaskStatus(taskId);
            if (task == null)
            {
                return error("任务不存在");
            }

            logger.info("查询任务状态: taskId={}, status={}, progress={}%",
                    taskId, task.getStatus(), task.getProgress());

            return AjaxResult.success("查询成功", task);
        }
        catch (Exception e)
        {
            logger.error("查询任务状态失败: taskId={}", taskId, e);
            return error("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 通过UUID查询任务状态
     *
     * @param taskUuid 任务UUID
     * @return 任务状态信息
     */
    @GetMapping("/queryTaskByUuid/{taskUuid}")
    public AjaxResult testQueryTaskByUuid(@PathVariable String taskUuid)
    {
        try
        {
            AiVideoTask task = aiVideoService.queryTaskByUuid(taskUuid);
            if (task == null)
            {
                return error("任务不存在");
            }

            logger.info("通过UUID查询任务状态: taskUuid={}, status={}, progress={}%",
                    taskUuid, task.getStatus(), task.getProgress());

            return AjaxResult.success("查询成功", task);
        }
        catch (Exception e)
        {
            logger.error("通过UUID查询任务状态失败: taskUuid={}", taskUuid, e);
            return error("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 同步任务状态（从AI提供商同步）
     *
     * @param taskId 任务ID
     * @return 同步后的任务状态
     */
    @PostMapping("/syncTask/{taskId}")
    public AjaxResult testSyncTask(@PathVariable Long taskId)
    {
        try
        {
            logger.info("测试同步任务状态: taskId={}", taskId);

            AiVideoTask task = aiVideoService.syncTaskStatus(taskId);
            if (task == null)
            {
                return error("任务不存在");
            }

            logger.info("任务状态同步成功: taskId={}, status={}, progress={}%",
                    taskId, task.getStatus(), task.getProgress());

            return AjaxResult.success("同步成功", task);
        }
        catch (Exception e)
        {
            logger.error("同步任务状态失败: taskId={}", taskId, e);
            return error("同步任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 处理完成的任务（下载视频并上传到OSS）
     *
     * @param taskId 任务ID
     * @return 处理结果
     */
    @PostMapping("/processCompletedTask/{taskId}")
    public AjaxResult testProcessCompletedTask(@PathVariable Long taskId)
    {
        try
        {
            logger.info("测试处理完成的任务: taskId={}", taskId);

            boolean result = aiVideoService.processCompletedTask(taskId);
            if (result)
            {
                logger.info("任务处理成功: taskId={}", taskId);
                return success("任务处理成功");
            }
            else
            {
                logger.warn("任务处理失败: taskId={}", taskId);
                return error("任务处理失败");
            }
        }
        catch (Exception e)
        {
            logger.error("处理完成任务失败: taskId={}", taskId, e);
            return error("处理完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    @PostMapping("/cancelTask/{taskId}")
    public AjaxResult testCancelTask(@PathVariable Long taskId)
    {
        try
        {
            logger.info("测试取消任务: taskId={}", taskId);

            boolean result = aiVideoService.cancelTask(taskId);
            if (result)
            {
                logger.info("任务取消成功: taskId={}", taskId);
                return success("任务取消成功");
            }
            else
            {
                logger.warn("任务取消失败: taskId={}", taskId);
                return error("任务取消失败");
            }
        }
        catch (Exception e)
        {
            logger.error("取消任务失败: taskId={}", taskId, e);
            return error("取消任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{taskId}")
    public AjaxResult testDeleteTask(@PathVariable Long taskId)
    {
        try
        {
            logger.info("测试删除任务: taskId={}", taskId);

            int rows = aiVideoService.deleteTask(taskId);
            if (rows > 0)
            {
                logger.info("任务删除成功: taskId={}", taskId);
                return success("任务删除成功");
            }
            else
            {
                return error("任务删除失败");
            }
        }
        catch (Exception e)
        {
            logger.error("删除任务失败: taskId={}", taskId, e);
            return error("删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的任务列表
     *
     * @return 任务列表
     */
    @GetMapping("/myTasks")
    public AjaxResult testGetMyTasks()
    {
        try
        {
            Long userId = SecurityUtils.getUserId();
            logger.info("获取用户任务列表: userId={}", userId);

            List<AiVideoTask> tasks = aiVideoService.getUserTasks(userId);
            logger.info("获取到 {} 个任务", tasks.size());

            return AjaxResult.success("获取成功", tasks);
        }
        catch (Exception e)
        {
            logger.error("获取用户任务列表失败", e);
            return error("获取用户任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 快速测试 - 创建简单的文本视频任务
     *
     * @return 任务创建结果
     */
    @PostMapping("/quickTest")
    public AjaxResult quickTest()
    {
        try
        {
            VideoGenerationRequest request = new VideoGenerationRequest();
            request.setPrompt("A beautiful sunset over the ocean");
            request.setModel("default");
            request.setDuration(5);
            request.setResolution("720p");
            request.setAspectRatio("16:9");

            Long userId = SecurityUtils.getUserId();
            logger.info("快速测试 - 创建视频任务: userId={}", userId);

            VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);

            if (!"failed".equals(result.getStatus()))
            {
                logger.info("快速测试任务创建成功: taskId={}, taskUuid={}",
                        result.getTaskId(), result.getTaskUuid());
                return AjaxResult.success("快速测试任务创建成功", result);
            }
            else
            {
                return error("快速测试失败: " + result.getErrorMessage());
            }
        }
        catch (Exception e)
        {
            logger.error("快速测试失败", e);
            return error("快速测试失败: " + e.getMessage());
        }
    }

    /**
     * 诊断AI提供商配置
     *
     * @return 诊断信息
     */
    @GetMapping("/diagnoseProviders")
    public AjaxResult diagnoseProviders()
    {
        try
        {
            Map<String, Object> result = new HashMap<>();

            if (videoProviderServices == null || videoProviderServices.isEmpty())
            {
                result.put("status", "error");
                result.put("message", "未找到任何可用的视频提供商服务");
                result.put("providers", new String[0]);
                return AjaxResult.success("诊断完成", result);
            }

            List<Map<String, Object>> providers = new java.util.ArrayList<>();
            for (IVideoProviderService service : videoProviderServices)
            {
                Map<String, Object> providerInfo = new HashMap<>();
                providerInfo.put("name", service.getProviderName());
                providerInfo.put("class", service.getClass().getSimpleName());
                providerInfo.put("available", true);
                providers.add(providerInfo);
            }

            result.put("status", "success");
            result.put("totalProviders", videoProviderServices.size());
            result.put("providers", providers);

            logger.info("AI提供商诊断: 找到 {} 个可用提供商", videoProviderServices.size());

            return AjaxResult.success("诊断完成", result);
        }
        catch (Exception e)
        {
            logger.error("诊断AI提供商失败", e);
            return error("诊断失败: " + e.getMessage());
        }
    }

    /**
     * 测试查询提供商任务状态（直接调用提供商服务）
     *
     * @param providerName 提供商名称（如 ark, openai）
     * @param providerTaskId 提供商任务ID
     * @return 任务状态
     */
    @GetMapping("/testProviderQuery/{providerName}/{providerTaskId}")
    public AjaxResult testProviderQuery(@PathVariable String providerName, @PathVariable String providerTaskId)
    {
        try
        {
            logger.info("测试查询提供商任务: provider={}, taskId={}", providerName, providerTaskId);

            if (videoProviderServices == null || videoProviderServices.isEmpty())
            {
                return error("未找到任何可用的视频提供商服务");
            }

            IVideoProviderService targetService = null;
            for (IVideoProviderService service : videoProviderServices)
            {
                if (providerName.equalsIgnoreCase(service.getProviderName()))
                {
                    targetService = service;
                    break;
                }
            }

            if (targetService == null)
            {
                return error("未找到指定的提供商: " + providerName);
            }

            logger.info("找到提供商服务: {}", targetService.getClass().getSimpleName());

            IVideoProviderService.TaskStatus status = targetService.queryTaskStatus(providerTaskId);

            Map<String, Object> result = new HashMap<>();
            result.put("provider", providerName);
            result.put("providerTaskId", providerTaskId);
            result.put("status", status.getStatus());
            result.put("progress", status.getProgress());
            result.put("videoUrl", status.getVideoUrl());
            result.put("errorCode", status.getErrorCode());
            result.put("errorMessage", status.getErrorMessage());

            logger.info("查询成功: status={}, progress={}%", status.getStatus(), status.getProgress());

            return AjaxResult.success("查询成功", result);
        }
        catch (Exception e)
        {
            logger.error("测试查询提供商任务失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务的详细信息（用于诊断 provider 字段问题）
     *
     * @param taskId 任务ID
     * @return 任务详细信息
     */
    @GetMapping("/inspectTask/{taskId}")
    public AjaxResult inspectTask(@PathVariable Long taskId)
    {
        try
        {
            logger.info("检查任务详细信息: taskId={}", taskId);

            AiVideoTask task = aiVideoService.queryTaskStatus(taskId);
            if (task == null)
            {
                return error("任务不存在: " + taskId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("taskId", task.getTaskId());
            result.put("taskUuid", task.getTaskUuid());
            result.put("provider", task.getProvider());  // 关键字段
            result.put("providerTaskId", task.getProviderTaskId());
            result.put("status", task.getStatus());
            result.put("progress", task.getProgress());
            result.put("errorCode", task.getErrorCode());
            result.put("errorMessage", task.getErrorMessage());
            result.put("retryCount", task.getRetryCount());
            result.put("maxRetry", task.getMaxRetry());
            result.put("modelName", task.getModelName());
            result.put("providerVideoUrl", task.getProviderVideoUrl());
            result.put("ossVideoUrl", task.getOssVideoUrl());

            // 检查提供商服务是否存在
            boolean providerExists = false;
            String providerClass = null;
            if (videoProviderServices != null)
            {
                for (IVideoProviderService service : videoProviderServices)
                {
                    if (task.getProvider() != null && task.getProvider().equalsIgnoreCase(service.getProviderName()))
                    {
                        providerExists = true;
                        providerClass = service.getClass().getSimpleName();
                        break;
                    }
                }
            }
            result.put("providerServiceExists", providerExists);
            result.put("providerServiceClass", providerClass);

            logger.info("任务检查完成: taskId={}, provider={}, providerExists={}",
                taskId, task.getProvider(), providerExists);

            return AjaxResult.success("检查完成", result);
        }
        catch (Exception e)
        {
            logger.error("检查任务失败: taskId={}", taskId, e);
            return error("检查失败: " + e.getMessage());
        }
    }
}
