package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualUserFavorite;
import org.apache.ibatis.annotations.Param;

/**
 * 用户收藏Mapper接口
 */
public interface VirtualUserFavoriteMapper {
    public VirtualUserFavorite selectById(Long id);

    public VirtualUserFavorite selectByUserAndTarget(@Param("userId") Long userId,
            @Param("targetType") String targetType, @Param("targetId") Long targetId);

    public List<VirtualUserFavorite> selectByUserId(Long userId);

    public List<VirtualUserFavorite> selectVirtualUserFavoriteList(VirtualUserFavorite favorite);

    public int insertVirtualUserFavorite(VirtualUserFavorite favorite);

    public int deleteById(Long id);

    public int deleteByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType,
            @Param("targetId") Long targetId);
}
