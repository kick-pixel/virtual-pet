package com.ruoyi.virtual.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.VirtualPetShowcase;
import com.ruoyi.system.domain.VirtualUserFavorite;
import com.ruoyi.system.service.IVirtualShowcaseService;
import com.ruoyi.virtual.security.VirtualSecurityUtils;

/**
 * 虚拟宠物平台展示控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/virtual/showcase")
public class VirtualShowcaseController extends BaseController
{
    @Autowired
    private IVirtualShowcaseService virtualShowcaseService;

    /**
     * 展示列表（公开，支持分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(VirtualPetShowcase query)
    {
        startPage();
        List<VirtualPetShowcase> list = virtualShowcaseService.listShowcases(query);
        return getDataTable(list);
    }

    /**
     * 精选展示（公开）
     */
    @GetMapping("/featured")
    public AjaxResult featured()
    {
        List<VirtualPetShowcase> list = virtualShowcaseService.getFeaturedShowcases();
        return AjaxResult.success(list);
    }

    /**
     * 展示详情（公开）
     */
    @GetMapping("/{showcaseId}")
    public AjaxResult detail(@PathVariable Long showcaseId)
    {
        VirtualPetShowcase showcase = virtualShowcaseService.getShowcaseById(showcaseId);
        if (showcase == null)
        {
            return AjaxResult.error("作品不存在");
        }
        // 增加浏览量
        virtualShowcaseService.viewShowcase(showcaseId);
        return AjaxResult.success(showcase);
    }

    /**
     * 点赞
     */
    @PostMapping("/{showcaseId}/like")
    public AjaxResult like(@PathVariable Long showcaseId)
    {
        virtualShowcaseService.likeShowcase(showcaseId);
        return AjaxResult.success();
    }

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{showcaseId}/favorite")
    public AjaxResult toggleFavorite(@PathVariable Long showcaseId)
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        boolean isFavorited = virtualShowcaseService.isFavorited(userId, "showcase", showcaseId);
        if (isFavorited)
        {
            virtualShowcaseService.removeFavorite(userId, "showcase", showcaseId);
            return AjaxResult.success("已取消收藏");
        }
        else
        {
            virtualShowcaseService.addFavorite(userId, "showcase", showcaseId);
            return AjaxResult.success("已收藏");
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/favorites")
    public AjaxResult getFavorites()
    {
        Long userId = VirtualSecurityUtils.getCurrentUserId();
        List<VirtualUserFavorite> list = virtualShowcaseService.getUserFavorites(userId);
        return AjaxResult.success(list);
    }
}
