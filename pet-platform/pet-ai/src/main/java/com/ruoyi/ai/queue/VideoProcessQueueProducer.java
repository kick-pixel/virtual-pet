package com.ruoyi.ai.queue;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.ai.domain.VideoProcessMessage;

/**
 * 视频处理队列 - 生产者
 *
 * @author ruoyi
 */
@Component
public class VideoProcessQueueProducer
{
    private static final Logger log = LoggerFactory.getLogger(VideoProcessQueueProducer.class);

    public static final String QUEUE_NAME = "queue:ai:video:process";

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 发送视频处理消息到队列
     */
    public void send(Long taskId)
    {
        VideoProcessMessage message = new VideoProcessMessage(taskId);
        RBlockingQueue<VideoProcessMessage> queue = redissonClient.getBlockingQueue(QUEUE_NAME);
        queue.offer(message);
        log.info("【视频队列】已发送到处理队列: {}", message);
    }

    /**
     * 重新入队（用于消费失败重试）
     */
    public void resend(VideoProcessMessage message)
    {
        message.setRetryCount(message.getRetryCount() + 1);
        RBlockingQueue<VideoProcessMessage> queue = redissonClient.getBlockingQueue(QUEUE_NAME);
        queue.offer(message);
        log.info("【视频队列】重新入队: {}", message);
    }
}
