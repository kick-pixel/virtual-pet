package com.ruoyi.web.controller.web3;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.service.ISysTokenConfigService;

/**
 * 代币配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/web3/token")
public class TokenConfigController extends BaseController
{
    @Autowired
    private ISysTokenConfigService tokenConfigService;

    /**
     * 查询代币配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:token:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTokenConfig tokenConfig)
    {
        startPage();
        List<SysTokenConfig> list = tokenConfigService.selectTokenConfigList(tokenConfig);
        return getDataTable(list);
    }

    /**
     * 导出代币配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:token:export')")
    @Log(title = "代币配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysTokenConfig tokenConfig)
    {
        List<SysTokenConfig> list = tokenConfigService.selectTokenConfigList(tokenConfig);
        ExcelUtil<SysTokenConfig> util = new ExcelUtil<SysTokenConfig>(SysTokenConfig.class);
        util.exportExcel(response, list, "代币配置数据");
    }

    /**
     * 获取代币配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:token:query')")
    @GetMapping(value = "/{tokenId}")
    public AjaxResult getInfo(@PathVariable("tokenId") Long tokenId)
    {
        return success(tokenConfigService.selectTokenConfigById(tokenId));
    }

    /**
     * 新增代币配置
     */
    @PreAuthorize("@ss.hasPermi('system:token:add')")
    @Log(title = "代币配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTokenConfig tokenConfig)
    {
        return toAjax(tokenConfigService.insertTokenConfig(tokenConfig));
    }

    /**
     * 修改代币配置
     */
    @PreAuthorize("@ss.hasPermi('system:token:edit')")
    @Log(title = "代币配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTokenConfig tokenConfig)
    {
        return toAjax(tokenConfigService.updateTokenConfig(tokenConfig));
    }

    /**
     * 删除代币配置
     */
    @PreAuthorize("@ss.hasPermi('system:token:remove')")
    @Log(title = "代币配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tokenIds}")
    public AjaxResult remove(@PathVariable Long[] tokenIds)
    {
        return toAjax(tokenConfigService.deleteTokenConfigByIds(tokenIds));
    }

    /**
     * 启用/禁用代币
     */
    @PreAuthorize("@ss.hasPermi('system:token:edit')")
    @Log(title = "代币配置", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysTokenConfig tokenConfig)
    {
        return toAjax(tokenConfigService.updateTokenConfig(tokenConfig));
    }

    /**
     * 根据网络类型查询代币列表
     */
    @PreAuthorize("@ss.hasPermi('system:token:list')")
    @GetMapping("/network/{networkType}")
    public AjaxResult getTokensByNetwork(@PathVariable("networkType") String networkType)
    {
        List<SysTokenConfig> list = tokenConfigService.selectTokenConfigByNetwork(networkType);
        return success(list);
    }
}
