package com.ruoyi.web3.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.service.IPetPaymentTransactionService;
import com.ruoyi.system.service.ISysBlockchainConfigService;
import com.ruoyi.system.service.ISysTokenConfigService;
import com.ruoyi.web3.service.IBlockchainService;
import com.ruoyi.web3.service.IPaymentService;

/**
 * 支付服务实现
 *
 * @author ruoyi
 */
@Service
public class PaymentServiceImpl implements IPaymentService
{
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private ISysBlockchainConfigService blockchainConfigService;

    @Autowired
    private ISysTokenConfigService tokenConfigService;

    @Autowired
    private IPetPaymentTransactionService transactionService;

    @Autowired
    private IBlockchainService blockchainService;

    @Override
    public SysBlockchainConfig getPaymentInfo(String networkType)
    {
        SysBlockchainConfig config = blockchainConfigService.selectBlockchainConfigByNetworkType(networkType);
        if (config == null || !"1".equals(config.getStatus()))
        {
            return null;
        }
        return config;
    }

    @Override
    public List<SysBlockchainConfig> getSupportedNetworks()
    {
        return blockchainConfigService.selectEnabledBlockchainConfigList();
    }

    @Override
    public List<SysTokenConfig> getSupportedTokens(String networkType)
    {
        return tokenConfigService.selectTokenConfigByNetwork(networkType);
    }

    @Override
    public List<PetPaymentTransaction> queryTransactions(PetPaymentTransaction transaction)
    {
        return transactionService.selectTransactionList(transaction);
    }

    @Override
    public PetPaymentTransaction getTransaction(Long txId)
    {
        return transactionService.selectTransactionById(txId);
    }

    @Override
    public PetPaymentTransaction getTransactionByHash(String txHash)
    {
        return transactionService.selectTransactionByHash(txHash);
    }

    @Override
    @Transactional
    public boolean bindTransactionToUser(Long txId, Long userId)
    {
        PetPaymentTransaction tx = transactionService.selectTransactionById(txId);
        if (tx == null)
        {
            return false;
        }

        tx.setUserId(userId);
        transactionService.updateTransaction(tx);
        log.info("交易已关联用户: txId={}, userId={}", txId, userId);
        return true;
    }

    @Override
    @Transactional
    public boolean bindTransactionToBusiness(Long txId, String businessType, Long businessId)
    {
        PetPaymentTransaction tx = transactionService.selectTransactionById(txId);
        if (tx == null)
        {
            return false;
        }

        tx.setBusinessType(businessType);
        tx.setBusinessId(businessId);
        transactionService.updateTransaction(tx);
        log.info("交易已关联业务: txId={}, businessType={}, businessId={}", txId, businessType, businessId);
        return true;
    }

    @Override
    @Transactional
    public boolean markTransactionProcessed(Long txId)
    {
        int result = transactionService.markTransactionProcessed(txId);
        if (result > 0)
        {
            log.info("交易已标记为已处理: txId={}", txId);
            return true;
        }
        return false;
    }

    @Override
    public List<PetPaymentTransaction> getUserTransactions(Long userId)
    {
        return transactionService.selectTransactionByUserId(userId);
    }

    @Override
    public boolean verifyTransaction(String txHash, String networkType)
    {
        SysBlockchainConfig config = blockchainConfigService.selectBlockchainConfigByNetworkType(networkType);
        if (config == null)
        {
            log.warn("未找到网络配置: {}", networkType);
            return false;
        }

        // 检查交易是否成功
        boolean successful = blockchainService.isTransactionSuccessful(config, txHash);
        if (!successful)
        {
            return false;
        }

        // 检查确认数
        int confirmations = blockchainService.getTransactionConfirmations(config, txHash);
        return confirmations >= config.getMinConfirmations();
    }
}
