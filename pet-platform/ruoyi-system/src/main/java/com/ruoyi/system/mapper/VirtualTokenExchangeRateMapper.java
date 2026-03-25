package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.VirtualTokenExchangeRate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代币汇率配置 Mapper 接口
 *
 * @author ruoyi
 */
public interface VirtualTokenExchangeRateMapper
{
    /**
     * 根据代币符号查询汇率配置（启用状态）
     *
     * @param tokenSymbol 代币符号（如 BNB, ETH, USDT）
     * @return 汇率配置对象，如果不存在或未启用返回 null
     */
    VirtualTokenExchangeRate selectByTokenSymbol(@Param("tokenSymbol") String tokenSymbol);

    /**
     * 查询所有启用的汇率配置（按优先级排序）
     *
     * @return 汇率配置列表
     */
    List<VirtualTokenExchangeRate> selectEnabledRates();

    /**
     * 查询所有汇率配置（包括禁用的）
     *
     * @return 汇率配置列表
     */
    List<VirtualTokenExchangeRate> selectAllRates();

    /**
     * 插入汇率配置
     *
     * @param rate 汇率配置对象
     * @return 影响的行数
     */
    int insert(VirtualTokenExchangeRate rate);

    /**
     * 更新汇率配置
     *
     * @param rate 汇率配置对象（必须包含 rateId）
     * @return 影响的行数
     */
    int update(VirtualTokenExchangeRate rate);

    /**
     * 更新汇率（仅更新汇率字段）
     *
     * @param tokenSymbol 代币符号
     * @param rate 新汇率
     * @param source 汇率来源
     * @return 影响的行数
     */
    int updateRate(@Param("tokenSymbol") String tokenSymbol,
                   @Param("rate") java.math.BigDecimal rate,
                   @Param("source") String source);

    /**
     * 根据 ID 删除汇率配置
     *
     * @param rateId 汇率ID
     * @return 影响的行数
     */
    int deleteById(@Param("rateId") Long rateId);

    /**
     * 根据代币符号删除汇率配置
     *
     * @param tokenSymbol 代币符号
     * @return 影响的行数
     */
    int deleteByTokenSymbol(@Param("tokenSymbol") String tokenSymbol);
}
