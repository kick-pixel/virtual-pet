package com.ruoyi.web3.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.event.web3.PaymentReceivedEvent;
import com.ruoyi.common.event.web3.TransactionConfirmedEvent;
import com.ruoyi.common.event.web3.TransactionDetectedEvent;
import com.ruoyi.common.event.web3.TransactionFailedEvent;
import com.ruoyi.system.domain.PetPaymentTransaction;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.domain.VirtualRechargeOrder;
import com.ruoyi.system.service.IPetPaymentTransactionService;
import com.ruoyi.system.service.ISysBlockchainConfigService;
import com.ruoyi.system.service.ISysTokenConfigService;
import com.ruoyi.system.service.IVirtualRechargeService;
import com.ruoyi.web3.config.Web3Config;
import com.ruoyi.web3.domain.TransactionInfo;
import com.ruoyi.web3.service.IBlockchainService;
import com.ruoyi.web3.service.ITransactionScanService;

/**
 * 交易扫描服务实现
 *
 * @author ruoyi
 */
@Service
public class TransactionScanServiceImpl implements ITransactionScanService
{
    private static final Logger log = LoggerFactory.getLogger(TransactionScanServiceImpl.class);

    /**
     * 并行扫描线程池：每个网络独立线程，数量 = max(4, CPU核数)
     * daemon 线程，JVM 退出时自动销毁
     */
    private final ExecutorService scanExecutor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors()),
        r -> {
            Thread t = new Thread(r, "web3-scan-" + r.hashCode());
            t.setDaemon(true);
            return t;
        }
    );

    @Autowired
    private Web3Config web3Config;

    @Autowired
    private ISysBlockchainConfigService blockchainConfigService;

    @Autowired
    private IPetPaymentTransactionService transactionService;

    @Autowired
    private IBlockchainService blockchainService;

    @Autowired
    private ISysTokenConfigService tokenConfigService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IVirtualRechargeService virtualRechargeService;

    /**
     * 自注入代理，解决 Spring AOP 自调用事务失效问题：
     * scanAllNetworks() 调用 self.scanNetwork() 才能触发 @Transactional
     */
    @Lazy
    @Autowired
    private ITransactionScanService self;

    @Override
    @Transactional
    public int scanNetwork(SysBlockchainConfig config)
    {
        if (!"0".equals(config.getStatus()))
        {
            log.debug("网络已停用，跳过扫描: {}", config.getNetworkType());
            return 0;
        }

        try
        {
            // 获取当前区块高度
            BigInteger currentBlock = blockchainService.getCurrentBlockNumber(config);
            if (currentBlock.equals(BigInteger.ZERO))
            {
                log.warn("无法获取区块高度: {}", config.getNetworkType());
                return 0;
            }

            // 计算扫描范围
            BigInteger fromBlock = config.getScanCurrentBlock() > 0
                ? BigInteger.valueOf(config.getScanCurrentBlock() + 1)
                : (config.getScanStartBlock() > 0
                    ? BigInteger.valueOf(config.getScanStartBlock())
                    : currentBlock.subtract(BigInteger.valueOf(100)));

            // 限制每次扫描的区块数量
            BigInteger toBlock = fromBlock.add(BigInteger.valueOf(config.getScanBatchSize() - 1));
            if (toBlock.compareTo(currentBlock) > 0)
            {
                toBlock = currentBlock;
            }

            // 需要留出确认数
            BigInteger safeBlock = currentBlock.subtract(BigInteger.valueOf(config.getMinConfirmations()));
            if (toBlock.compareTo(safeBlock) > 0)
            {
                toBlock = safeBlock;
            }

            if (fromBlock.compareTo(toBlock) > 0)
            {
                log.debug("没有新区块需要扫描: network={}", config.getNetworkType());
                return 0;
            }

            log.info("开始扫描区块: network={}, blocks={}-{}", config.getNetworkType(), fromBlock, toBlock);

            // 扫描交易
            List<TransactionInfo> transactions = blockchainService.scanTransactions(config, fromBlock, toBlock);

            int newTxCount = 0;
            for (TransactionInfo txInfo : transactions)
            {
                // 检查是否已存在
                if (transactionService.checkTxHashExists(txInfo.getTxHash()))
                {
                    continue;
                }

                // 保存交易记录
                PetPaymentTransaction tx = createTransactionRecord(txInfo, config);
                transactionService.insertTransaction(tx);
                newTxCount++;

                // 发布交易检测事件
                publishTransactionDetectedEvent(tx, config, txInfo);
            }

            // 更新扫描进度
            blockchainConfigService.updateScanCurrentBlock(config.getConfigId(), toBlock.longValue());

            log.info("扫描完成: network={}, blocks={}-{}, newTx={}",
                config.getNetworkType(), fromBlock, toBlock, newTxCount);

            return newTxCount;
        }
        catch (Exception e)
        {
            log.error("扫描网络失败: network={}, error={}", config.getNetworkType(), e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int scanAllNetworks()
    {
        if (!web3Config.isScanEnabled())
        {
            return 0;
        }

        List<SysBlockchainConfig> configs = blockchainConfigService.selectEnabledBlockchainConfigList();
        if (configs.isEmpty())
        {
            return 0;
        }

        // 并行扫描：每个网络独占一个线程，互不阻塞
        // 通过 self（Spring 代理）调用，保证 @Transactional 在子线程中正确生效
        List<CompletableFuture<Integer>> futures = configs.stream()
            .map(config -> CompletableFuture.supplyAsync(
                () -> self.scanNetwork(config),
                scanExecutor
            ))
            .collect(Collectors.toList());

        AtomicInteger totalNewTx = new AtomicInteger(0);
        for (CompletableFuture<Integer> future : futures)
        {
            try
            {
                totalNewTx.addAndGet(future.get(60, TimeUnit.SECONDS));
            }
            catch (java.util.concurrent.TimeoutException e)
            {
                log.warn("某网络扫描超时（60s），已跳过本轮");
            }
            catch (Exception e)
            {
                log.error("并行扫描任务异常: {}", e.getMessage());
            }
        }

        return totalNewTx.get();
    }

    @Override
    public int updatePendingTransactions()
    {
        List<PetPaymentTransaction> pendingTxs = transactionService.selectPendingTransactions();
        if (pendingTxs.isEmpty())
        {
            return 0;
        }

        // 并行处理：每笔待确认交易各自发起 RPC 确认查询，互不阻塞
        List<CompletableFuture<Integer>> futures = pendingTxs.stream()
            .map(tx -> CompletableFuture.supplyAsync(
                () -> processSinglePendingTx(tx),
                scanExecutor
            ))
            .collect(Collectors.toList());

        return futures.stream()
            .mapToInt(f -> {
                try
                {
                    return f.get(30, TimeUnit.SECONDS);
                }
                catch (Exception e)
                {
                    log.error("并行交易确认任务异常: {}", e.getMessage());
                    return 0;
                }
            })
            .sum();
    }

    /**
     * 处理单笔待确认交易（在并行线程中调用，每笔独立，异常不影响其他笔）
     */
    private int processSinglePendingTx(PetPaymentTransaction tx)
    {
        try
        {
            SysBlockchainConfig config = blockchainConfigService.selectBlockchainConfigByNetworkType(tx.getNetworkType());
            if (config == null)
            {
                return 0;
            }

            // 获取确认数（RPC 调用）
            int confirmations = blockchainService.getTransactionConfirmations(config, tx.getTxHash());

            if (confirmations >= config.getMinConfirmations())
            {
                // 验证交易成功（第二次 RPC 调用）
                boolean successful = blockchainService.isTransactionSuccessful(config, tx.getTxHash());
                String status = successful ? "confirmed" : "failed";

                transactionService.updateTransactionStatus(tx.getTxId(), status, confirmations);
                log.info("交易状态已更新: hash={}, status={}, confirmations={}",
                    tx.getTxHash(), status, confirmations);

                tx.setStatus(status);
                tx.setConfirmations(confirmations);
                if (successful)
                {
                    publishTransactionConfirmedEvent(tx, config);

                    // 通过 txHash 查找对应的充值订单
                    VirtualRechargeOrder order = null;
                    try
                    {
                        order = virtualRechargeService.getOrderByTxHash(tx.getTxHash());
                    }
                    catch (Exception e)
                    {
                        log.warn("查询充值订单失败(txHash): txHash={}, error={}", tx.getTxHash(), e.getMessage());
                    }

                    // 兜底匹配：前端 submitTxHash 可能因网络抖动等原因未能将 txHash 写入订单
                    // 此时按发款地址+代币+网络类型匹配最近的待处理订单
                    if (order == null && tx.getFromAddress() != null && tx.getTokenSymbol() != null)
                    {
                        try
                        {
                            order = virtualRechargeService.findMatchingPendingOrder(
                                tx.getFromAddress(), tx.getTokenSymbol(),
                                tx.getNetworkType(), tx.getTxHash());
                            if (order != null)
                            {
                                log.info("兜底匹配充值订单成功: orderNo={}, fromAddr={}, txHash={}",
                                    order.getOrderNo(), tx.getFromAddress(), tx.getTxHash());
                            }
                        }
                        catch (Exception e)
                        {
                            log.warn("兜底匹配充值订单失败: txHash={}, error={}", tx.getTxHash(), e.getMessage());
                        }
                    }

                    if (order != null)
                    {
                        tx.setUserId(order.getUserId());
                        tx.setBusinessId(order.getOrderId());
                        tx.setBusinessType("recharge");
                        transactionService.updateTransaction(tx);
                        try
                        {
                            virtualRechargeService.completeOrder(order.getOrderId(), tx.getTxHash(), tx.getTxId());
                            transactionService.markTransactionProcessed(tx.getTxId());
                            log.info("充值订单已完成: orderNo={}, userId={}, txHash={}",
                                order.getOrderNo(), order.getUserId(), tx.getTxHash());
                        }
                        catch (Exception e)
                        {
                            log.error("完成充值订单失败: orderId={}, txHash={}, error={}",
                                order.getOrderId(), tx.getTxHash(), e.getMessage());
                        }
                    }
                    else if (tx.getUserId() != null)
                    {
                        publishPaymentReceivedEvent(tx, config);
                    }
                    else
                    {
                        log.warn("交易已确认但未找到关联订单: txHash={}, from={}, token={}, network={}",
                            tx.getTxHash(), tx.getFromAddress(), tx.getTokenSymbol(), tx.getNetworkType());
                    }
                }
                else
                {
                    publishTransactionFailedEvent(tx, config);
                }
                return 1;
            }
            else if (confirmations > tx.getConfirmations())
            {
                transactionService.updateTransactionStatus(tx.getTxId(), "pending", confirmations);
            }
            return 0;
        }
        catch (Exception e)
        {
            log.error("更新交易状态失败: txId={}, error={}", tx.getTxId(), e.getMessage());
            return 0;
        }
    }

    @Override
    public int manualScan(String networkType)
    {
        SysBlockchainConfig config = blockchainConfigService.selectBlockchainConfigByNetworkType(networkType);
        if (config == null)
        {
            log.warn("未找到网络配置: {}", networkType);
            return 0;
        }
        return scanNetwork(config);
    }

    /**
     * 创建交易记录
     */
    private PetPaymentTransaction createTransactionRecord(TransactionInfo txInfo, SysBlockchainConfig config)
    {
        PetPaymentTransaction tx = new PetPaymentTransaction();
        tx.setTxHash(txInfo.getTxHash());
        tx.setNetworkType(config.getNetworkType());
        tx.setBlockNumber(txInfo.getBlockNumber().longValue());
        tx.setBlockTimestamp(txInfo.getBlockTimestamp().longValue());
        tx.setFromAddress(txInfo.getFromAddress());
        tx.setToAddress(txInfo.getToAddress());
        tx.setTokenSymbol(txInfo.getTokenSymbol());
        tx.setTokenAddress(txInfo.getTokenAddress());
        tx.setAmount(new BigDecimal(txInfo.getDisplayAmount()));
        tx.setAmountDisplay(txInfo.getDisplayAmount());
        tx.setGasPrice(txInfo.getGasPrice() != null ? txInfo.getGasPrice().toString() : null);
        tx.setGasUsed(txInfo.getGasUsed() != null ? txInfo.getGasUsed().toString() : null);
        tx.setTxFee(txInfo.getTxFee());
        tx.setStatus("pending");
        tx.setConfirmations(0);
        tx.setIsProcessed("0");

        return tx;
    }

    /**
     * 发布交易检测事件
     */
    private void publishTransactionDetectedEvent(PetPaymentTransaction tx, SysBlockchainConfig config, TransactionInfo txInfo)
    {
        try
        {
            // 查询代币配置
            SysTokenConfig tokenConfig = tokenConfigService.selectTokenConfigByNetworkAndSymbol(
                config.getNetworkType(), tx.getTokenSymbol()
            );

            // 转换txFee为BigDecimal
            BigDecimal txFee = convertTxFeeToBigDecimal(tx.getTxFee());

            TransactionDetectedEvent event = new TransactionDetectedEvent(
                this,
                tx.getTxId(),
                tx.getTxHash(),
                config.getConfigId(),
                config.getNetworkName(),
                tokenConfig != null ? tokenConfig.getTokenId() : null,
                tx.getTokenSymbol(),
                tx.getFromAddress(),
                tx.getToAddress(),
                tx.getAmount(),
                tx.getUserId(),
                tx.getBlockNumber(),
                tx.getConfirmations(),
                config.getMinConfirmations(),
                txFee
            );

            eventPublisher.publishEvent(event);
            log.debug("已发布交易检测事件: txHash={}", tx.getTxHash());
        }
        catch (Exception e)
        {
            log.error("发布交易检测事件失败: txHash={}", tx.getTxHash(), e);
        }
    }

    /**
     * 发布交易确认事件
     */
    private void publishTransactionConfirmedEvent(PetPaymentTransaction tx, SysBlockchainConfig config)
    {
        try
        {
            // 查询代币配置
            SysTokenConfig tokenConfig = tokenConfigService.selectTokenConfigByNetworkAndSymbol(
                config.getNetworkType(), tx.getTokenSymbol()
            );

            // 转换txFee为BigDecimal
            BigDecimal txFee = convertTxFeeToBigDecimal(tx.getTxFee());

            TransactionConfirmedEvent event = new TransactionConfirmedEvent(
                this,
                tx.getTxId(),
                tx.getTxHash(),
                config.getConfigId(),
                config.getNetworkName(),
                tokenConfig != null ? tokenConfig.getTokenId() : null,
                tx.getTokenSymbol(),
                tx.getFromAddress(),
                tx.getToAddress(),
                tx.getAmount(),
                tx.getUserId(),
                tx.getBlockNumber(),
                tx.getConfirmations(),
                txFee,
                tx.getBlockTimestamp()
            );

            eventPublisher.publishEvent(event);
            log.info("已发布交易确认事件: txHash={}", tx.getTxHash());
        }
        catch (Exception e)
        {
            log.error("发布交易确认事件失败: txHash={}", tx.getTxHash(), e);
        }
    }

    /**
     * 发布交易失败事件
     */
    private void publishTransactionFailedEvent(PetPaymentTransaction tx, SysBlockchainConfig config)
    {
        try
        {
            // 查询代币配置
            SysTokenConfig tokenConfig = tokenConfigService.selectTokenConfigByNetworkAndSymbol(
                config.getNetworkType(), tx.getTokenSymbol()
            );

            // 转换txFee为BigDecimal
            BigDecimal txFee = convertTxFeeToBigDecimal(tx.getTxFee());

            TransactionFailedEvent event = new TransactionFailedEvent(
                this,
                tx.getTxId(),
                tx.getTxHash(),
                config.getConfigId(),
                config.getNetworkName(),
                tokenConfig != null ? tokenConfig.getTokenId() : null,
                tx.getTokenSymbol(),
                tx.getFromAddress(),
                tx.getToAddress(),
                tx.getAmount(),
                tx.getUserId(),
                "Transaction execution failed",
                tx.getBlockNumber(),
                txFee
            );

            eventPublisher.publishEvent(event);
            log.warn("已发布交易失败事件: txHash={}", tx.getTxHash());
        }
        catch (Exception e)
        {
            log.error("发布交易失败事件失败: txHash={}", tx.getTxHash(), e);
        }
    }

    /**
     * 发布支付到账事件
     */
    private void publishPaymentReceivedEvent(PetPaymentTransaction tx, SysBlockchainConfig config)
    {
        try
        {
            // 查询代币配置
            SysTokenConfig tokenConfig = tokenConfigService.selectTokenConfigByNetworkAndSymbol(
                config.getNetworkType(), tx.getTokenSymbol()
            );

            // 使用 businessId 作为订单号
            String orderNo = tx.getBusinessId() != null ? tx.getBusinessId().toString() : null;

            PaymentReceivedEvent event = new PaymentReceivedEvent(
                this,
                tx.getTxId(),
                tx.getTxHash(),
                config.getConfigId(),
                config.getNetworkName(),
                tokenConfig != null ? tokenConfig.getTokenId() : null,
                tx.getTokenSymbol(),
                tx.getFromAddress(),
                tx.getToAddress(),
                tx.getAmount(),
                tx.getUserId(),
                tx.getBlockNumber(),
                tx.getConfirmations(),
                tx.getStatus(),
                orderNo,
                tx.getBusinessType()
            );

            eventPublisher.publishEvent(event);
            log.info("已发布支付到账事件: txHash={}, userId={}, amount={} {}",
                tx.getTxHash(), tx.getUserId(), tx.getAmount(), tx.getTokenSymbol());
        }
        catch (Exception e)
        {
            log.error("发布支付到账事件失败: txHash={}", tx.getTxHash(), e);
        }
    }

    /**
     * 转换txFee为BigDecimal
     */
    private BigDecimal convertTxFeeToBigDecimal(String txFee)
    {
        if (txFee == null || txFee.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        try
        {
            return new BigDecimal(txFee);
        }
        catch (Exception e)
        {
            log.warn("txFee转换失败: {}", txFee);
            return BigDecimal.ZERO;
        }
    }
}
