package com.ruoyi.web3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Web3配置
 *
 * @author ruoyi
 */
@Configuration
@ConfigurationProperties(prefix = "pet.web3")
public class Web3Config
{
    /** 是否启用区块链扫描 */
    private boolean scanEnabled = true;

    /** 扫描间隔（毫秒） */
    private long scanInterval = 15000;

    /** Infura项目ID（用于以太坊RPC） */
    private String infuraProjectId;

    /** 确认交易后是否自动处理 */
    private boolean autoProcessTransaction = true;

    public boolean isScanEnabled()
    {
        return scanEnabled;
    }

    public void setScanEnabled(boolean scanEnabled)
    {
        this.scanEnabled = scanEnabled;
    }

    public long getScanInterval()
    {
        return scanInterval;
    }

    public void setScanInterval(long scanInterval)
    {
        this.scanInterval = scanInterval;
    }

    public String getInfuraProjectId()
    {
        return infuraProjectId;
    }

    public void setInfuraProjectId(String infuraProjectId)
    {
        this.infuraProjectId = infuraProjectId;
    }

    public boolean isAutoProcessTransaction()
    {
        return autoProcessTransaction;
    }

    public void setAutoProcessTransaction(boolean autoProcessTransaction)
    {
        this.autoProcessTransaction = autoProcessTransaction;
    }
}
