package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysBlockchainConfig;

/**
 * 区块链网络配置 数据层
 *
 * @author ruoyi
 */
public interface SysBlockchainConfigMapper
{
    /**
     * 查询区块链配置
     *
     * @param configId 配置ID
     * @return 区块链配置
     */
    public SysBlockchainConfig selectBlockchainConfigById(Long configId);

    /**
     * 根据网络类型查询配置
     *
     * @param networkType 网络类型
     * @return 区块链配置
     */
    public SysBlockchainConfig selectBlockchainConfigByNetworkType(String networkType);

    /**
     * 根据 chainId 查询已启用的配置
     *
     * @param chainId 链 ID
     * @return 区块链配置
     */
    public SysBlockchainConfig selectEnabledBlockchainConfigByChainId(Long chainId);

    /**
     * 查询区块链配置列表
     *
     * @param config 区块链配置
     * @return 区块链配置集合
     */
    public List<SysBlockchainConfig> selectBlockchainConfigList(SysBlockchainConfig config);

    /**
     * 查询已启用的区块链配置列表
     *
     * @return 区块链配置集合
     */
    public List<SysBlockchainConfig> selectEnabledBlockchainConfigList();

    /**
     * 新增区块链配置
     *
     * @param config 区块链配置
     * @return 结果
     */
    public int insertBlockchainConfig(SysBlockchainConfig config);

    /**
     * 修改区块链配置
     *
     * @param config 区块链配置
     * @return 结果
     */
    public int updateBlockchainConfig(SysBlockchainConfig config);

    /**
     * 更新扫描区块高度
     *
     * @param configId 配置ID
     * @param blockNumber 当前区块高度
     * @return 结果
     */
    public int updateScanCurrentBlock(Long configId, Long blockNumber);

    /**
     * 删除区块链配置
     *
     * @param configId 配置ID
     * @return 结果
     */
    public int deleteBlockchainConfigById(Long configId);

    /**
     * 批量删除区块链配置
     *
     * @param configIds 需要删除的配置ID
     * @return 结果
     */
    public int deleteBlockchainConfigByIds(Long[] configIds);
}
