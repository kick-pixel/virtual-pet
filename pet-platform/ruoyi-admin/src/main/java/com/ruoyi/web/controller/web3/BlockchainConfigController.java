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
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.service.ISysBlockchainConfigService;
import com.ruoyi.web3.service.IBlockchainService;

/**
 * 区块链网络配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/web3/blockchain")
public class BlockchainConfigController extends BaseController
{
    @Autowired
    private ISysBlockchainConfigService blockchainConfigService;

    @Autowired
    private IBlockchainService blockchainService;

    /**
     * 查询区块链网络配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysBlockchainConfig blockchainConfig)
    {
        startPage();
        List<SysBlockchainConfig> list = blockchainConfigService.selectBlockchainConfigList(blockchainConfig);
        return getDataTable(list);
    }

    /**
     * 导出区块链网络配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:export')")
    @Log(title = "区块链网络配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysBlockchainConfig blockchainConfig)
    {
        List<SysBlockchainConfig> list = blockchainConfigService.selectBlockchainConfigList(blockchainConfig);
        ExcelUtil<SysBlockchainConfig> util = new ExcelUtil<SysBlockchainConfig>(SysBlockchainConfig.class);
        util.exportExcel(response, list, "区块链网络配置数据");
    }

    /**
     * 获取区块链网络配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(blockchainConfigService.selectBlockchainConfigById(configId));
    }

    /**
     * 新增区块链网络配置
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:add')")
    @Log(title = "区块链网络配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysBlockchainConfig blockchainConfig)
    {
        return toAjax(blockchainConfigService.insertBlockchainConfig(blockchainConfig));
    }

    /**
     * 修改区块链网络配置
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:edit')")
    @Log(title = "区块链网络配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysBlockchainConfig blockchainConfig)
    {
        return toAjax(blockchainConfigService.updateBlockchainConfig(blockchainConfig));
    }

    /**
     * 删除区块链网络配置
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:remove')")
    @Log(title = "区块链网络配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(blockchainConfigService.deleteBlockchainConfigByIds(configIds));
    }

    /**
     * 启用/禁用区块链网络
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:edit')")
    @Log(title = "区块链网络配置", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysBlockchainConfig blockchainConfig)
    {
        return toAjax(blockchainConfigService.updateBlockchainConfig(blockchainConfig));
    }

    /**
     * 测试区块链网络连接
     */
    @PreAuthorize("@ss.hasPermi('system:blockchain:query')")
    @GetMapping("/testConnection/{configId}")
    public AjaxResult testConnection(@PathVariable("configId") Long configId)
    {
        try
        {
            SysBlockchainConfig config = blockchainConfigService.selectBlockchainConfigById(configId);
            if (config == null)
            {
                return error("网络配置不存在");
            }
            java.math.BigInteger currentBlock = blockchainService.getCurrentBlockNumber(config);
            return success(currentBlock);
        }
        catch (Exception e)
        {
            return error("连接测试失败: " + e.getMessage());
        }
    }
}
