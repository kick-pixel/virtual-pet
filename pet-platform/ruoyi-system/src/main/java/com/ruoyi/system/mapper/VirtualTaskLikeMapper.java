package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VirtualTaskLike;

/**
 * 视频点赞记录 数据层
 *
 * @author ruoyi
 */
public interface VirtualTaskLikeMapper
{
    int insert(VirtualTaskLike like);

    int deleteByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);

    VirtualTaskLike selectByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);

    List<Long> selectTaskIdsByUserId(@Param("userId") Long userId);
}
