package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.VirtualPetShowcase;
import com.ruoyi.system.domain.VirtualUserFavorite;
import com.ruoyi.system.mapper.VirtualPetShowcaseMapper;
import com.ruoyi.system.mapper.VirtualUserFavoriteMapper;
import com.ruoyi.system.service.IVirtualShowcaseService;

/**
 * Virtual platform showcase service implementation
 */
@Service
public class VirtualShowcaseServiceImpl implements IVirtualShowcaseService {
    @Autowired
    private VirtualPetShowcaseMapper showcaseMapper;

    @Autowired
    private VirtualUserFavoriteMapper favoriteMapper;

    @Override
    public VirtualPetShowcase getShowcaseById(Long showcaseId) {
        return showcaseMapper.selectByShowcaseId(showcaseId);
    }

    @Override
    public List<VirtualPetShowcase> listShowcases(VirtualPetShowcase showcase) {
        return showcaseMapper.selectVirtualPetShowcaseList(showcase);
    }

    @Override
    public List<VirtualPetShowcase> getFeaturedShowcases() {
        return showcaseMapper.selectFeaturedShowcases();
    }

    @Override
    public int createShowcase(VirtualPetShowcase showcase) {
        return showcaseMapper.insertVirtualPetShowcase(showcase);
    }

    @Override
    public int updateShowcase(VirtualPetShowcase showcase) {
        return showcaseMapper.updateVirtualPetShowcase(showcase);
    }

    @Override
    public void likeShowcase(Long showcaseId) {
        showcaseMapper.incrementLikeCount(showcaseId);
    }

    @Override
    public void viewShowcase(Long showcaseId) {
        showcaseMapper.incrementViewCount(showcaseId);
    }

    @Override
    @Transactional
    public int addFavorite(Long userId, String targetType, Long targetId) {
        // Check if already favorited
        VirtualUserFavorite existing = favoriteMapper.selectByUserAndTarget(userId, targetType, targetId);
        if (existing != null) {
            return 0; // Already favorited
        }

        VirtualUserFavorite favorite = new VirtualUserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favoriteMapper.insertVirtualUserFavorite(favorite);

        // Increment showcase favorite count if target is showcase
        if ("showcase".equals(targetType)) {
            showcaseMapper.incrementFavoriteCount(targetId);
        }
        return 1;
    }

    @Override
    @Transactional
    public int removeFavorite(Long userId, String targetType, Long targetId) {
        int rows = favoriteMapper.deleteByUserAndTarget(userId, targetType, targetId);
        if (rows > 0 && "showcase".equals(targetType)) {
            showcaseMapper.decrementFavoriteCount(targetId);
        }
        return rows;
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteMapper.selectByUserAndTarget(userId, targetType, targetId) != null;
    }

    @Override
    public List<VirtualUserFavorite> getUserFavorites(Long userId) {
        return favoriteMapper.selectByUserId(userId);
    }
}
