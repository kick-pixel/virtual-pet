package com.ruoyi.web.controller.web3;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.service.IPetPaymentTransactionService;
import com.ruoyi.web3.service.ITransactionScanService;

/**
 * 支付交易记录Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/web3/transaction")
public class TransactionController extends BaseController
{
    @Autowired
    private IPetPaymentTransactionService transactionService;

    @Autowired
    private ITransactionScanService transactionScanService;

    /**
     * 查询支付交易记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:list')")
    @GetMapping("/list")
    public TableDataInfo list(PetPaymentTransaction transaction)
    {
        startPage();
        List<PetPaymentTransaction> list = transactionService.selectTransactionList(transaction);
        return getDataTable(list);
    }

    /**
     * 导出支付交易记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:export')")
    @Log(title = "支付交易记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PetPaymentTransaction transaction)
    {
        List<PetPaymentTransaction> list = transactionService.selectTransactionList(transaction);
        ExcelUtil<PetPaymentTransaction> util = new ExcelUtil<PetPaymentTransaction>(PetPaymentTransaction.class);
        util.exportExcel(response, list, "支付交易记录");
    }

    /**
     * 获取支付交易记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(transactionService.selectTransactionById(id));
    }

    /**
     * 根据交易哈希查询交易记录
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:query')")
    @GetMapping("/hash/{txHash}")
    public AjaxResult getByHash(@PathVariable("txHash") String txHash)
    {
        return success(transactionService.selectTransactionByHash(txHash));
    }

    /**
     * 删除支付交易记录
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:remove')")
    @Log(title = "支付交易记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(transactionService.deleteTransactionByIds(ids));
    }

    /**
     * 查询用户交易记录
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:list')")
    @GetMapping("/user/{userId}")
    public TableDataInfo getUserTransactions(@PathVariable("userId") Long userId)
    {
        startPage();
        List<PetPaymentTransaction> list = transactionService.selectTransactionByUserId(userId);
        return getDataTable(list);
    }

    /**
     * 查询待确认的交易
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:list')")
    @GetMapping("/pending")
    public TableDataInfo getPendingTransactions()
    {
        startPage();
        List<PetPaymentTransaction> list = transactionService.selectPendingTransactions();
        return getDataTable(list);
    }

    /**
     * 手动触发交易扫描
     */
    @PreAuthorize("@ss.hasPermi('system:transaction:scan')")
    @Log(title = "交易扫描", businessType = BusinessType.OTHER)
    @PostMapping("/scan")
    public AjaxResult triggerScan()
    {
        try
        {
            transactionScanService.scanAllNetworks();
            return success("扫描任务已启动");
        }
        catch (Exception e)
        {
            return error("扫描失败: " + e.getMessage());
        }
    }
}
