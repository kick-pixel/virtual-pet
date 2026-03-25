package com.ruoyi.ai.queue;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.ai.domain.VideoProcessMessage;
import com.ruoyi.ai.service.IAiVideoService;

/**
 * 视频处理队列 - 消费者
 * 从队列中取出消息，执行视频下载和OSS上传
 *
 * @author ruoyi
 */
@Component
public class VideoProcessQueueConsumer
{
    private static final Logger log = LoggerFactory.getLogger(VideoProcessQueueConsumer.class);

    private static final int MAX_RETRY = 3;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private IAiVideoService aiVideoService;

    @Autowired
    private VideoProcessQueueProducer producer;

    private volatile boolean running = true;
    private Thread consumerThread;

    @PostConstruct
    public void start()
    {
        consumerThread = new Thread(this::consumeLoop, "video-queue-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("【视频队列】消费者已启动");
    }

    @PreDestroy
    public void stop()
    {
        running = false;
        if (consumerThread != null)
        {
            consumerThread.interrupt();
        }
        log.info("【视频队列】消费者已停止");
    }

    private void consumeLoop()
    {
        RBlockingQueue<VideoProcessMessage> queue = redissonClient.getBlockingQueue(VideoProcessQueueProducer.QUEUE_NAME);

        while (running)
        {
            try
            {
                VideoProcessMessage message = queue.poll(5, TimeUnit.SECONDS);
                if (message == null)
                {
                    continue;
                }

                processMessage(message);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                if (running)
                {
                    log.warn("【视频队列】消费者线程被中断");
                }
            }
            catch (Exception e)
            {
                log.error("【视频队列】消费循环异常: {}", e.getMessage(), e);
                // 避免异常导致 CPU 空转
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processMessage(VideoProcessMessage message)
    {
        Long taskId = message.getTaskId();
        log.info("【视频队列】开始处理: {}", message);

        try
        {
            boolean success = aiVideoService.processCompletedTask(taskId);

            if (success)
            {
                log.info("【视频队列】处理完成: taskId={}", taskId);
            }
            else
            {
                handleFailure(message, "processCompletedTask 返回 false");
            }
        }
        catch (Exception e)
        {
            handleFailure(message, e.getMessage());
        }
    }

    private void handleFailure(VideoProcessMessage message, String reason)
    {
        if (message.getRetryCount() < MAX_RETRY)
        {
            log.warn("【视频队列】处理失败，准备重试: {}, 原因: {}", message, reason);
            producer.resend(message);
        }
        else
        {
            log.error("【视频队列】处理失败且已达最大重试次数，放弃: {}, 原因: {}", message, reason);
        }
    }
}
