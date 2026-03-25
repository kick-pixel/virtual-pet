package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.VirtualTokenExchangeRate;
import com.ruoyi.system.mapper.VirtualTokenExchangeRateMapper;
import com.ruoyi.system.service.ITokenExchangeRateService;
import com.ruoyi.system.service.ICoinGeckoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 代币汇率服务实现
 *
 * @author ruoyi
 */
@Service
public class TokenExchangeRateServiceImpl implements ITokenExchangeRateService
{
    private static final Logger log = LoggerFactory.getLogger(TokenExchangeRateServiceImpl.class);

    @Autowired
    private VirtualTokenExchangeRateMapper exchangeRateMapper;

    @Autowired
    private ICoinGeckoService coinGeckoService;

    /**
     * 根据代币符号获取汇率配置
     */
    @Override
    public VirtualTokenExchangeRate getExchangeRate(String tokenSymbol)
    {
        if (tokenSymbol == null || tokenSymbol.isEmpty())
        {
            log.warn("代币符号为空");
            return null;
        }

        try
        {
            VirtualTokenExchangeRate rate = exchangeRateMapper.selectByTokenSymbol(tokenSymbol.toUpperCase());
            if (rate == null)
            {
                log.warn("未找到代币汇率配置: {}", tokenSymbol);
            }
            return rate;
        }
        catch (Exception e)
        {
            log.error("查询代币汇率失败: tokenSymbol={}", tokenSymbol, e);
            return null;
        }
    }

    /**
     * 根据代币金额计算积分数量
     */
    @Override
    public Long calculateCredits(String tokenSymbol, BigDecimal tokenAmount)
    {
        if (tokenAmount == null || tokenAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            log.warn("代币金额无效: {}", tokenAmount);
            return null;
        }

        VirtualTokenExchangeRate rate = getExchangeRate(tokenSymbol);
        if (rate == null)
        {
            log.warn("无法计算积分，未找到汇率配置: {}", tokenSymbol);
            return null;
        }

        // 检查金额范围
        if (rate.getMinAmount() != null && tokenAmount.compareTo(rate.getMinAmount()) < 0)
        {
            log.warn("充值金额低于最小限制: {} < {}", tokenAmount, rate.getMinAmount());
            return null;
        }
        if (rate.getMaxAmount() != null && tokenAmount.compareTo(rate.getMaxAmount()) > 0)
        {
            log.warn("充值金额超过最大限制: {} > {}", tokenAmount, rate.getMaxAmount());
            return null;
        }

        // 使用实体类方法计算积分
        Long credits = rate.calculateCredits(tokenAmount);
        log.info("积分计算: {} {} -> {} 积分 (汇率: {}, USDT比率: {})",
            tokenAmount, tokenSymbol, credits, rate.getEffectiveRate(), rate.getCreditsPerUsdt());

        return credits;
    }

    /**
     * 查询所有启用的汇率配置
     */
    @Override
    public List<VirtualTokenExchangeRate> listEnabledRates()
    {
        try
        {
            return exchangeRateMapper.selectEnabledRates();
        }
        catch (Exception e)
        {
            log.error("查询启用汇率列表失败", e);
            return List.of();
        }
    }

    /**
     * 查询所有汇率配置
     */
    @Override
    public List<VirtualTokenExchangeRate> listAllRates()
    {
        try
        {
            return exchangeRateMapper.selectAllRates();
        }
        catch (Exception e)
        {
            log.error("查询汇率列表失败", e);
            return List.of();
        }
    }

    /**
     * 更新代币汇率
     */
    @Override
    @Transactional
    public boolean updateExchangeRate(String tokenSymbol, BigDecimal newRate, String source)
    {
        if (tokenSymbol == null || newRate == null || newRate.compareTo(BigDecimal.ZERO) <= 0)
        {
            log.warn("汇率更新参数无效: tokenSymbol={}, rate={}", tokenSymbol, newRate);
            return false;
        }

        try
        {
            int rows = exchangeRateMapper.updateRate(tokenSymbol.toUpperCase(), newRate, source);
            if (rows > 0)
            {
                log.info("汇率更新成功: {} = {} USDT (来源: {})", tokenSymbol, newRate, source);
                return true;
            }
            else
            {
                log.warn("汇率更新失败，未找到配置: {}", tokenSymbol);
                return false;
            }
        }
        catch (Exception e)
        {
            log.error("更新汇率失败: tokenSymbol={}, rate={}", tokenSymbol, newRate, e);
            return false;
        }
    }

    /**
     * 新增汇率配置
     */
    @Override
    @Transactional
    public boolean insertExchangeRate(VirtualTokenExchangeRate rate)
    {
        if (rate == null || rate.getTokenSymbol() == null)
        {
            log.warn("汇率配置对象无效");
            return false;
        }

        try
        {
            // 设置默认值
            if (rate.getBaseCurrency() == null)
            {
                rate.setBaseCurrency("USDT");
            }
            if (rate.getCreditsPerUsdt() == null)
            {
                rate.setCreditsPerUsdt(100L);  // 默认 1 USDT = 100 积分
            }
            if (rate.getStatus() == null)
            {
                rate.setStatus(1);  // 默认启用
            }
            if (rate.getManualOverride() == null)
            {
                rate.setManualOverride(false);
            }

            rate.setCreateTime(new Date());
            rate.setUpdateTime(new Date());

            int rows = exchangeRateMapper.insert(rate);
            if (rows > 0)
            {
                log.info("汇率配置新增成功: {} = {} USDT", rate.getTokenSymbol(), rate.getCurrentRate());
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            log.error("新增汇率配置失败: {}", rate.getTokenSymbol(), e);
            return false;
        }
    }

    /**
     * 修改汇率配置
     */
    @Override
    @Transactional
    public boolean updateExchangeRateConfig(VirtualTokenExchangeRate rate)
    {
        if (rate == null || rate.getRateId() == null)
        {
            log.warn("汇率配置对象无效，必须包含 rateId");
            return false;
        }

        try
        {
            rate.setUpdateTime(new Date());

            int rows = exchangeRateMapper.update(rate);
            if (rows > 0)
            {
                log.info("汇率配置修改成功: rateId={}, tokenSymbol={}", rate.getRateId(), rate.getTokenSymbol());
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            log.error("修改汇率配置失败: rateId={}", rate.getRateId(), e);
            return false;
        }
    }

    /**
     * 删除汇率配置
     */
    @Override
    @Transactional
    public boolean deleteExchangeRate(Long rateId)
    {
        if (rateId == null)
        {
            log.warn("汇率ID为空");
            return false;
        }

        try
        {
            int rows = exchangeRateMapper.deleteById(rateId);
            if (rows > 0)
            {
                log.info("汇率配置删除成功: rateId={}", rateId);
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            log.error("删除汇率配置失败: rateId={}", rateId, e);
            return false;
        }
    }

    /**
     * 从外部 API 同步汇率
     *
     * 集成 CoinGecko API 获取实时价格
     */
    @Override
    @Transactional
    public boolean syncExchangeRateFromApi(String tokenSymbol)
    {
        if (tokenSymbol == null || tokenSymbol.isEmpty())
        {
            log.warn("代币符号为空");
            return false;
        }

        try
        {
            // 1. 从 CoinGecko API 获取最新价格
            BigDecimal newRate = coinGeckoService.getPrice(tokenSymbol);

            if (newRate == null)
            {
                log.warn("❌ 从 CoinGecko 获取价格失败: {}", tokenSymbol);
                return false;
            }

            // 2. 更新数据库汇率
            boolean success = updateExchangeRate(tokenSymbol, newRate, "coingecko");

            if (success)
            {
                log.info("✅ 汇率同步成功: {} = {} USDT (来源: CoinGecko)", tokenSymbol, newRate);
            }
            else
            {
                log.warn("❌ 汇率更新失败: {}", tokenSymbol);
            }

            return success;
        }
        catch (Exception e)
        {
            log.error("从 API 同步汇率失败: tokenSymbol={}", tokenSymbol, e);
            return false;
        }
    }

    /**
     * 批量同步所有代币汇率
     *
     * 遍历所有启用的代币，从 CoinGecko API 获取最新价格并更新数据库
     */
    @Override
    @Transactional
    public int syncAllExchangeRates()
    {
        List<VirtualTokenExchangeRate> rates = listEnabledRates();

        if (rates.isEmpty())
        {
            log.warn("没有启用的汇率配置");
            return 0;
        }

        int successCount = 0;

        for (VirtualTokenExchangeRate rate : rates)
        {
            // 跳过手动汇率
            if (Boolean.TRUE.equals(rate.getManualOverride()))
            {
                log.debug("跳过手动汇率: {}", rate.getTokenSymbol());
                continue;
            }

            // 跳过稳定币（汇率固定为 1）
            if (isStablecoin(rate.getTokenSymbol()))
            {
                log.debug("跳过稳定币: {}", rate.getTokenSymbol());
                continue;
            }

            try
            {
                if (syncExchangeRateFromApi(rate.getTokenSymbol()))
                {
                    successCount++;
                }

                // 避免 API 限流，每次请求间隔 200ms
                Thread.sleep(200);
            }
            catch (Exception e)
            {
                log.error("同步汇率异常: tokenSymbol={}", rate.getTokenSymbol(), e);
            }
        }

        log.info("批量汇率同步完成: {} / {} 成功", successCount, rates.size());

        return successCount;
    }

    /**
     * 判断是否为稳定币
     */
    private boolean isStablecoin(String tokenSymbol)
    {
        if (tokenSymbol == null) return false;
        String symbol = tokenSymbol.toUpperCase();
        return symbol.equals("USDT") || symbol.equals("USDC")
            || symbol.equals("BUSD") || symbol.equals("DAI");
    }
}
