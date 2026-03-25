package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.VirtualPetShowcase;
import com.ruoyi.system.domain.VirtualUserFavorite;

/**
 * Virtual platform showcase service
 */
public interface IVirtualShowcaseService {
    /** Get showcase by ID */
    public VirtualPetShowcase getShowcaseById(Long showcaseId);

    /** List showcases with filters */
    public List<VirtualPetShowcase> listShowcases(VirtualPetShowcase showcase);

    /** Get featured showcases */
    public List<VirtualPetShowcase> getFeaturedShowcases();

    /** Create showcase */
    public int createShowcase(VirtualPetShowcase showcase);

    /** Update showcase */
    public int updateShowcase(VirtualPetShowcase showcase);

    /** Like a showcase */
    public void likeShowcase(Long showcaseId);

    /** Record view */
    public void viewShowcase(Long showcaseId);

    /** Add favorite */
    public int addFavorite(Long userId, String targetType, Long targetId);

    /** Remove favorite */
    public int removeFavorite(Long userId, String targetType, Long targetId);

    /** Check if user has favorited */
    public boolean isFavorited(Long userId, String targetType, Long targetId);

    /** Get user favorites */
    public List<VirtualUserFavorite> getUserFavorites(Long userId);
}
