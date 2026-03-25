package com.ruoyi.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.service.ICoinGeckoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CoinGecko API 服务实现
 *
 * @author ruoyi
 */
@Service
public class CoinGeckoServiceImpl implements ICoinGeckoService
{
    private static final Logger log = LoggerFactory.getLogger(CoinGeckoServiceImpl.class);

    private static final String API_BASE_URL = "https://api.coingecko.com/api/v3";
    private static final String PRICE_ENDPOINT = "/simple/price";

    /**
     * 代币符号到 CoinGecko ID 的映射
     *
     * CoinGecko 使用完整的 ID（如 "bitcoin"），而不是符号（如 "BTC"）
     * 完整列表：https://api.coingecko.com/api/v3/coins/list
     */
    private static final Map<String, String> SYMBOL_TO_ID_MAP = Map.ofEntries(
        Map.entry("BTC", "bitcoin"),
        Map.entry("ETH", "ethereum"),
        Map.entry("BNB", "binancecoin"),
        Map.entry("USDT", "tether"),
        Map.entry("USDC", "usd-coin"),
        Map.entry("BUSD", "binance-usd"),
        Map.entry("XRP", "ripple"),
        Map.entry("ADA", "cardano"),
        Map.entry("SOL", "solana"),
        Map.entry("DOT", "polkadot"),
        Map.entry("MATIC", "matic-network"),
        Map.entry("AVAX", "avalanche-2"),
        Map.entry("LINK", "chainlink"),
        Map.entry("UNI", "uniswap"),
        Map.entry("ATOM", "cosmos"),
        Map.entry("LTC", "litecoin"),
        Map.entry("TRX", "tron"),
        Map.entry("DAI", "dai"),
        Map.entry("SHIB", "shiba-inu"),
        Map.entry("DOGE", "dogecoin")
    );

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取单个代币的 USDT 价格
     */
    @Override
    public BigDecimal getPrice(String tokenSymbol)
    {
        if (tokenSymbol == null || tokenSymbol.isEmpty())
        {
            log.warn("代币符号为空");
            return null;
        }

        try
        {
            String coinId = getCoinGeckoId(tokenSymbol);
            if (coinId == null)
            {
                log.warn("未找到代币映射: {}", tokenSymbol);
                return null;
            }

            // 构建 API URL
            String url = String.format("%s%s?ids=%s&vs_currencies=usd",
                API_BASE_URL, PRICE_ENDPOINT, coinId);

            log.debug("调用 CoinGecko API: {}", url);

            // 发送 HTTP 请求
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty())
            {
                log.warn("CoinGecko API 返回空响应");
                return null;
            }

            // 解析 JSON 响应
            // 响应格式：{"bitcoin":{"usd":50000.0}}
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode coinNode = rootNode.get(coinId);

            if (coinNode == null || !coinNode.has("usd"))
            {
                log.warn("CoinGecko API 返回格式异常: {}", response);
                return null;
            }

            BigDecimal price = new BigDecimal(coinNode.get("usd").asText());
            log.info("✅ CoinGecko 价格获取成功: {} = ${}", tokenSymbol, price);

            return price;
        }
        catch (Exception e)
        {
            log.error("CoinGecko API 调用失败: tokenSymbol={}", tokenSymbol, e);
            return null;
        }
    }

    /**
     * 批量获取多个代币的 USDT 价格
     */
    @Override
    public Map<String, BigDecimal> getPrices(String... tokenSymbols)
    {
        Map<String, BigDecimal> result = new HashMap<>();

        if (tokenSymbols == null || tokenSymbols.length == 0)
        {
            return result;
        }

        try
        {
            // 构建 CoinGecko ID 列表
            String coinIds = java.util.Arrays.stream(tokenSymbols)
                .map(this::getCoinGeckoId)
                .filter(id -> id != null)
                .collect(Collectors.joining(","));

            if (coinIds.isEmpty())
            {
                log.warn("没有有效的代币映射");
                return result;
            }

            // 构建 API URL
            String url = String.format("%s%s?ids=%s&vs_currencies=usd",
                API_BASE_URL, PRICE_ENDPOINT, coinIds);

            log.debug("调用 CoinGecko API (批量): {}", url);

            // 发送 HTTP 请求
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty())
            {
                log.warn("CoinGecko API 返回空响应");
                return result;
            }

            // 解析 JSON 响应
            JsonNode rootNode = objectMapper.readTree(response);

            // 遍历所有代币
            for (String tokenSymbol : tokenSymbols)
            {
                String coinId = getCoinGeckoId(tokenSymbol);
                if (coinId == null) continue;

                JsonNode coinNode = rootNode.get(coinId);
                if (coinNode != null && coinNode.has("usd"))
                {
                    BigDecimal price = new BigDecimal(coinNode.get("usd").asText());
                    result.put(tokenSymbol, price);
                    log.info("✅ {} = ${}", tokenSymbol, price);
                }
            }

            log.info("批量价格获取完成: {} / {} 成功", result.size(), tokenSymbols.length);

            return result;
        }
        catch (Exception e)
        {
            log.error("CoinGecko API 批量调用失败", e);
            return result;
        }
    }

    /**
     * 获取代币详细信息
     */
    @Override
    public Map<String, Object> getTokenInfo(String tokenSymbol)
    {
        Map<String, Object> result = new HashMap<>();

        try
        {
            String coinId = getCoinGeckoId(tokenSymbol);
            if (coinId == null)
            {
                return result;
            }

            // 构建 API URL
            String url = String.format("%s/coins/%s?localization=false&tickers=false&community_data=false&developer_data=false",
                API_BASE_URL, coinId);

            log.debug("调用 CoinGecko API (详情): {}", url);

            // 发送 HTTP 请求
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty())
            {
                return result;
            }

            // 解析 JSON 响应
            JsonNode rootNode = objectMapper.readTree(response);

            // 提取关键信息
            if (rootNode.has("market_data"))
            {
                JsonNode marketData = rootNode.get("market_data");
                JsonNode currentPrice = marketData.get("current_price");

                if (currentPrice != null && currentPrice.has("usd"))
                {
                    result.put("price", new BigDecimal(currentPrice.get("usd").asText()));
                }

                // 24小时价格变化
                if (marketData.has("price_change_percentage_24h"))
                {
                    result.put("change_24h", marketData.get("price_change_percentage_24h").asDouble());
                }

                // 市值
                if (marketData.has("market_cap"))
                {
                    JsonNode marketCap = marketData.get("market_cap");
                    if (marketCap.has("usd"))
                    {
                        result.put("market_cap", marketCap.get("usd").asLong());
                    }
                }
            }

            return result;
        }
        catch (Exception e)
        {
            log.error("CoinGecko API 详情调用失败: tokenSymbol={}", tokenSymbol, e);
            return result;
        }
    }

    /**
     * 根据代币符号获取 CoinGecko ID
     */
    private String getCoinGeckoId(String tokenSymbol)
    {
        if (tokenSymbol == null)
        {
            return null;
        }
        return SYMBOL_TO_ID_MAP.get(tokenSymbol.toUpperCase());
    }
}
