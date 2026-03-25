package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.AiVideoTask;

/**
 * AI视频任务 服务层
 *
 * @author ruoyi
 */
public interface IAiVideoTaskService
{
    /**
     * 查询AI视频任务
     *
     * @param taskId 任务ID
     * @return AI视频任务
     */
    public AiVideoTask selectVideoTaskById(Long taskId);

    /**
     * 根据任务UUID查询任务
     *
     * @param taskUuid 任务UUID
     * @return AI视频任务
     */
    public AiVideoTask selectVideoTaskByUuid(String taskUuid);

    /**
     * 根据提供商任务ID查询任务
     *
     * @param providerTaskId 提供商任务ID
     * @return AI视频任务
     */
    public AiVideoTask selectVideoTaskByProviderTaskId(String providerTaskId);

    /**
     * 查询AI视频任务列表
     *
     * @param videoTask AI视频任务
     * @return AI视频任务集合
     */
    public List<AiVideoTask> selectVideoTaskList(AiVideoTask videoTask);

    /**
     * 查询处理中的任务列表
     *
     * @return AI视频任务集合
     */
    public List<AiVideoTask> selectProcessingVideoTasks();

    /**
     * 查询待处理的任务列表
     *
     * @return AI视频任务集合
     */
    public List<AiVideoTask> selectPendingVideoTasks();

    /**
     * 根据用户ID查询任务列表
     *
     * @param userId 用户ID
     * @return AI视频任务集合
     */
    public List<AiVideoTask> selectVideoTaskByUserId(Long userId);

    /**
     * 新增AI视频任务
     *
     * @param videoTask AI视频任务
     * @return 结果
     */
    public int insertVideoTask(AiVideoTask videoTask);

    /**
     * 修改AI视频任务
     *
     * @param videoTask AI视频任务
     * @return 结果
     */
    public int updateVideoTask(AiVideoTask videoTask);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param progress 进度
     * @return 结果
     */
    public int updateVideoTaskStatus(Long taskId, String status, Integer progress);

    /**
     * 更新任务错误信息
     *
     * @param taskId 任务ID
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 结果
     */
    public int updateVideoTaskError(Long taskId, String errorCode, String errorMessage);

    /**
     * 增加重试次数
     *
     * @param taskId 任务ID
     * @return 结果
     */
    public int incrementRetryCount(Long taskId);

    /**
     * 删除AI视频任务
     *
     * @param taskId 任务ID
     * @return 结果
     */
    public int deleteVideoTaskById(Long taskId);

    /**
     * 批量删除AI视频任务
     *
     * @param taskIds 需要删除的任务ID
     * @return 结果
     */
    public int deleteVideoTaskByIds(Long[] taskIds);
}
