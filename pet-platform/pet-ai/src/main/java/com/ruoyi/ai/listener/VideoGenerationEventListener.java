package com.ruoyi.ai.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.ai.VideoGenerationCompletedEvent;
import com.ruoyi.common.event.ai.VideoGenerationCreatedEvent;
import com.ruoyi.common.event.ai.VideoGenerationFailedEvent;
import com.ruoyi.common.event.ai.VideoGenerationStatusChangedEvent;

/**
 * AI视频生成事件监听器示例
 *
 * 这是一个示例监听器，展示如何监听视频生成事件
 * 业务系统可以创建自己的监听器来处理这些事件
 *
 * 使用说明：
 * 1. 创建自己的监听器类，添加 @Component 注解
 * 2. 注入需要的业务服务（通知、计费、统计等）
 * 3. 使用 @EventListener 监听事件
 * 4. 使用 @Async 异步处理，避免阻塞主流程
 *
 * @author ruoyi
 */
@Component
public class VideoGenerationEventListener
{
    private static final Logger log = LoggerFactory.getLogger(VideoGenerationEventListener.class);

    /**
     * 监听任务创建事件
     *
     * 业务场景：
     * - 发送任务创建通知给用户
     * - 记录用户行为数据
     * - 预扣费或锁定积分
     * - 统计任务创建量
     */
    @EventListener
    @Async("taskExecutor")
    public void handleTaskCreated(VideoGenerationCreatedEvent event)
    {
        log.info("【事件监听】视频生成任务创建: taskId={}, userId={}, model={}, prompt={}",
            event.getTaskId(), event.getUserId(), event.getModelName(),
            event.getPromptText() != null ? event.getPromptText().substring(0, Math.min(50, event.getPromptText().length())) : "");

        // 业务处理示例：
        // 1. 发送通知
        // notificationService.sendTaskCreatedNotification(event.getUserId(), event.getTaskId());

        // 2. 记录用户行为
        // analyticsService.recordTaskCreation(event);

        // 3. 预扣费
        // billingService.reserveCredits(event.getUserId(), event.getModelName());

        // 4. 统计分析
        // statisticsService.incrementTaskCount(event.getModelName());
    }

    /**
     * 监听任务状态变化事件
     *
     * 业务场景：
     * - 发送进度通知（每25%或每50%）
     * - 更新前端实时状态（WebSocket推送）
     * - 记录状态变化历史
     * - 统计平均处理时间
     */
    @EventListener
    @Async("taskExecutor")
    public void handleStatusChanged(VideoGenerationStatusChangedEvent event)
    {
        log.info("【事件监听】任务状态变化: taskId={}, {} -> {}, progress={}%",
            event.getTaskId(), event.getOldStatus(), event.getNewStatus(), event.getProgress());

        // 业务处理示例：
        // 1. 重要进度节点通知（每25%）
        // if (event.isSignificantProgress()) {
        //     notificationService.sendProgressUpdate(event.getUserId(), event.getTaskId(), event.getProgress());
        // }

        // 2. WebSocket实时推送
        // if (webSocketService != null) {
        //     webSocketService.pushTaskStatus(event.getUserId(), event);
        // }

        // 3. 记录状态历史
        // taskHistoryService.recordStatusChange(event);

        // 4. 任务开始时记录开始时间
        // if (event.isStarted()) {
        //     taskMetricsService.recordTaskStartTime(event.getTaskId());
        // }
    }

    /**
     * 监听任务完成事件
     *
     * 业务场景：
     * - 发送完成通知（站内信、邮件、短信、微信推送）
     * - 扣费处理
     * - 更新用户积分/等级
     * - 统计成功率
     * - Webhook回调
     */
    @EventListener
    @Async("taskExecutor")
    public void handleTaskCompleted(VideoGenerationCompletedEvent event)
    {
        log.info("【事件监听】视频生成完成: taskId={}, userId={}, videoUrl={}, fileSize={}",
            event.getTaskId(), event.getUserId(), event.getOssVideoUrl(),
            event.getFileSize() != null ? event.getFileSize() / 1024 / 1024 + "MB" : "unknown");

        // 业务处理示例：
        // 1. 发送完成通知
        // notificationService.sendTaskCompletedNotification(
        //     event.getUserId(),
        //     event.getTaskId(),
        //     event.getOssVideoUrl()
        // );

        // 2. 扣费处理
        // BigDecimal cost = billingService.calculateCost(event.getVideoDuration(), event.getModelName());
        // billingService.charge(event.getUserId(), cost, event.getTaskId());

        // 3. 更新用户积分
        // userService.addPoints(event.getUserId(), 10);

        // 4. 统计分析
        // analyticsService.recordSuccessfulGeneration(event);
        // analyticsService.updateModelUsageStats(event.getModelName());

        // 5. 发送邮件（VIP用户）
        // if (userService.isVip(event.getUserId())) {
        //     emailService.sendVideoReadyEmail(event.getUserId(), event.getOssVideoUrl());
        // }

        // 6. Webhook回调
        // String webhookUrl = userService.getWebhookUrl(event.getUserId());
        // if (webhookUrl != null) {
        //     webhookService.sendCallback(webhookUrl, event);
        // }

        // 7. 推送到消息队列（供其他系统消费）
        // messageQueue.publish("video.generation.completed", event);
    }

    /**
     * 监听任务失败事件
     *
     * 业务场景：
     * - 发送失败通知
     * - 退款处理（如果已扣费）
     * - 记录失败统计
     * - 触发告警（失败率过高）
     * - 自动重试判断
     */
    @EventListener
    @Async("taskExecutor")
    public void handleTaskFailed(VideoGenerationFailedEvent event)
    {
        log.error("【事件监听】视频生成失败: taskId={}, userId={}, errorCode={}, errorMessage={}, retryCount={}/{}",
            event.getTaskId(), event.getUserId(), event.getErrorCode(),
            event.getErrorMessage(), event.getRetryCount(), event.getMaxRetry());

        // 业务处理示例：
        // 1. 发送失败通知
        // notificationService.sendTaskFailedNotification(
        //     event.getUserId(),
        //     event.getTaskId(),
        //     event.getErrorMessage()
        // );

        // 2. 退款（如果已扣费）
        // billingService.refundVideoGeneration(event.getUserId(), event.getTaskId());

        // 3. 记录失败统计
        // analyticsService.recordFailedGeneration(event);

        // 4. 告警（失败率过高）
        // if (!event.canRetry()) {
        //     alertService.sendAlert("视频生成任务最终失败", event);
        // }

        // 5. 自动重试判断（可重试的错误）
        // if (event.canRetry() && isRetryableError(event.getErrorCode())) {
        //     taskRetryService.scheduleRetry(event.getTaskId());
        // }

        // 6. 记录错误日志到数据库（供后续分析）
        // errorLogService.recordError(event);
    }

    /**
     * 判断是否是可重试的错误
     */
    private boolean isRetryableError(String errorCode)
    {
        // 网络错误、超时等可以重试
        return "NETWORK_ERROR".equals(errorCode)
            || "TIMEOUT".equals(errorCode)
            || "SERVICE_UNAVAILABLE".equals(errorCode);
    }
}
