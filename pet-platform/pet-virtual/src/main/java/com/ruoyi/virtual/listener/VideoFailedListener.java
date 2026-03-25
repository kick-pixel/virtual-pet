package com.ruoyi.virtual.listener;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.ruoyi.common.event.ai.VideoGenerationFailedEvent;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.system.domain.VirtualCreditsTransaction;
import com.ruoyi.system.mapper.VirtualUserStatsMapper;
import com.ruoyi.system.service.IVirtualCreditsService;

/**
 * 视频生成失败事件监听器
 *
 * 负责在视频生成任务失败时，退还冻结的积分给用户。
 * 通过查询原始冻结交易记录获取准确退款金额，并防止重复退还。
 */
@Component
public class VideoFailedListener
{
    private static final Logger log = LoggerFactory.getLogger(VideoFailedListener.class);

    private static final String BUSINESS_TYPE_VIDEO_TASK = "video_task";

    @Autowired
    private IVirtualCreditsService virtualCreditsService;

    @Autowired
    private VirtualUserStatsMapper userStatsMapper;

    @EventListener
    @Async("eventExecutor")
    public void onVideoFailed(VideoGenerationFailedEvent event)
    {
        Long taskId = event.getTaskId();
        Long userId = event.getUserId();

        try
        {
            String errorMessage = event.getErrorMessage();
            String errorCode = event.getErrorCode();

            log.warn("收到视频生成失败事件: userId={}, taskId={}, errorCode={}, error={}",
                userId, taskId, errorCode, errorMessage);

            if (userId == null)
            {
                log.warn("任务无 userId，无法退还积分: taskId={}", taskId);
                return;
            }

            // 1. 防重复退还：检查是否已存在该任务的退还记录
            VirtualCreditsTransaction refundQuery = new VirtualCreditsTransaction();
            refundQuery.setTxType("refund");
            refundQuery.setBusinessType(BUSINESS_TYPE_VIDEO_TASK);
            refundQuery.setBusinessId(taskId);
            List<VirtualCreditsTransaction> existingRefunds =
                virtualCreditsService.selectTransactionList(refundQuery);

            if (!existingRefunds.isEmpty())
            {
                log.info("任务已退还过积分，跳过: taskId={}, refundTxId={}",
                    taskId, existingRefunds.get(0).getTxId());
                return;
            }

            // 2. 查询原始冻结交易，获取准确的冻结金额
            VirtualCreditsTransaction freezeQuery = new VirtualCreditsTransaction();
            freezeQuery.setTxType("freeze");
            freezeQuery.setBusinessType(BUSINESS_TYPE_VIDEO_TASK);
            freezeQuery.setBusinessId(taskId);
            List<VirtualCreditsTransaction> freezeTxs =
                virtualCreditsService.selectTransactionList(freezeQuery);

            if (freezeTxs.isEmpty())
            {
                log.warn("未找到冻结交易记录，无法退还: taskId={}, userId={}", taskId, userId);
                return;
            }

            Long refundAmount = freezeTxs.get(0).getAmount();

            // 3. 检查是否已被确认消费（confirmSpend），如果已消费则不退还
            VirtualCreditsTransaction spendQuery = new VirtualCreditsTransaction();
            spendQuery.setTxType("spend");
            spendQuery.setBusinessType(BUSINESS_TYPE_VIDEO_TASK);
            spendQuery.setBusinessId(taskId);
            List<VirtualCreditsTransaction> spendTxs =
                virtualCreditsService.selectTransactionList(spendQuery);

            if (!spendTxs.isEmpty())
            {
                log.info("任务积分已被消费确认，不退还: taskId={}, spendTxId={}",
                    taskId, spendTxs.get(0).getTxId());
                return;
            }

            // 4. 执行退还
            String description = MessageUtils.message("virtual.credits.video.failed",
                errorCode != null ? errorCode : "UNKNOWN",
                errorMessage != null ? errorMessage : "Unknown error");

            virtualCreditsService.refundFrozen(
                userId,
                refundAmount,
                BUSINESS_TYPE_VIDEO_TASK,
                taskId,
                description
            );

            try {
                userStatsMapper.incrementBothCounts(userId);
            } catch (Exception e) {
                log.warn("Failed to increment fail stats for userId={}: {}", userId, e.getMessage());
            }

            log.info("任务失败积分已退还: userId={}, taskId={}, refundAmount={}", userId, taskId, refundAmount);
        }
        catch (Exception e)
        {
            log.error("处理视频失败退还积分异常: taskId={}, userId={}", taskId, userId, e);
        }
    }
}
