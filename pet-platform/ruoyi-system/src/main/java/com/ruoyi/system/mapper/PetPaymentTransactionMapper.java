package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.PetPaymentTransaction;

/**
 * 支付交易记录 数据层
 *
 * @author ruoyi
 */
public interface PetPaymentTransactionMapper
{
    /**
     * 查询交易记录
     *
     * @param txId 交易ID
     * @return 交易记录
     */
    public PetPaymentTransaction selectTransactionById(Long txId);

    /**
     * 根据交易哈希查询交易记录
     *
     * @param txHash 交易哈希
     * @return 交易记录
     */
    public PetPaymentTransaction selectTransactionByHash(String txHash);

    /**
     * 查询交易记录列表
     *
     * @param transaction 交易记录
     * @return 交易记录集合
     */
    public List<PetPaymentTransaction> selectTransactionList(PetPaymentTransaction transaction);

    /**
     * 查询待处理的交易列表
     *
     * @return 交易记录集合
     */
    public List<PetPaymentTransaction> selectPendingTransactions();

    /**
     * 查询未处理的已确认交易列表
     *
     * @return 交易记录集合
     */
    public List<PetPaymentTransaction> selectUnprocessedConfirmedTransactions();

    /**
     * 根据用户ID查询交易列表
     *
     * @param userId 用户ID
     * @return 交易记录集合
     */
    public List<PetPaymentTransaction> selectTransactionByUserId(Long userId);

    /**
     * 新增交易记录
     *
     * @param transaction 交易记录
     * @return 结果
     */
    public int insertTransaction(PetPaymentTransaction transaction);

    /**
     * 修改交易记录
     *
     * @param transaction 交易记录
     * @return 结果
     */
    public int updateTransaction(PetPaymentTransaction transaction);

    /**
     * 更新交易状态
     *
     * @param txId 交易ID
     * @param status 状态
     * @param confirmations 确认数
     * @return 结果
     */
    public int updateTransactionStatus(@Param("txId") Long txId, @Param("status") String status, @Param("confirmations") Integer confirmations);

    /**
     * 标记交易为已处理
     *
     * @param txId 交易ID
     * @return 结果
     */
    public int markTransactionProcessed(Long txId);

    /**
     * 删除交易记录
     *
     * @param txId 交易ID
     * @return 结果
     */
    public int deleteTransactionById(Long txId);

    /**
     * 批量删除交易记录
     *
     * @param txIds 需要删除的交易ID
     * @return 结果
     */
    public int deleteTransactionByIds(Long[] txIds);

    /**
     * 检查交易哈希是否存在
     *
     * @param txHash 交易哈希
     * @return 数量
     */
    public int checkTxHashExists(String txHash);
}
