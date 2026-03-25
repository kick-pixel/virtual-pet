package com.ruoyi.web.controller.virtual;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.VirtualCheckinAdminVO;
import com.ruoyi.system.mapper.VirtualDailyCheckinMapper;

/**
 * 管理后台 - 签到管理
 */
@RestController
@RequestMapping("/admin/virtual/checkin")
public class AdminVirtualCheckinController extends BaseController {

    @Autowired
    private VirtualDailyCheckinMapper virtualDailyCheckinMapper;

    /**
     * 用户签到统计列表
     */
    @PreAuthorize("@ss.hasPermi('virtual:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(VirtualCheckinAdminVO query) {
        startPage();
        List<VirtualCheckinAdminVO> list = virtualDailyCheckinMapper.selectAdminCheckinList(query);
        return getDataTable(list);
    }
}
