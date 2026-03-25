package com.ruoyi.web.controller.test;

import java.math.BigInteger;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.service.IPetPaymentTransactionService;
import com.ruoyi.system.service.ISysBlockchainConfigService;
import com.ruoyi.system.service.ISysTokenConfigService;
import com.ruoyi.web3.service.IBlockchainService;
import com.ruoyi.web3.service.ITransactionScanService;

/**
 * Web3功能测试Controller
 *
 * 提供Web3区块链扫描、余额查询等功能的测试接口
 * 注意：此Controller仅用于开发测试，生产环境应移除或限制访问权限
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/test/web3")
public class Web3TestController extends BaseController
{
    @Autowired
    private IBlockchainService blockchainService;

    @Autowired
    private ITransactionScanService transactionScanService;

    @Autowired
    private ISysBlockchainConfigService blockchainConfigService;

    @Autowired
    private ISysTokenConfigService tokenConfigService;

    @Autowired
    private IPetPaymentTransactionService transactionService;

    /**
     * 测试区块链连接
     *
     * @param networkId 网络ID
     * @return 连接测试结果
     */
    @GetMapping("/testConnection/{networkId}")
    public AjaxResult testConnection(@PathVariable Long networkId)
    {
        try
        {
            SysBlockchainConfig network = blockchainConfigService.selectBlockchainConfigById(networkId);
            if (network == null)
            {
                return error("网络配置不存在");
            }

            logger.info("测试区块链连接: networkId={}, networkName={}, rpcUrl={}",
                    networkId, network.getNetworkName(), network.getRpcUrl());

            BigInteger blockNumber = blockchainService.getCurrentBlockNumber(network);
            logger.info("连接成功，当前区块高度: {}", blockNumber);

            return AjaxResult.success("连接成功", AjaxResult.success()
                    .put("networkId", networkId)
                    .put("networkName", network.getNetworkName())
                    .put("currentBlockNumber", blockNumber.toString())
            );
        }
        catch (Exception e)
        {
            logger.error("区块链连接测试失败: networkId={}", networkId, e);
            return error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试手动触发区块链扫描
     *
     * @param networkId 网络ID（可选，不传则扫描所有启用的网络）
     * @return 扫描结果
     */
    @PostMapping("/triggerScan")
    public AjaxResult testTriggerScan(@RequestParam(required = false) Long networkId)
    {
        try
        {
            if (networkId != null)
            {
                logger.info("手动触发区块链扫描: networkId={}", networkId);
                SysBlockchainConfig network = blockchainConfigService.selectBlockchainConfigById(networkId);
                if (network == null)
                {
                    return error("网络配置不存在");
                }
                transactionScanService.scanNetwork(network);
                return success("扫描任务已启动: networkId=" + networkId);
            }
            else
            {
                logger.info("手动触发所有网络扫描");
                transactionScanService.scanAllNetworks();
                return success("所有网络扫描任务已启动");
            }
        }
        catch (Exception e)
        {
            logger.error("触发扫描失败: networkId={}", networkId, e);
            return error("触发扫描失败: " + e.getMessage());
        }
    }

    /**
     * 查询交易详情
     *
     * @param txHash 交易哈希
     * @return 交易详情
     */
    @GetMapping("/getTransaction")
    public AjaxResult testGetTransaction(@RequestParam String txHash)
    {
        try
        {
            logger.info("查询交易详情: txHash={}", txHash);

            // 从数据库查询
            PetPaymentTransaction dbTransaction = transactionService.selectTransactionByHash(txHash);

            if (dbTransaction != null)
            {
                logger.info("从数据库查询到交易: txHash={}", txHash);
                return AjaxResult.success("查询成功（数据库）", dbTransaction);
            }

            // 数据库没有
            logger.info("数据库未找到交易: txHash={}", txHash);

            return AjaxResult.success("查询完成", AjaxResult.success()
                    .put("txHash", txHash)
                    .put("message", "交易未在数据库中，请先执行扫描")
            );
        }
        catch (Exception e)
        {
            logger.error("查询交易详情失败: txHash={}", txHash, e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取待确认的交易列表
     *
     * @return 待确认交易列表
     */
    @GetMapping("/getPendingTransactions")
    public AjaxResult testGetPendingTransactions()
    {
        try
        {
            logger.info("查询待确认交易列表");

            List<PetPaymentTransaction> transactions = transactionService.selectPendingTransactions();
            logger.info("发现 {} 笔待确认交易", transactions.size());

            return AjaxResult.success("查询成功", AjaxResult.success()
                    .put("count", transactions.size())
                    .put("transactions", transactions)
            );
        }
        catch (Exception e)
        {
            logger.error("查询待确认交易失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新待确认交易的确认数
     *
     * @return 更新结果
     */
    @PostMapping("/updatePendingTransactions")
    public AjaxResult testUpdatePendingTransactions()
    {
        try
        {
            logger.info("更新待确认交易的确认数");

            int updateCount = transactionScanService.updatePendingTransactions();
            logger.info("更新了 {} 笔交易的确认数", updateCount);

            return AjaxResult.success("更新成功", AjaxResult.success()
                    .put("updateCount", updateCount)
            );
        }
        catch (Exception e)
        {
            logger.error("更新待确认交易失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的网络配置
     *
     * @return 网络配置列表
     */
    @GetMapping("/getEnabledNetworks")
    public AjaxResult testGetEnabledNetworks()
    {
        try
        {
            logger.info("获取所有启用的网络配置");

            List<SysBlockchainConfig> networks = blockchainConfigService.selectEnabledBlockchainConfigList();

            logger.info("找到 {} 个启用的网络", networks.size());

            return AjaxResult.success("查询成功", AjaxResult.success()
                    .put("count", networks.size())
                    .put("networks", networks)
            );
        }
        catch (Exception e)
        {
            logger.error("获取启用网络失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定网络类型的所有代币配置
     *
     * @param networkType 网络类型
     * @return 代币配置列表
     */
    @GetMapping("/getTokensByNetwork/{networkType}")
    public AjaxResult testGetTokensByNetwork(@PathVariable String networkType)
    {
        try
        {
            logger.info("获取网络的代币配置: networkType={}", networkType);

            List<SysTokenConfig> tokens = tokenConfigService.selectTokenConfigByNetwork(networkType);
            logger.info("找到 {} 个代币配置", tokens.size());

            return AjaxResult.success("查询成功", AjaxResult.success()
                    .put("networkType", networkType)
                    .put("count", tokens.size())
                    .put("tokens", tokens)
            );
        }
        catch (Exception e)
        {
            logger.error("获取代币配置失败: networkType={}", networkType, e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 快速测试 - 测试以太坊测试网连接
     *
     * @return 测试结果
     */
    @GetMapping("/quickTestEthereum")
    public AjaxResult quickTestEthereum()
    {
        try
        {
            logger.info("快速测试 - 以太坊测试网连接");

            // 查找以太坊测试网配置
            SysBlockchainConfig query = new SysBlockchainConfig();
            query.setNetworkName("Ethereum Sepolia");
            List<SysBlockchainConfig> networks = blockchainConfigService.selectBlockchainConfigList(query);

            if (networks.isEmpty())
            {
                return error("未找到以太坊Sepolia测试网配置，请先在系统中配置");
            }

            SysBlockchainConfig network = networks.get(0);
            BigInteger blockNumber = blockchainService.getCurrentBlockNumber(network);

            logger.info("快速测试成功: 当前区块高度={}", blockNumber);

            return AjaxResult.success("测试成功", AjaxResult.success()
                    .put("networkName", network.getNetworkName())
                    .put("chainId", network.getChainId())
                    .put("currentBlockNumber", blockNumber.toString())
            );
        }
        catch (Exception e)
        {
            logger.error("快速测试失败", e);
            return error("快速测试失败: " + e.getMessage());
        }
    }

    /**
     * 快速测试 - 测试BSC测试网连接
     *
     * @return 测试结果
     */
    @GetMapping("/quickTestBSC")
    public AjaxResult quickTestBSC()
    {
        try
        {
            logger.info("快速测试 - BSC测试网连接");

            // 查找BSC测试网配置
            SysBlockchainConfig query = new SysBlockchainConfig();
            query.setNetworkName("BSC Testnet");
            List<SysBlockchainConfig> networks = blockchainConfigService.selectBlockchainConfigList(query);

            if (networks.isEmpty())
            {
                return error("未找到BSC测试网配置，请先在系统中配置");
            }

            SysBlockchainConfig network = networks.get(0);
            BigInteger blockNumber = blockchainService.getCurrentBlockNumber(network);

            logger.info("快速测试成功: 当前区块高度={}", blockNumber);

            return AjaxResult.success("测试成功", AjaxResult.success()
                    .put("networkName", network.getNetworkName())
                    .put("chainId", network.getChainId())
                    .put("currentBlockNumber", blockNumber.toString())
            );
        }
        catch (Exception e)
        {
            logger.error("快速测试失败", e);
            return error("快速测试失败: " + e.getMessage());
        }
    }
}
