package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.mapper.PetPaymentTransactionMapper;
import com.ruoyi.system.service.IPetPaymentTransactionService;

/**
 * 支付交易记录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class PetPaymentTransactionServiceImpl implements IPetPaymentTransactionService
{
    @Autowired
    private PetPaymentTransactionMapper transactionMapper;

    /**
     * 查询交易记录
     *
     * @param txId 交易ID
     * @return 交易记录
     */
    @Override
    public PetPaymentTransaction selectTransactionById(Long txId)
    {
        return transactionMapper.selectTransactionById(txId);
    }

    /**
     * 根据交易哈希查询交易记录
     *
     * @param txHash 交易哈希
     * @return 交易记录
     */
    @Override
    public PetPaymentTransaction selectTransactionByHash(String txHash)
    {
        return transactionMapper.selectTransactionByHash(txHash);
    }

    /**
     * 查询交易记录列表
     *
     * @param transaction 交易记录
     * @return 交易记录集合
     */
    @Override
    public List<PetPaymentTransaction> selectTransactionList(PetPaymentTransaction transaction)
    {
        return transactionMapper.selectTransactionList(transaction);
    }

    /**
     * 查询待处理的交易列表
     *
     * @return 交易记录集合
     */
    @Override
    public List<PetPaymentTransaction> selectPendingTransactions()
    {
        return transactionMapper.selectPendingTransactions();
    }

    /**
     * 查询未处理的已确认交易列表
     *
     * @return 交易记录集合
     */
    @Override
    public List<PetPaymentTransaction> selectUnprocessedConfirmedTransactions()
    {
        return transactionMapper.selectUnprocessedConfirmedTransactions();
    }

    /**
     * 根据用户ID查询交易列表
     *
     * @param userId 用户ID
     * @return 交易记录集合
     */
    @Override
    public List<PetPaymentTransaction> selectTransactionByUserId(Long userId)
    {
        return transactionMapper.selectTransactionByUserId(userId);
    }

    /**
     * 新增交易记录
     *
     * @param transaction 交易记录
     * @return 结果
     */
    @Override
    public int insertTransaction(PetPaymentTransaction transaction)
    {
        return transactionMapper.insertTransaction(transaction);
    }

    /**
     * 修改交易记录
     *
     * @param transaction 交易记录
     * @return 结果
     */
    @Override
    public int updateTransaction(PetPaymentTransaction transaction)
    {
        return transactionMapper.updateTransaction(transaction);
    }

    /**
     * 更新交易状态
     *
     * @param txId 交易ID
     * @param status 状态
     * @param confirmations 确认数
     * @return 结果
     */
    @Override
    public int updateTransactionStatus(Long txId, String status, Integer confirmations)
    {
        return transactionMapper.updateTransactionStatus(txId, status, confirmations);
    }

    /**
     * 标记交易为已处理
     *
     * @param txId 交易ID
     * @return 结果
     */
    @Override
    public int markTransactionProcessed(Long txId)
    {
        return transactionMapper.markTransactionProcessed(txId);
    }

    /**
     * 删除交易记录
     *
     * @param txId 交易ID
     * @return 结果
     */
    @Override
    public int deleteTransactionById(Long txId)
    {
        return transactionMapper.deleteTransactionById(txId);
    }

    /**
     * 批量删除交易记录
     *
     * @param txIds 需要删除的交易ID
     * @return 结果
     */
    @Override
    public int deleteTransactionByIds(Long[] txIds)
    {
        return transactionMapper.deleteTransactionByIds(txIds);
    }

    /**
     * 检查交易哈希是否存在
     *
     * @param txHash 交易哈希
     * @return 是否存在
     */
    @Override
    public boolean checkTxHashExists(String txHash)
    {
        return transactionMapper.checkTxHashExists(txHash) > 0;
    }
}
