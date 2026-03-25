package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付交易记录对象 pet_payment_transaction
 *
 * @author ruoyi
 */
public class PetPaymentTransaction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交易ID */
    @Excel(name = "Transaction ID", cellType = ColumnType.NUMERIC)
    private Long txId;

    /** 交易哈希 */
    @Excel(name = "Transaction Hash")
    private String txHash;

    /** 网络类型 */
    @Excel(name = "Network Type")
    private String networkType;

    /** 网络名称（关联查询字段） */
    private String networkName;

    /** 区块浏览器URL（关联查询字段） */
    private String explorerUrl;

    /** 区块高度 */
    @Excel(name = "Block Number", cellType = ColumnType.NUMERIC)
    private Long blockNumber;

    /** 区块时间戳 */
    private Long blockTimestamp;

    /** 交易时间（从blockTimestamp转换） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transactionTime;

    /** 发送方地址 */
    @Excel(name = "From Address")
    private String fromAddress;

    /** 接收方地址 */
    @Excel(name = "To Address")
    private String toAddress;

    /** 代币符号 */
    @Excel(name = "Token Symbol")
    private String tokenSymbol;

    /** 代币合约地址 */
    private String tokenAddress;

    /** 转账金额（原始值） */
    private BigDecimal amount;

    /** 显示金额 */
    @Excel(name = "Amount")
    private String amountDisplay;

    /** Gas价格 */
    private String gasPrice;

    /** Gas消耗 */
    private String gasUsed;

    /** 交易手续费 */
    private String txFee;

    /** 交易状态（pending/confirmed/failed） */
    @Excel(name = "Status", readConverterExp = "pending=Pending,confirmed=Confirmed,failed=Failed")
    private String status;

    /** 确认数 */
    private Integer confirmations;

    /** 关联用户ID */
    private Long userId;

    /** 业务类型（recharge/purchase） */
    @Excel(name = "Business Type")
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    /** 是否已处理（0否 1是） */
    @Excel(name = "Processed", readConverterExp = "0=No,1=Yes")
    private String isProcessed;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processTime;

    public Long getTxId()
    {
        return txId;
    }

    public void setTxId(Long txId)
    {
        this.txId = txId;
    }

    public String getTxHash()
    {
        return txHash;
    }

    public void setTxHash(String txHash)
    {
        this.txHash = txHash;
    }

    public String getNetworkType()
    {
        return networkType;
    }

    public void setNetworkType(String networkType)
    {
        this.networkType = networkType;
    }

    public String getNetworkName()
    {
        return networkName;
    }

    public void setNetworkName(String networkName)
    {
        this.networkName = networkName;
    }

    public String getExplorerUrl()
    {
        return explorerUrl;
    }

    public void setExplorerUrl(String explorerUrl)
    {
        this.explorerUrl = explorerUrl;
    }

    public Long getBlockNumber()
    {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber)
    {
        this.blockNumber = blockNumber;
    }

    public Long getBlockTimestamp()
    {
        return blockTimestamp;
    }

    public void setBlockTimestamp(Long blockTimestamp)
    {
        this.blockTimestamp = blockTimestamp;
        // 自动转换为 transactionTime
        if (blockTimestamp != null && blockTimestamp > 0)
        {
            this.transactionTime = new Date(blockTimestamp * 1000);
        }
    }

    public Date getTransactionTime()
    {
        if (transactionTime == null && blockTimestamp != null && blockTimestamp > 0)
        {
            return new Date(blockTimestamp * 1000);
        }
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime)
    {
        this.transactionTime = transactionTime;
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

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getAmountDisplay()
    {
        return amountDisplay;
    }

    public void setAmountDisplay(String amountDisplay)
    {
        this.amountDisplay = amountDisplay;
    }

    public String getGasPrice()
    {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice)
    {
        this.gasPrice = gasPrice;
    }

    public String getGasUsed()
    {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed)
    {
        this.gasUsed = gasUsed;
    }

    public String getTxFee()
    {
        return txFee;
    }

    public void setTxFee(String txFee)
    {
        this.txFee = txFee;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getConfirmations()
    {
        return confirmations;
    }

    public void setConfirmations(Integer confirmations)
    {
        this.confirmations = confirmations;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getIsProcessed()
    {
        return isProcessed;
    }

    public void setIsProcessed(String isProcessed)
    {
        this.isProcessed = isProcessed;
    }

    public Date getProcessTime()
    {
        return processTime;
    }

    public void setProcessTime(Date processTime)
    {
        this.processTime = processTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("txId", getTxId())
            .append("txHash", getTxHash())
            .append("networkType", getNetworkType())
            .append("blockNumber", getBlockNumber())
            .append("fromAddress", getFromAddress())
            .append("toAddress", getToAddress())
            .append("tokenSymbol", getTokenSymbol())
            .append("amountDisplay", getAmountDisplay())
            .append("status", getStatus())
            .append("userId", getUserId())
            .append("isProcessed", getIsProcessed())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
