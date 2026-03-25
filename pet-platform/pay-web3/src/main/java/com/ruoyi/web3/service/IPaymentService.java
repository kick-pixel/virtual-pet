package com.ruoyi.web3.service;

import java.util.List;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;

/**
 * 支付服务接口
 *
 * @author ruoyi
 */
public interface IPaymentService
{
    /**
     * 获取支付信息（用于前端展示收款地址）
     *
     * @param networkType 网络类型
     * @return 区块链配置
     */
    SysBlockchainConfig getPaymentInfo(String networkType);

    /**
     * 获取支持的网络列表
     *
     * @return 网络列表
     */
    List<SysBlockchainConfig> getSupportedNetworks();

    /**
     * 获取网络支持的代币列表
     *
     * @param networkType 网络类型
     * @return 代币列表
     */
    List<SysTokenConfig> getSupportedTokens(String networkType);

    /**
     * 查询交易记录
     *
     * @param transaction 查询条件
     * @return 交易列表
     */
    List<PetPaymentTransaction> queryTransactions(PetPaymentTransaction transaction);

    /**
     * 获取交易详情
     *
     * @param txId 交易ID
     * @return 交易记录
     */
    PetPaymentTransaction getTransaction(Long txId);

    /**
     * 通过交易哈希获取交易详情
     *
     * @param txHash 交易哈希
     * @return 交易记录
     */
    PetPaymentTransaction getTransactionByHash(String txHash);

    /**
     * 关联交易到用户
     *
     * @param txId 交易ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean bindTransactionToUser(Long txId, Long userId);

    /**
     * 关联交易到业务
     *
     * @param txId 交易ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 是否成功
     */
    boolean bindTransactionToBusiness(Long txId, String businessType, Long businessId);

    /**
     * 标记交易为已处理
     *
     * @param txId 交易ID
     * @return 是否成功
     */
    boolean markTransactionProcessed(Long txId);

    /**
     * 获取用户的交易记录
     *
     * @param userId 用户ID
     * @return 交易列表
     */
    List<PetPaymentTransaction> getUserTransactions(Long userId);

    /**
     * 验证交易
     *
     * @param txHash 交易哈希
     * @param networkType 网络类型
     * @return 验证结果
     */
    boolean verifyTransaction(String txHash, String networkType);
}
