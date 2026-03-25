package com.ruoyi.system.service;

import com.ruoyi.system.domain.VirtualTokenExchangeRate;

import java.math.BigDecimal;
import java.util.List;

/**
 * 代币汇率服务接口
 *
 * 提供代币汇率查询、更新、管理功能
 *
 * @author ruoyi
 */
public interface ITokenExchangeRateService
{
    /**
     * 根据代币符号获取汇率配置
     *
     * 返回启用状态的汇率配置，如果启用了手动汇率则使用手动汇率，否则使用自动汇率
     *
     * @param tokenSymbol 代币符号（如 BNB, ETH, USDT）
     * @return 汇率配置对象，如果不存在返回 null
     */
    VirtualTokenExchangeRate getExchangeRate(String tokenSymbol);

    /**
     * 根据代币金额计算积分数量
     *
     * 计算流程：
     * 1. 查询代币汇率（1 Token = X USDT）
     * 2. 计算 USDT 等价金额（tokenAmount * rate）
     * 3. 计算积分（usdtAmount * creditsPerUsdt）
     *
     * @param tokenSymbol 代币符号
     * @param tokenAmount 代币金额
     * @return 积分数量，如果汇率不存在返回 null
     */
    Long calculateCredits(String tokenSymbol, BigDecimal tokenAmount);

    /**
     * 查询所有启用的汇率配置（按优先级排序）
     *
     * @return 汇率配置列表
     */
    List<VirtualTokenExchangeRate> listEnabledRates();

    /**
     * 查询所有汇率配置（包括禁用的）
     *
     * @return 汇率配置列表
     */
    List<VirtualTokenExchangeRate> listAllRates();

    /**
     * 更新代币汇率
     *
     * @param tokenSymbol 代币符号
     * @param newRate 新汇率（1 Token = X USDT）
     * @param source 汇率来源（如 "binance", "coingecko", "manual"）
     * @return 是否更新成功
     */
    boolean updateExchangeRate(String tokenSymbol, BigDecimal newRate, String source);

    /**
     * 新增汇率配置
     *
     * @param rate 汇率配置对象
     * @return 是否新增成功
     */
    boolean insertExchangeRate(VirtualTokenExchangeRate rate);

    /**
     * 修改汇率配置
     *
     * @param rate 汇率配置对象（必须包含 rateId）
     * @return 是否修改成功
     */
    boolean updateExchangeRateConfig(VirtualTokenExchangeRate rate);

    /**
     * 删除汇率配置
     *
     * @param rateId 汇率ID
     * @return 是否删除成功
     */
    boolean deleteExchangeRate(Long rateId);

    /**
     * 从外部 API 同步汇率（可选功能）
     *
     * 从 CoinGecko/Binance 等 API 获取最新汇率并更新数据库
     *
     * @param tokenSymbol 代币符号
     * @return 是否同步成功
     */
    boolean syncExchangeRateFromApi(String tokenSymbol);

    /**
     * 批量同步所有代币汇率
     *
     * @return 成功同步的数量
     */
    int syncAllExchangeRates();
}
