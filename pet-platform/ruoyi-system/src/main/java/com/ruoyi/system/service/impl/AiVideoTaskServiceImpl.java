package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.AiVideoTask;
import com.ruoyi.system.mapper.AiVideoTaskMapper;
import com.ruoyi.system.service.IAiVideoTaskService;

/**
 * AI视频任务 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AiVideoTaskServiceImpl implements IAiVideoTaskService
{
    @Autowired
    private AiVideoTaskMapper videoTaskMapper;

    /**
     * 查询AI视频任务
     *
     * @param taskId 任务ID
     * @return AI视频任务
     */
    @Override
    public AiVideoTask selectVideoTaskById(Long taskId)
    {
        return videoTaskMapper.selectVideoTaskById(taskId);
    }

    /**
     * 根据任务UUID查询任务
     *
     * @param taskUuid 任务UUID
     * @return AI视频任务
     */
    @Override
    public AiVideoTask selectVideoTaskByUuid(String taskUuid)
    {
        return videoTaskMapper.selectVideoTaskByUuid(taskUuid);
    }

    /**
     * 根据提供商任务ID查询任务
     *
     * @param providerTaskId 提供商任务ID
     * @return AI视频任务
     */
    @Override
    public AiVideoTask selectVideoTaskByProviderTaskId(String providerTaskId)
    {
        return videoTaskMapper.selectVideoTaskByProviderTaskId(providerTaskId);
    }

    /**
     * 查询AI视频任务列表
     *
     * @param videoTask AI视频任务
     * @return AI视频任务集合
     */
    @Override
    public List<AiVideoTask> selectVideoTaskList(AiVideoTask videoTask)
    {
        return videoTaskMapper.selectVideoTaskList(videoTask);
    }

    /**
     * 查询处理中的任务列表
     *
     * @return AI视频任务集合
     */
    @Override
    public List<AiVideoTask> selectProcessingVideoTasks()
    {
        return videoTaskMapper.selectProcessingVideoTasks();
    }

    /**
     * 查询待处理的任务列表
     *
     * @return AI视频任务集合
     */
    @Override
    public List<AiVideoTask> selectPendingVideoTasks()
    {
        return videoTaskMapper.selectPendingVideoTasks();
    }

    /**
     * 根据用户ID查询任务列表
     *
     * @param userId 用户ID
     * @return AI视频任务集合
     */
    @Override
    public List<AiVideoTask> selectVideoTaskByUserId(Long userId)
    {
        return videoTaskMapper.selectVideoTaskByUserId(userId);
    }

    /**
     * 新增AI视频任务
     *
     * @param videoTask AI视频任务
     * @return 结果
     */
    @Override
    public int insertVideoTask(AiVideoTask videoTask)
    {
        return videoTaskMapper.insertVideoTask(videoTask);
    }

    /**
     * 修改AI视频任务
     *
     * @param videoTask AI视频任务
     * @return 结果
     */
    @Override
    public int updateVideoTask(AiVideoTask videoTask)
    {
        return videoTaskMapper.updateVideoTask(videoTask);
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param progress 进度
     * @return 结果
     */
    @Override
    public int updateVideoTaskStatus(Long taskId, String status, Integer progress)
    {
        return videoTaskMapper.updateVideoTaskStatus(taskId, status, progress);
    }

    /**
     * 更新任务错误信息
     *
     * @param taskId 任务ID
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 结果
     */
    @Override
    public int updateVideoTaskError(Long taskId, String errorCode, String errorMessage)
    {
        return videoTaskMapper.updateVideoTaskError(taskId, errorCode, errorMessage);
    }

    /**
     * 增加重试次数
     *
     * @param taskId 任务ID
     * @return 结果
     */
    @Override
    public int incrementRetryCount(Long taskId)
    {
        return videoTaskMapper.incrementRetryCount(taskId);
    }

    /**
     * 删除AI视频任务
     *
     * @param taskId 任务ID
     * @return 结果
     */
    @Override
    public int deleteVideoTaskById(Long taskId)
    {
        return videoTaskMapper.deleteVideoTaskById(taskId);
    }

    /**
     * 批量删除AI视频任务
     *
     * @param taskIds 需要删除的任务ID
     * @return 结果
     */
    @Override
    public int deleteVideoTaskByIds(Long[] taskIds)
    {
        return videoTaskMapper.deleteVideoTaskByIds(taskIds);
    }
}
