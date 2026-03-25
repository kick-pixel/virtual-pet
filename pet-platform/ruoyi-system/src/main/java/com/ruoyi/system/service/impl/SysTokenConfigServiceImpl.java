package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysTokenConfig;
import com.ruoyi.system.mapper.SysTokenConfigMapper;
import com.ruoyi.system.service.ISysTokenConfigService;

/**
 * 代币配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SysTokenConfigServiceImpl implements ISysTokenConfigService
{
    @Autowired
    private SysTokenConfigMapper tokenConfigMapper;

    /**
     * 查询代币配置
     *
     * @param tokenId 代币ID
     * @return 代币配置
     */
    @Override
    public SysTokenConfig selectTokenConfigById(Long tokenId)
    {
        return tokenConfigMapper.selectTokenConfigById(tokenId);
    }

    /**
     * 根据网络类型和代币符号查询配置
     *
     * @param networkType 网络类型
     * @param tokenSymbol 代币符号
     * @return 代币配置
     */
    @Override
    public SysTokenConfig selectTokenConfigByNetworkAndSymbol(String networkType, String tokenSymbol)
    {
        return tokenConfigMapper.selectTokenConfigByNetworkAndSymbol(networkType, tokenSymbol);
    }

    /**
     * 查询代币配置列表
     *
     * @param tokenConfig 代币配置
     * @return 代币配置集合
     */
    @Override
    public List<SysTokenConfig> selectTokenConfigList(SysTokenConfig tokenConfig)
    {
        return tokenConfigMapper.selectTokenConfigList(tokenConfig);
    }

    /**
     * 根据网络类型查询代币列表
     *
     * @param networkType 网络类型
     * @return 代币配置集合
     */
    @Override
    public List<SysTokenConfig> selectTokenConfigByNetwork(String networkType)
    {
        return tokenConfigMapper.selectTokenConfigByNetwork(networkType);
    }

    /**
     * 查询已启用的代币配置列表
     *
     * @return 代币配置集合
     */
    @Override
    public List<SysTokenConfig> selectEnabledTokenConfigList()
    {
        return tokenConfigMapper.selectEnabledTokenConfigList();
    }

    /**
     * 新增代币配置
     *
     * @param tokenConfig 代币配置
     * @return 结果
     */
    @Override
    public int insertTokenConfig(SysTokenConfig tokenConfig)
    {
        return tokenConfigMapper.insertTokenConfig(tokenConfig);
    }

    /**
     * 修改代币配置
     *
     * @param tokenConfig 代币配置
     * @return 结果
     */
    @Override
    public int updateTokenConfig(SysTokenConfig tokenConfig)
    {
        return tokenConfigMapper.updateTokenConfig(tokenConfig);
    }

    /**
     * 删除代币配置
     *
     * @param tokenId 代币ID
     * @return 结果
     */
    @Override
    public int deleteTokenConfigById(Long tokenId)
    {
        return tokenConfigMapper.deleteTokenConfigById(tokenId);
    }

    /**
     * 批量删除代币配置
     *
     * @param tokenIds 需要删除的代币ID
     * @return 结果
     */
    @Override
    public int deleteTokenConfigByIds(Long[] tokenIds)
    {
        return tokenConfigMapper.deleteTokenConfigByIds(tokenIds);
    }
}
