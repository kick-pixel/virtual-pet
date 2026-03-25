package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysBlockchainConfig;
import com.ruoyi.system.mapper.SysBlockchainConfigMapper;
import com.ruoyi.system.service.ISysBlockchainConfigService;

/**
 * 区块链网络配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SysBlockchainConfigServiceImpl implements ISysBlockchainConfigService
{
    @Autowired
    private SysBlockchainConfigMapper blockchainConfigMapper;

    /**
     * 查询区块链配置
     *
     * @param configId 配置ID
     * @return 区块链配置
     */
    @Override
    public SysBlockchainConfig selectBlockchainConfigById(Long configId)
    {
        return blockchainConfigMapper.selectBlockchainConfigById(configId);
    }

    /**
     * 根据网络类型查询配置
     *
     * @param networkType 网络类型
     * @return 区块链配置
     */
    @Override
    public SysBlockchainConfig selectBlockchainConfigByNetworkType(String networkType)
    {
        return blockchainConfigMapper.selectBlockchainConfigByNetworkType(networkType);
    }

    /**
     * 查询区块链配置列表
     *
     * @param config 区块链配置
     * @return 区块链配置集合
     */
    @Override
    public List<SysBlockchainConfig> selectBlockchainConfigList(SysBlockchainConfig config)
    {
        return blockchainConfigMapper.selectBlockchainConfigList(config);
    }

    /**
     * 查询已启用的区块链配置列表
     *
     * @return 区块链配置集合
     */
    @Override
    public List<SysBlockchainConfig> selectEnabledBlockchainConfigList()
    {
        return blockchainConfigMapper.selectEnabledBlockchainConfigList();
    }

    /**
     * 新增区块链配置
     *
     * @param config 区块链配置
     * @return 结果
     */
    @Override
    public int insertBlockchainConfig(SysBlockchainConfig config)
    {
        return blockchainConfigMapper.insertBlockchainConfig(config);
    }

    /**
     * 修改区块链配置
     *
     * @param config 区块链配置
     * @return 结果
     */
    @Override
    public int updateBlockchainConfig(SysBlockchainConfig config)
    {
        return blockchainConfigMapper.updateBlockchainConfig(config);
    }

    /**
     * 更新扫描区块高度
     *
     * @param configId 配置ID
     * @param blockNumber 当前区块高度
     * @return 结果
     */
    @Override
    public int updateScanCurrentBlock(Long configId, Long blockNumber)
    {
        return blockchainConfigMapper.updateScanCurrentBlock(configId, blockNumber);
    }

    /**
     * 删除区块链配置
     *
     * @param configId 配置ID
     * @return 结果
     */
    @Override
    public int deleteBlockchainConfigById(Long configId)
    {
        return blockchainConfigMapper.deleteBlockchainConfigById(configId);
    }

    /**
     * 批量删除区块链配置
     *
     * @param configIds 需要删除的配置ID
     * @return 结果
     */
    @Override
    public int deleteBlockchainConfigByIds(Long[] configIds)
    {
        return blockchainConfigMapper.deleteBlockchainConfigByIds(configIds);
    }
}
