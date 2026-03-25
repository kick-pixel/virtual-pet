package com.ruoyi.web.controller.virtual;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.VirtualRechargeOrder;
import com.ruoyi.system.mapper.VirtualRechargeOrderMapper;

/**
 * 管理后台 - 订单管理
 */
@RestController
@RequestMapping("/admin/virtual/order")
public class AdminVirtualOrderController extends BaseController {

    @Autowired
    private VirtualRechargeOrderMapper rechargeOrderMapper;

    /**
     * 订单列表（含套餐名称）
     */
    @PreAuthorize("@ss.hasPermi('virtual:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(VirtualRechargeOrder query) {
        startPage();
        List<VirtualRechargeOrder> list = rechargeOrderMapper.selectAdminOrderList(query);
        return getDataTable(list);
    }
}
