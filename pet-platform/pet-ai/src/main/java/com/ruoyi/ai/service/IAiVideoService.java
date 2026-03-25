package com.ruoyi.ai.service;

import java.util.List;
import com.ruoyi.ai.domain.VideoGenerationRequest;
import com.ruoyi.ai.domain.VideoGenerationResult;
import com.ruoyi.system.domain.AiVideoTask;

/**
 * AI视频服务接口
 *
 * @author ruoyi
 */
public interface IAiVideoService
{
    /**
     * 创建视频生成任务
     *
     * @param request 生成请求
     * @param userId 用户ID
     * @return 生成结果
     */
    VideoGenerationResult createVideoTask(VideoGenerationRequest request, Long userId);

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    AiVideoTask queryTaskStatus(Long taskId);

    /**
     * 通过UUID查询任务状态
     *
     * @param taskUuid 任务UUID
     * @return 任务信息
     */
    AiVideoTask queryTaskByUuid(String taskUuid);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelTask(Long taskId);

    /**
     * 获取用户任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<AiVideoTask> getUserTasks(Long userId);

    /**
     * 获取任务列表
     *
     * @param task 查询条件
     * @return 任务列表
     */
    List<AiVideoTask> selectTaskList(AiVideoTask task);

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 结果
     */
    int deleteTask(Long taskId);

    /**
     * 批量删除任务
     *
     * @param taskIds 任务ID数组
     * @return 结果
     */
    int deleteTaskByIds(Long[] taskIds);

    /**
     * 同步任务状态（从提供商获取最新状态）
     *
     * @param taskId 任务ID
     * @return 更新后的任务信息
     */
    AiVideoTask syncTaskStatus(Long taskId);

    /**
     * 处理完成的任务（下载视频并上传到OSS）
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean processCompletedTask(Long taskId);
}
