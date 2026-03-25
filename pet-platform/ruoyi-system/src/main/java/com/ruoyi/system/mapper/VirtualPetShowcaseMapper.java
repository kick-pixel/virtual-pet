package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualPetShowcase;

/**
 * 宠物示例展示Mapper接口
 */
public interface VirtualPetShowcaseMapper {
    public VirtualPetShowcase selectByShowcaseId(Long showcaseId);

    public List<VirtualPetShowcase> selectVirtualPetShowcaseList(VirtualPetShowcase showcase);

    public List<VirtualPetShowcase> selectFeaturedShowcases();

    public int insertVirtualPetShowcase(VirtualPetShowcase showcase);

    public int updateVirtualPetShowcase(VirtualPetShowcase showcase);

    public int incrementViewCount(Long showcaseId);

    public int incrementLikeCount(Long showcaseId);

    public int incrementShareCount(Long showcaseId);

    public int incrementFavoriteCount(Long showcaseId);

    public int decrementFavoriteCount(Long showcaseId);
}
