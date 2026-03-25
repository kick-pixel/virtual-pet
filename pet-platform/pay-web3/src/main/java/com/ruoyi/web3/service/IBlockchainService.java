package com.ruoyi.web3.service;

import java.math.BigInteger;
import java.util.List;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.web3.domain.TransactionInfo;

/**
 * 区块链服务接口
 *
 * @author ruoyi
 */
public interface IBlockchainService
{
    /**
     * 获取当前区块高度
     *
     * @param config 区块链配置
     * @return 区块高度
     */
    BigInteger getCurrentBlockNumber(SysBlockchainConfig config);

    /**
     * 扫描区块获取转账到平台钱包的交易
     *
     * @param config 区块链配置
     * @param fromBlock 起始区块
     * @param toBlock 结束区块
     * @return 交易列表
     */
    List<TransactionInfo> scanTransactions(SysBlockchainConfig config, BigInteger fromBlock, BigInteger toBlock);

    /**
     * 获取交易确认数
     *
     * @param config 区块链配置
     * @param txHash 交易哈希
     * @return 确认数
     */
    int getTransactionConfirmations(SysBlockchainConfig config, String txHash);

    /**
     * 验证交易是否成功
     *
     * @param config 区块链配置
     * @param txHash 交易哈希
     * @return 是否成功
     */
    boolean isTransactionSuccessful(SysBlockchainConfig config, String txHash);

    /**
     * 获取交易详情
     *
     * @param config 区块链配置
     * @param txHash 交易哈希
     * @return 交易信息
     */
    TransactionInfo getTransaction(SysBlockchainConfig config, String txHash);
}
