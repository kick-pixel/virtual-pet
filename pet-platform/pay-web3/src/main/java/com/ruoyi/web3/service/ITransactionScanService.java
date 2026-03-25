package com.ruoyi.web3.service;

import com.ruoyi.system.domain.SysBlockchainConfig;

/**
 * 交易扫描服务接口
 *
 * @author ruoyi
 */
public interface ITransactionScanService
{
    /**
     * 扫描指定网络的交易
     *
     * @param config 区块链配置
     * @return 新发现的交易数量
     */
    int scanNetwork(SysBlockchainConfig config);

    /**
     * 扫描所有已启用网络的交易
     *
     * @return 新发现的交易总数
     */
    int scanAllNetworks();

    /**
     * 更新待确认交易的状态
     *
     * @return 更新的交易数量
     */
    int updatePendingTransactions();

    /**
     * 手动触发扫描
     *
     * @param networkType 网络类型
     * @return 新发现的交易数量
     */
    int manualScan(String networkType);
}
