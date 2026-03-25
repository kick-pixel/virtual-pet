package com.ruoyi.web.controller.virtual;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.VirtualCreditsTransaction;
import com.ruoyi.system.mapper.VirtualCreditsTransactionMapper;

/**
 * 管理后台 - 积分流水
 */
@RestController
@RequestMapping("/admin/virtual/credits")
public class AdminVirtualCreditsController extends BaseController {

    @Autowired
    private VirtualCreditsTransactionMapper creditsTransactionMapper;

    /**
     * 积分流水列表
     */
    @PreAuthorize("@ss.hasPermi('virtual:credits:list')")
    @GetMapping("/list")
    public TableDataInfo list(VirtualCreditsTransaction query) {
        startPage();
        List<VirtualCreditsTransaction> list =
                creditsTransactionMapper.selectAdminCreditsTransactionList(query);
        return getDataTable(list);
    }
}
