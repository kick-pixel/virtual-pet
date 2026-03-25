package com.ruoyi.system.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * CoinGecko API 服务接口
 *
 * CoinGecko 是全球最大的加密货币数据聚合平台
 * 免费 API 额度：10-50 calls/min，无需 API Key
 *
 * API 文档：https://www.coingecko.com/en/api/documentation
 *
 * @author ruoyi
 */
public interface ICoinGeckoService
{
    /**
     * 获取单个代币的 USDT 价格
     *
     * @param tokenSymbol 代币符号（如 BTC, ETH, BNB）
     * @return USDT 价格，失败返回 null
     */
    BigDecimal getPrice(String tokenSymbol);

    /**
     * 批量获取多个代币的 USDT 价格
     *
     * @param tokenSymbols 代币符号数组（如 ["BTC", "ETH", "BNB"]）
     * @return Map<代币符号, USDT价格>
     */
    Map<String, BigDecimal> getPrices(String... tokenSymbols);

    /**
     * 获取代币详细信息（包括价格、市值、24h变化等）
     *
     * @param tokenSymbol 代币符号
     * @return 详细信息 Map
     */
    Map<String, Object> getTokenInfo(String tokenSymbol);
}
