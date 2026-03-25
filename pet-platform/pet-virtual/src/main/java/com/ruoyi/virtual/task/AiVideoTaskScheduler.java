package com.ruoyi.virtual.task;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.ai.queue.VideoProcessQueueProducer;
import com.ruoyi.ai.service.IAiVideoService;
import com.ruoyi.common.annotation.DistributedLock;
import com.ruoyi.system.domain.AiVideoTask;
import com.ruoyi.system.service.IAiVideoTaskService;

/**
 * AI视频任务调度器
 *
 * ⭐ 已从 pet-ai 模块移动到 pet-virtual
 * 原因：避免 ruoyi-admin 管理后台也触发此定时任务
 *
 * @author ruoyi
 */
@Component
public class AiVideoTaskScheduler {
    private static final Logger log = LoggerFactory.getLogger(AiVideoTaskScheduler.class);

    // 实例唯一标识
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);

    @Autowired
    private IAiVideoTaskService videoTaskService;

    @Autowired
    private IAiVideoService aiVideoService;

    @Autowired
    private VideoProcessQueueProducer videoProcessQueueProducer;

    /**
     * 同步处理中的任务状态
     * 每7秒执行一次
     */
    @Scheduled(fixedDelay = 7000)
    @DistributedLock(key = "schedule:ai:video:sync", leaseTime = 120)
    public void syncProcessingTasks() {
        List<AiVideoTask> processingTasks = videoTaskService.selectProcessingVideoTasks();

        if (processingTasks.isEmpty()) {
            return;
        }

        log.info("【定时任务-{}】开始同步处理中的任务，数量: {}", instanceId, processingTasks.size());

        for (AiVideoTask task : processingTasks) {
            try {
                log.info(
                        "【定时任务-{}】同步任务开始: taskId={}, provider={}, providerTaskId={}, status={}, retryCount={}/{}, hasOssUrl={}",
                        instanceId, task.getTaskId(), task.getProvider(), task.getProviderTaskId(), task.getStatus(),
                        task.getRetryCount(), task.getMaxRetry(),
                        (task.getOssVideoUrl() != null && !task.getOssVideoUrl().isEmpty()));

                // 如果任务已经有 OSS URL，说明已完全处理完毕，不应该再同步
                if (task.getOssVideoUrl() != null && !task.getOssVideoUrl().isEmpty()) {
                    log.warn("【定时任务-{}】任务已有OSS URL但仍在处理列表中，跳过: taskId={}, ossUrl={}",
                            instanceId, task.getTaskId(), task.getOssVideoUrl());
                    // 确保状态是 completed
                    if (!"completed".equals(task.getStatus())) {
                        log.error("【定时任务-{}】数据异常：任务已上传但状态不是completed，修复中: taskId={}, currentStatus={}",
                                instanceId, task.getTaskId(), task.getStatus());
                        videoTaskService.updateVideoTaskStatus(task.getTaskId(), "completed", 100);
                    }
                    continue;
                }

                String statusBeforeSync = task.getStatus();
                log.info("【定时任务-{}】准备调用syncTaskStatus: taskId={}, currentStatus={}",
                        instanceId, task.getTaskId(), statusBeforeSync);

                AiVideoTask updatedTask = aiVideoService.syncTaskStatus(task.getTaskId());

                log.info(
                        "【定时任务-{}】syncTaskStatus返回: taskId={}, oldStatus={}, newStatus={}, progress={}, hasProviderUrl={}",
                        instanceId, task.getTaskId(), statusBeforeSync, updatedTask.getStatus(),
                        updatedTask.getProgress(),
                        (updatedTask.getProviderVideoUrl() != null && !updatedTask.getProviderVideoUrl().isEmpty()));

                // 如果任务完成，发送到视频处理队列（异步下载+上传OSS）
                if ("completed".equals(updatedTask.getStatus())) {
                    videoProcessQueueProducer.send(task.getTaskId());
                    log.info("【定时任务-{}】已发送到视频处理队列: taskId={}", instanceId, task.getTaskId());
                } else if ("failed".equals(updatedTask.getStatus())) {
                    log.error("【定时任务-{}】任务同步后状态变为failed: taskId={}, errorCode={}, errorMessage={}",
                            instanceId, task.getTaskId(), updatedTask.getErrorCode(), updatedTask.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("【定时任务-{}】同步任务状态失败: taskId={}, error={}", instanceId, task.getTaskId(), e.getMessage(), e);
            }
        }

        log.info("【定时任务-{}】同步处理中的任务完成", instanceId);
    }
}
