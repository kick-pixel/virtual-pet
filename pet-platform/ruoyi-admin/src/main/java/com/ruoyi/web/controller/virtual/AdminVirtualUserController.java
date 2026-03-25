package com.ruoyi.web.controller.virtual;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.VirtualUserAdminVO;
import com.ruoyi.system.mapper.VirtualUserMapper;

/**
 * 管理后台 - 平台用户管理
 */
@RestController
@RequestMapping("/admin/virtual/user")
public class AdminVirtualUserController extends BaseController {

    @Autowired
    private VirtualUserMapper virtualUserMapper;

    /**
     * 平台用户列表（含积分统计）
     */
    @PreAuthorize("@ss.hasPermi('virtual:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(VirtualUserAdminVO query) {
        startPage();
        List<VirtualUserAdminVO> list = virtualUserMapper.selectAdminList(query);
        return getDataTable(list);
    }

    /**
     * 切换禁止生成状态
     */
    @PreAuthorize("@ss.hasPermi('virtual:user:banGenerate')")
    @Log(title = "平台用户-禁止生成", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/gen-disabled")
    public AjaxResult updateGenDisabled(@PathVariable Long userId,
                                        @RequestBody Map<String, Integer> body) {
        Integer genDisabled = body.get("genDisabled");
        if (genDisabled == null || (genDisabled != 0 && genDisabled != 1)) {
            return AjaxResult.error("参数错误：genDisabled 必须为 0 或 1");
        }
        virtualUserMapper.updateGenDisabled(userId, genDisabled);
        return AjaxResult.success();
    }

    /**
     * 禁用/启用账号
     */
    @PreAuthorize("@ss.hasPermi('virtual:user:disable')")
    @Log(title = "平台用户-禁用账号", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/status")
    public AjaxResult updateStatus(@PathVariable Long userId,
                                   @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return AjaxResult.error("参数错误：status 必须为 0 或 1");
        }
        virtualUserMapper.updateStatus(userId, status);
        return AjaxResult.success();
    }
}
