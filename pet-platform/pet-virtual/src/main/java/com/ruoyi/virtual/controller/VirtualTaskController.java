package com.ruoyi.virtual.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ruoyi.system.domain.VirtualTaskLike;
import com.ruoyi.system.mapper.AiVideoTaskMapper;
import com.ruoyi.system.mapper.VirtualTaskLikeMapper;
import com.ruoyi.file.domain.FileUploadResult;
import com.ruoyi.file.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.github.pagehelper.PageHelper;
import jakarta.validation.Valid;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.domain.VideoGenerationResult;
import com.ruoyi.ai.service.IAiVideoService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.domain.AiVideoTask;
import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserCredits;
import com.ruoyi.system.mapper.VirtualUserMapper;
import com.ruoyi.system.service.IVirtualCreditsService;
import com.ruoyi.system.service.IVirtualGenerationOptionService;
import com.ruoyi.virtual.dto.request.CreateTaskRequest;
import com.ruoyi.virtual.dto.response.TaskDetailResponse;
import com.ruoyi.virtual.security.VirtualSecurityUtils;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/api/virtual/task")
public class VirtualTaskController extends BaseController {
    @Autowired
    private IAiVideoService aiVideoService;

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Autowired(required = false)
    private IFileService fileService;

    @Autowired(required = false)
    private IVirtualGenerationOptionService generationOptionService;

    @Autowired(required = false)
    private com.ruoyi.system.service.ISysFileInfoService fileInfoService;

    @Autowired
    private VirtualTaskLikeMapper taskLikeMapper;

    @Autowired
    private AiVideoTaskMapper aiVideoTaskMapper;

    @Autowired
    private VirtualUserMapper virtualUserMapper;

    @GetMapping("/options")
    public AjaxResult getOptions() {
        try {
            if (generationOptionService == null) {
                Map<String, List<Map<String, Object>>> defaultOptions = new HashMap<>();
                return AjaxResult.success(defaultOptions);
            }
            Map<String, List<com.ruoyi.system.domain.VirtualGenerationOption>> options = generationOptionService
                    .getGroupedActiveOptions();
            return AjaxResult.success(options);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.options.failed", e.getMessage()));
        }
    }

    @PostMapping("/upload-image")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return AjaxResult.error(MessageUtils.message("virtual.upload.image.only"));
            }

            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                return AjaxResult.error(MessageUtils.message("virtual.upload.size.exceed", "10"));
            }
            FileUploadResult result = fileService.upload(file);
            Map<String, Object> data = new HashMap<>();
            data.put("fileId", result.getFileId());
            data.put("url", result.getFileUrl());
            data.put("filename", result.getFileName());
            data.put("size", file.getSize());
            return AjaxResult.success(MessageUtils.message("virtual.upload.success"), data);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.upload.failed", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public AjaxResult createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            if (!request.isValid()) {
                return AjaxResult.error(MessageUtils.message("virtual.prompt.required"));
            }

            Long userId = VirtualSecurityUtils.getCurrentUserId();

            // 校验账号状态及生成权限
            VirtualUser currentUser = virtualUserMapper.selectVirtualUserById(userId);
            if (currentUser == null || !Integer.valueOf(1).equals(currentUser.getStatus())) {
                return AjaxResult.error(MessageUtils.message("virtual.auth.account.disabled"));
            }
            if (Integer.valueOf(1).equals(currentUser.getGenDisabled())) {
                return AjaxResult.error(MessageUtils.message("virtual.task.gen.disabled"));
            }

            int durationSeconds = request.getDuration() != null ? request.getDuration() : 5;
            String resolution = request.getResolution() != null ? request.getResolution() : "720p";

//            Long cost = virtualCreditsService.estimateCost(durationSeconds, resolution);
            Long cost = 30L;

            VirtualUserCredits credits = virtualCreditsService.getBalance(userId);
            if (credits == null) {
                return AjaxResult.error(MessageUtils.message("virtual.credits.account.not.found"));
            }
            Long currentBalance = credits.getBalance();
            if (currentBalance < cost) {
                return AjaxResult.error(MessageUtils.message("virtual.credits.insufficient", currentBalance, cost));
            }

            VideoGenerationRequest videoRequest = new VideoGenerationRequest();
            videoRequest.setPrompt(request.getPrompt());
            videoRequest.setFileId(request.getFileId());
            videoRequest.setImageUrl(request.getImageUrl());
            videoRequest.setDuration(durationSeconds);
            videoRequest.setResolution(resolution);
            videoRequest.setAspectRatio(request.getAspectRatio() != null ? request.getAspectRatio() : "16:9");

            VideoGenerationResult result = aiVideoService.createVideoTask(videoRequest, userId);

            if ("failed".equals(result.getStatus())) {
                return AjaxResult.error(MessageUtils.message("virtual.task.create.failed", result.getErrorMessage()));
            }

            try {
                String promptSuffix = request.getPrompt() != null ? ": " + request.getPrompt() : "";
                virtualCreditsService.freezeCredits(
                        userId,
                        cost,
                        "video_task",
                        result.getTaskId(),
                        MessageUtils.message("virtual.credits.transaction.video.freeze", promptSuffix));
            } catch (Exception e) {
                return AjaxResult.error(MessageUtils.message("virtual.credits.freeze.failed", e.getMessage()));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("taskId", result.getTaskId());
            data.put("taskUuid", result.getTaskUuid());
            data.put("providerTaskId", result.getProviderTaskId());
            data.put("estimatedCost", cost);
            data.put("status", result.getStatus());

            return AjaxResult.success(MessageUtils.message("virtual.task.create.success"), data);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.task.create.failed", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public TableDataInfo list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        PageHelper.startPage(pageNum, pageSize);
        AiVideoTask query = new AiVideoTask();
        query.setUserId(userId);
        List<AiVideoTask> list = aiVideoService.selectTaskList(query);
        fillPresignedUrls(list);
        return getDataTable(list);
    }

    @GetMapping("/showcase")
    public TableDataInfo showcase(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        AiVideoTask query = new AiVideoTask();
        query.setStatus("completed");
        query.setAdminStatus(0);   // 只返回未封禁的视频
        List<AiVideoTask> list = aiVideoService.selectTaskList(query);
        fillPresignedUrls(list);
        return getDataTable(list);
    }

    @GetMapping("/{taskId}")
    public AjaxResult detail(@PathVariable Long taskId) {
        try {
            AiVideoTask task = aiVideoService.queryTaskStatus(taskId);
            if (task == null) {
                return AjaxResult.error(MessageUtils.message("virtual.task.not.found"));
            }

            fillPresignedUrls(List.of(task));

            TaskDetailResponse response = new TaskDetailResponse();
            response.setTaskId(task.getTaskId());
            response.setLikeCount(task.getLikeCount());
            response.setFileId(task.getFileId());
            response.setOssVideoUrl(task.getOssVideoUrl());
            response.setStatus(task.getStatus());
            response.setProgress(task.getProgress());
            response.setVideoUrl(task.getOssVideoUrl());
            response.setPromptText(task.getPromptText());
            response.setPromptImageUrl(task.getPromptImageUrl());
            response.setModelName(task.getModelName());
            response.setVideoDuration(task.getVideoDuration());
            response.setVideoResolution(task.getVideoResolution());
            response.setVideoAspectRatio(task.getVideoAspectRatio());
            response.setErrorMessage(task.getErrorMessage());
            response.setCreateTime(task.getCreateTime());
            return AjaxResult.success(response);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.task.detail.failed", e.getMessage()));
        }
    }

    @GetMapping("/{taskId}/progress")
    public AjaxResult progress(@PathVariable Long taskId) {
        try {
            AiVideoTask task = aiVideoService.queryTaskStatus(taskId);
            if (task == null) {
                return AjaxResult.error(MessageUtils.message("virtual.task.not.found"));
            }

            if (task.getFileId() != null) {
                try {
                    String url = fileService.getPresignedUrl(task.getFileId(), 7 * 24 * 60);
                    task.setOssVideoUrl(url);
                } catch (Exception e) {
                    logger.error("Failed to generate video presigned URL: {}", e.getMessage());
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("status", task.getStatus());
            data.put("progress", task.getProgress());
            data.put("ossVideoUrl", task.getOssVideoUrl());
            data.put("videoUrl", task.getOssVideoUrl());
            data.put("videoPicUrl", task.getVideoPicUrl());
            data.put("errorMessage", task.getErrorMessage());
            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.task.progress.failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}")
    public AjaxResult cancel(@PathVariable Long taskId) {
        try {
            boolean result = aiVideoService.cancelTask(taskId);
            if (result) {
                return AjaxResult.success(MessageUtils.message("virtual.task.cancel.success"));
            } else {
                return AjaxResult.error(MessageUtils.message("virtual.task.cancel.failed"));
            }
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.task.cancel.failed") + ": " + e.getMessage());
        }
    }

    @PostMapping("/{taskId}/retry")
    public AjaxResult retry(@PathVariable Long taskId) {
        try {
            AiVideoTask task = aiVideoService.syncTaskStatus(taskId);
            if (task == null) {
                return AjaxResult.error(MessageUtils.message("virtual.task.not.found"));
            }

            return AjaxResult.success(MessageUtils.message("virtual.task.retry.success"), task);
        } catch (Exception e) {
            return AjaxResult.error(MessageUtils.message("virtual.task.retry.failed", e.getMessage()));
        }
    }

    @PostMapping("/{taskId}/like")
    public AjaxResult toggleLike(@PathVariable Long taskId) {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        VirtualTaskLike existing = taskLikeMapper.selectByUserIdAndTaskId(userId, taskId);
        boolean liked;
        if (existing != null) {
            taskLikeMapper.deleteByUserIdAndTaskId(userId, taskId);
            aiVideoTaskMapper.decrementLikeCount(taskId);
            liked = false;
        } else {
            VirtualTaskLike like = new VirtualTaskLike();
            like.setUserId(userId);
            like.setTaskId(taskId);
            taskLikeMapper.insert(like);
            aiVideoTaskMapper.incrementLikeCount(taskId);
            liked = true;
        }
        AiVideoTask task = aiVideoTaskMapper.selectVideoTaskById(taskId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", task != null ? task.getLikeCount() : 0);
        return AjaxResult.success(result);
    }

    @GetMapping("/liked-ids")
    public AjaxResult getLikedIds() {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        List<Long> ids = taskLikeMapper.selectTaskIdsByUserId(userId);
        return AjaxResult.success(ids);
    }

    @PostMapping("/{taskId}/view")
    public AjaxResult recordView(@PathVariable Long taskId) {
        try {
            aiVideoTaskMapper.incrementViewCount(taskId, 1L);
        } catch (Exception e) {
            logger.warn("Failed to increment view count: taskId={}", taskId);
        }
        return AjaxResult.success();
    }

    /**
     * 并行生成任务列表中所有文件的预签名 URL（Java 21 虚拟线程）
     */
    private void fillPresignedUrls(List<AiVideoTask> list) {
        if (list == null || list.isEmpty()) return;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (AiVideoTask task : list) {
                if (task.getFileId() != null) {
                    Long fileId = task.getFileId();
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            task.setOssVideoUrl(fileService.getPresignedUrl(fileId, 7 * 24 * 60));
                        } catch (Exception e) {
                            logger.error("Failed to generate video presigned URL: {}", e.getMessage());
                        }
                    }, executor));
                }
                if (task.getVideoPicFileId() != null) {
                    Long fileId = task.getVideoPicFileId();
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            task.setVideoPicUrl(fileService.getPresignedUrl(fileId, 7 * 24 * 60));
                        } catch (Exception e) {
                            logger.error("Failed to generate video pic presigned URL: {}", e.getMessage());
                        }
                    }, executor));
                }
                if (task.getPromptImageFileId() != null) {
                    Long fileId = task.getPromptImageFileId();
                    futures.add(CompletableFuture.runAsync(() -> {
                        try {
                            task.setPromptImageUrl(fileService.getPresignedUrl(fileId, 7 * 24 * 60));
                        } catch (Exception e) {
                            logger.error("Failed to generate image presigned URL: {}", e.getMessage());
                        }
                    }, executor));
                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }
}
