package com.ruoyi.web3.domain;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 交易信息
 *
 * @author ruoyi
 */
public class TransactionInfo
{
    /** 交易哈希 */
    private String txHash;

    /** 区块号 */
    private BigInteger blockNumber;

    /** 区块时间戳 */
    private BigInteger blockTimestamp;

    /** 发送方地址 */
    private String fromAddress;

    /** 接收方地址 */
    private String toAddress;

    /** 交易金额（原始值） */
    private BigInteger value;

    /** 代币符号 */
    private String tokenSymbol;

    /** 代币合约地址 */
    private String tokenAddress;

    /** 小数位数 */
    private int decimals;

    /** Gas价格 */
    private BigInteger gasPrice;

    /** Gas使用量 */
    private BigInteger gasUsed;

    /** 是否原生币转账 */
    private boolean nativeTransfer;

    public TransactionInfo()
    {
    }

    /**
     * 获取显示金额
     */
    public String getDisplayAmount()
    {
        if (value == null)
        {
            return "0";
        }
        BigDecimal amount = new BigDecimal(value);
        BigDecimal divisor = BigDecimal.TEN.pow(decimals);
        return amount.divide(divisor, decimals, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString();
    }

    /**
     * 获取交易手续费
     */
    public String getTxFee()
    {
        if (gasPrice == null || gasUsed == null)
        {
            return "0";
        }
        BigDecimal fee = new BigDecimal(gasPrice.multiply(gasUsed));
        BigDecimal divisor = BigDecimal.TEN.pow(18);
        return fee.divide(divisor, 18, BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString();
    }

    public String getTxHash()
    {
        return txHash;
    }

    public void setTxHash(String txHash)
    {
        this.txHash = txHash;
    }

    public BigInteger getBlockNumber()
    {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber)
    {
        this.blockNumber = blockNumber;
    }

    public BigInteger getBlockTimestamp()
    {
        return blockTimestamp;
    }

    public void setBlockTimestamp(BigInteger blockTimestamp)
    {
        this.blockTimestamp = blockTimestamp;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public String getToAddress()
    {
        return toAddress;
    }

    public void setToAddress(String toAddress)
    {
        this.toAddress = toAddress;
    }

    public BigInteger getValue()
    {
        return value;
    }

    public void setValue(BigInteger value)
    {
        this.value = value;
    }

    public String getTokenSymbol()
    {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol)
    {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenAddress()
    {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress)
    {
        this.tokenAddress = tokenAddress;
    }

    public int getDecimals()
    {
        return decimals;
    }

    public void setDecimals(int decimals)
    {
        this.decimals = decimals;
    }

    public BigInteger getGasPrice()
    {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice)
    {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasUsed()
    {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed)
    {
        this.gasUsed = gasUsed;
    }

    public boolean isNativeTransfer()
    {
        return nativeTransfer;
    }

    public void setNativeTransfer(boolean nativeTransfer)
    {
        this.nativeTransfer = nativeTransfer;
    }
}
