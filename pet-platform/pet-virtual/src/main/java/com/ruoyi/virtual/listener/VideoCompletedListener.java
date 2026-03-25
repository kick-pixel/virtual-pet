package com.ruoyi.virtual.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.ai.VideoGenerationCompletedEvent;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.mapper.VirtualUserStatsMapper;
import com.ruoyi.system.service.IVirtualCreditsService;

@Component
public class VideoCompletedListener
{
    private static final Logger log = LoggerFactory.getLogger(VideoCompletedListener.class);

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Autowired
    private VirtualUserStatsMapper userStatsMapper;

    @EventListener
    @Async("eventExecutor")
    public void onVideoCompleted(VideoGenerationCompletedEvent event)
    {
        try
        {
            Long taskId = event.getTaskId();
            Long userId = event.getUserId();
            String ossVideoUrl = event.getOssVideoUrl();
            Integer duration = event.getVideoDuration();
            String resolution = event.getVideoResolution();

            log.info("Processing video completed event: userId={}, taskId={}, videoUrl={}",
                userId, taskId, ossVideoUrl);

            Long actualCost = virtualCreditsService.estimateCost(
                duration != null ? duration : 5,
                resolution != null ? resolution : "720p"
            );

            String description = MessageUtils.message("virtual.credits.video.completed",
                duration != null ? duration : 5,
                resolution != null ? resolution : "720p",
                event.getTaskUuid());

            virtualCreditsService.confirmSpend(
                userId,
                actualCost,
                "video_completed",
                taskId,
                description
            );

            try {
                userStatsMapper.incrementTotalGenCount(userId);
            } catch (Exception e) {
                log.warn("Failed to increment user stats for userId={}: {}", userId, e.getMessage());
            }

            log.info("Video completed, credits confirmed: userId={}, taskId={}, cost={}, videoUrl={}",
                userId, taskId, actualCost, ossVideoUrl);
        }
        catch (Exception e)
        {
            log.error("Failed to process video completed event: taskId={}, userId={}",
                event.getTaskId(), event.getUserId(), e);
        }
    }
}
