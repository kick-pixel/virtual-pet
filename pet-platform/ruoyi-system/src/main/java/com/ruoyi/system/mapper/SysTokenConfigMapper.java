package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysTokenConfig;

/**
 * 代币配置 数据层
 *
 * @author ruoyi
 */
public interface SysTokenConfigMapper
{
    /**
     * 查询代币配置
     *
     * @param tokenId 代币ID
     * @return 代币配置
     */
    public SysTokenConfig selectTokenConfigById(Long tokenId);

    /**
     * 根据网络类型和代币符号查询配置
     *
     * @param networkType 网络类型
     * @param tokenSymbol 代币符号
     * @return 代币配置
     */
    public SysTokenConfig selectTokenConfigByNetworkAndSymbol(String networkType, String tokenSymbol);

    /**
     * 查询代币配置列表
     *
     * @param tokenConfig 代币配置
     * @return 代币配置集合
     */
    public List<SysTokenConfig> selectTokenConfigList(SysTokenConfig tokenConfig);

    /**
     * 根据网络类型查询代币列表
     *
     * @param networkType 网络类型
     * @return 代币配置集合
     */
    public List<SysTokenConfig> selectTokenConfigByNetwork(String networkType);

    /**
     * 查询已启用的代币配置列表
     *
     * @return 代币配置集合
     */
    public List<SysTokenConfig> selectEnabledTokenConfigList();

    /**
     * 新增代币配置
     *
     * @param tokenConfig 代币配置
     * @return 结果
     */
    public int insertTokenConfig(SysTokenConfig tokenConfig);

    /**
     * 修改代币配置
     *
     * @param tokenConfig 代币配置
     * @return 结果
     */
    public int updateTokenConfig(SysTokenConfig tokenConfig);

    /**
     * 删除代币配置
     *
     * @param tokenId 代币ID
     * @return 结果
     */
    public int deleteTokenConfigById(Long tokenId);

    /**
     * 批量删除代币配置
     *
     * @param tokenIds 需要删除的代币ID
     * @return 结果
     */
    public int deleteTokenConfigByIds(Long[] tokenIds);
}
