package com.ruoyi.web3.service.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.service.ISysTokenConfigService;
import com.ruoyi.web3.config.Web3Config;
import com.ruoyi.web3.domain.TransactionInfo;
import com.ruoyi.web3.service.IBlockchainService;

/**
 * 区块链服务实现
 *
 * @author ruoyi
 */
@Service
public class BlockchainServiceImpl implements IBlockchainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    /** ERC20 Transfer事件签名 */
    private static final String TRANSFER_EVENT_SIGNATURE = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    @Autowired
    private Web3Config web3Config;

    @Autowired
    private ISysTokenConfigService tokenConfigService;

    /** Web3j客户端缓存 */
    private final ConcurrentHashMap<String, Web3j> web3jClients = new ConcurrentHashMap<>();

    /**
     * 获取Web3j客户端
     */
    private Web3j getWeb3jClient(SysBlockchainConfig config)
    {
        return web3jClients.computeIfAbsent(config.getNetworkType(), key -> {
            String rpcUrl = config.getRpcUrl();
            // 如果是Infura URL，追加项目ID
            if (rpcUrl.contains("infura.io") && StringUtils.isNotEmpty(web3Config.getInfuraProjectId()))
            {
                if (!rpcUrl.endsWith("/"))
                {
                    rpcUrl = rpcUrl + "/";
                }
                rpcUrl = rpcUrl + web3Config.getInfuraProjectId();
            }
            log.info("创建Web3j客户端: network={}, rpcUrl={}", config.getNetworkType(), rpcUrl);
            return Web3j.build(new HttpService(rpcUrl));
        });
    }

    @Override
    public BigInteger getCurrentBlockNumber(SysBlockchainConfig config)
    {
        try
        {
            Web3j web3j = getWeb3jClient(config);
            return web3j.ethBlockNumber().send().getBlockNumber();
        }
        catch (Exception e)
        {
            log.error("获取区块高度失败: network={}, error={}", config.getNetworkType(), e);
            return BigInteger.ZERO;
        }
    }

    @Override
    public List<TransactionInfo> scanTransactions(SysBlockchainConfig config, BigInteger fromBlock, BigInteger toBlock)
    {
        List<TransactionInfo> transactions = new ArrayList<>();
        Web3j web3j = getWeb3jClient(config);
        String walletAddress = config.getWalletAddress().toLowerCase();

        // 获取该网络支持的代币列表
        List<SysTokenConfig> tokens = tokenConfigService.selectTokenConfigByNetwork(config.getNetworkType());

        try
        {
            for (BigInteger blockNum = fromBlock; blockNum.compareTo(toBlock) <= 0; blockNum = blockNum.add(BigInteger.ONE))
            {
                EthBlock ethBlock = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNum), true).send();

                if (ethBlock.getBlock() == null)
                {
                    continue;
                }

                EthBlock.Block block = ethBlock.getBlock();
                BigInteger blockTimestamp = block.getTimestamp();

                for (EthBlock.TransactionResult txResult : block.getTransactions())
                {
                    Transaction tx = (Transaction) txResult.get();

                    // 检查原生币转账（ETH/BNB）
                    if (tx.getTo() != null && tx.getTo().equalsIgnoreCase(walletAddress))
                    {
                        if (tx.getValue().compareTo(BigInteger.ZERO) > 0)
                        {
                            TransactionInfo info = createNativeTransferInfo(tx, blockTimestamp, config, tokens);
                            if (info != null)
                            {
                                transactions.add(info);
                                log.info("发现原生币转账: hash={}, from={}, value={}",
                                    tx.getHash(), tx.getFrom(), info.getDisplayAmount());
                            }
                        }
                    }

                    // 检查ERC20/BEP20代币转账
                    if (tx.getInput() != null && tx.getInput().length() >= 138)
                    {
                        TransactionInfo tokenTransfer = parseTokenTransfer(tx, blockTimestamp, walletAddress, tokens, web3j);
                        if (tokenTransfer != null)
                        {
                            transactions.add(tokenTransfer);
                            log.info("发现代币转账: hash={}, token={}, from={}, value={}",
                                tx.getHash(), tokenTransfer.getTokenSymbol(),
                                tokenTransfer.getFromAddress(), tokenTransfer.getDisplayAmount());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("扫描区块失败: network={}, blocks={}-{}, error={}",
                config.getNetworkType(), fromBlock, toBlock, e.getMessage());
        }

        return transactions;
    }

    /**
     * 创建原生币转账信息
     */
    private TransactionInfo createNativeTransferInfo(Transaction tx, BigInteger blockTimestamp,
                                                      SysBlockchainConfig config, List<SysTokenConfig> tokens)
    {
        // 查找原生币配置
        SysTokenConfig nativeToken = tokens.stream()
            .filter(t -> "1".equals(t.getIsNative()))
            .findFirst()
            .orElse(null);

        if (nativeToken == null)
        {
            return null;
        }

        TransactionInfo info = new TransactionInfo();
        info.setTxHash(tx.getHash());
        info.setBlockNumber(tx.getBlockNumber());
        info.setBlockTimestamp(blockTimestamp);
        info.setFromAddress(tx.getFrom());
        info.setToAddress(tx.getTo());
        info.setValue(tx.getValue());
        info.setTokenSymbol(nativeToken.getTokenSymbol());
        info.setDecimals(nativeToken.getDecimals());
        info.setGasPrice(tx.getGasPrice());
        info.setNativeTransfer(true);

        return info;
    }

    /**
     * 解析ERC20代币转账
     */
    private TransactionInfo parseTokenTransfer(Transaction tx, BigInteger blockTimestamp,
                                                String walletAddress, List<SysTokenConfig> tokens, Web3j web3j)
    {
        String input = tx.getInput();

        // 检查是否是transfer方法调用 (0xa9059cbb)
        if (!input.startsWith("0xa9059cbb"))
        {
            return null;
        }

        try
        {
            // 解析目标地址（去掉0xa9059cbb后的前64位，取后40位）
            String toAddressHex = "0x" + input.substring(34, 74);
            if (!toAddressHex.equalsIgnoreCase(walletAddress))
            {
                return null;
            }

            // 查找代币配置
            String contractAddress = tx.getTo();
            SysTokenConfig tokenConfig = tokens.stream()
                .filter(t -> contractAddress.equalsIgnoreCase(t.getContractAddress()))
                .findFirst()
                .orElse(null);

            if (tokenConfig == null)
            {
                return null;
            }

            // 解析转账金额
            String valueHex = input.substring(74, 138);
            BigInteger value = new BigInteger(valueHex, 16);

            // 获取交易回执以确认成功
            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(tx.getHash()).send();
            if (receiptResponse.getTransactionReceipt().isEmpty())
            {
                return null;
            }

            TransactionReceipt receipt = receiptResponse.getTransactionReceipt().get();
            if (!"0x1".equals(receipt.getStatus()))
            {
                return null;
            }

            TransactionInfo info = new TransactionInfo();
            info.setTxHash(tx.getHash());
            info.setBlockNumber(tx.getBlockNumber());
            info.setBlockTimestamp(blockTimestamp);
            info.setFromAddress(tx.getFrom());
            info.setToAddress(toAddressHex);
            info.setValue(value);
            info.setTokenSymbol(tokenConfig.getTokenSymbol());
            info.setTokenAddress(contractAddress);
            info.setDecimals(tokenConfig.getDecimals());
            info.setGasPrice(tx.getGasPrice());
            info.setGasUsed(receipt.getGasUsed());
            info.setNativeTransfer(false);

            return info;
        }
        catch (Exception e)
        {
            log.debug("解析代币转账失败: hash={}, error={}", tx.getHash(), e.getMessage());
            return null;
        }
    }

    @Override
    public int getTransactionConfirmations(SysBlockchainConfig config, String txHash)
    {
        try
        {
            Web3j web3j = getWeb3jClient(config);

            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
            if (receiptResponse.getTransactionReceipt().isEmpty())
            {
                return 0;
            }

            TransactionReceipt receipt = receiptResponse.getTransactionReceipt().get();
            BigInteger txBlockNumber = receipt.getBlockNumber();
            BigInteger currentBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            return currentBlockNumber.subtract(txBlockNumber).intValue() + 1;
        }
        catch (Exception e)
        {
            log.error("获取交易确认数失败: hash={}, error={}", txHash, e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean isTransactionSuccessful(SysBlockchainConfig config, String txHash)
    {
        try
        {
            Web3j web3j = getWeb3jClient(config);

            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(txHash).send();
            if (receiptResponse.getTransactionReceipt().isEmpty())
            {
                return false;
            }

            TransactionReceipt receipt = receiptResponse.getTransactionReceipt().get();
            return "0x1".equals(receipt.getStatus());
        }
        catch (Exception e)
        {
            log.error("验证交易状态失败: hash={}, error={}", txHash, e.getMessage());
            return false;
        }
    }

    @Override
    public TransactionInfo getTransaction(SysBlockchainConfig config, String txHash)
    {
        try
        {
            Web3j web3j = getWeb3jClient(config);

            Transaction tx = web3j.ethGetTransactionByHash(txHash).send().getTransaction().orElse(null);
            if (tx == null)
            {
                return null;
            }

            EthBlock ethBlock = web3j.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(tx.getBlockNumber()), false).send();

            BigInteger blockTimestamp = ethBlock.getBlock() != null
                ? ethBlock.getBlock().getTimestamp()
                : BigInteger.ZERO;

            List<SysTokenConfig> tokens = tokenConfigService.selectTokenConfigByNetwork(config.getNetworkType());

            // 检查是否是代币转账
            if (tx.getInput() != null && tx.getInput().startsWith("0xa9059cbb"))
            {
                return parseTokenTransfer(tx, blockTimestamp, config.getWalletAddress().toLowerCase(), tokens, web3j);
            }

            // 原生币转账
            return createNativeTransferInfo(tx, blockTimestamp, config, tokens);
        }
        catch (Exception e)
        {
            log.error("获取交易详情失败: hash={}, error={}", txHash, e.getMessage());
            return null;
        }
    }
}
