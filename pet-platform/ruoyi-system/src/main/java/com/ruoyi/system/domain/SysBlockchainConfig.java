package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 区块链网络配置对象 sys_blockchain_config
 *
 * @author ruoyi
 */
public class SysBlockchainConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    @Excel(name = "Config ID", cellType = ColumnType.NUMERIC)
    private Long configId;

    /** 网络类型（ethereum_mainnet/ethereum_testnet/bsc_mainnet/bsc_testnet） */
    @Excel(name = "Network Type")
    private String networkType;

    /** 网络名称 */
    @Excel(name = "Network Name")
    private String networkName;

    /** 链ID */
    @Excel(name = "Chain ID", cellType = ColumnType.NUMERIC)
    private Integer chainId;

    /** RPC节点URL */
    private String rpcUrl;

    /** 区块链浏览器URL */
    private String explorerUrl;

    /** 平台收款钱包地址 */
    @Excel(name = "Wallet Address")
    private String walletAddress;

    /** 扫描起始区块高度 */
    private Long scanStartBlock;

    /** 当前扫描到的区块高度 */
    private Long scanCurrentBlock;

    /** 每次扫描区块数量 */
    private Integer scanBatchSize;

    /** 最小确认数 */
    private Integer minConfirmations;

    /** 状态（0启用 1停用） */
    @Excel(name = "Status", readConverterExp = "0=Enabled,1=Disabled")
    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
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

    public Integer getChainId()
    {
        return chainId;
    }

    public void setChainId(Integer chainId)
    {
        this.chainId = chainId;
    }

    public String getRpcUrl()
    {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl)
    {
        this.rpcUrl = rpcUrl;
    }

    public String getExplorerUrl()
    {
        return explorerUrl;
    }

    public void setExplorerUrl(String explorerUrl)
    {
        this.explorerUrl = explorerUrl;
    }

    public String getWalletAddress()
    {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress)
    {
        this.walletAddress = walletAddress;
    }

    public Long getScanStartBlock()
    {
        return scanStartBlock;
    }

    public void setScanStartBlock(Long scanStartBlock)
    {
        this.scanStartBlock = scanStartBlock;
    }

    public Long getScanCurrentBlock()
    {
        return scanCurrentBlock;
    }

    public void setScanCurrentBlock(Long scanCurrentBlock)
    {
        this.scanCurrentBlock = scanCurrentBlock;
    }

    public Integer getScanBatchSize()
    {
        return scanBatchSize;
    }

    public void setScanBatchSize(Integer scanBatchSize)
    {
        this.scanBatchSize = scanBatchSize;
    }

    public Integer getMinConfirmations()
    {
        return minConfirmations;
    }

    public void setMinConfirmations(Integer minConfirmations)
    {
        this.minConfirmations = minConfirmations;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("networkType", getNetworkType())
            .append("networkName", getNetworkName())
            .append("chainId", getChainId())
            .append("rpcUrl", getRpcUrl())
            .append("walletAddress", getWalletAddress())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
