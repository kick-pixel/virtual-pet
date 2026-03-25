-- 代币汇率初始化数据
-- 包含常见的加密货币汇率配置

-- 清空旧数据（可选）
-- TRUNCATE TABLE virtual_token_exchange_rate;

-- 插入 USDT 汇率（稳定币，1:1）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'USDT', 'Tether USD', 'USDT', 1.00000000, 'manual',
    NOW(), 100, 1.00, 10000.00,
    0, 1, 1, NOW(), NOW()
);

-- 插入 USDC 汇率（稳定币，1:1）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'USDC', 'USD Coin', 'USDT', 1.00000000, 'manual',
    NOW(), 100, 1.00, 10000.00,
    0, 1, 2, NOW(), NOW()
);

-- 插入 BNB 汇率（示例汇率 1 BNB = 600 USDT）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'BNB', 'BNB', 'USDT', 600.00000000, 'manual',
    NOW(), 100, 0.01, 100.00,
    0, 1, 3, NOW(), NOW()
);

-- 插入 ETH 汇率（示例汇率 1 ETH = 3000 USDT）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'ETH', 'Ethereum', 'USDT', 3000.00000000, 'manual',
    NOW(), 100, 0.001, 10.00,
    0, 1, 4, NOW(), NOW()
);

-- 插入 BTC 汇率（示例汇率 1 BTC = 50000 USDT）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'BTC', 'Bitcoin', 'USDT', 50000.00000000, 'manual',
    NOW(), 100, 0.0001, 1.00,
    0, 1, 5, NOW(), NOW()
);

-- 插入 BUSD 汇率（稳定币，1:1）
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, status, priority, create_time, update_time
) VALUES (
    'BUSD', 'Binance USD', 'USDT', 1.00000000, 'manual',
    NOW(), 100, 1.00, 10000.00,
    0, 1, 6, NOW(), NOW()
);

-- 手动汇率示例（管理员可以覆盖自动汇率）
-- 例如：给某个代币设置促销汇率（更多积分）
/*
INSERT INTO virtual_token_exchange_rate (
    token_symbol, token_name, base_currency, current_rate, rate_source,
    last_update_time, credits_per_usdt, min_amount, max_amount,
    manual_override, manual_rate, status, priority, create_time, update_time
) VALUES (
    'BNB', 'BNB (促销)', 'USDT', 600.00000000, 'binance',
    NOW(), 120, 0.01, 100.00,  -- 注意这里 credits_per_usdt 是 120，比默认 100 多 20%
    1, 650.00000000, 1, 10, NOW(), NOW()  -- manual_override=1，手动汇率为 650
);
*/

-- 查询验证
SELECT
    rate_id,
    token_symbol,
    token_name,
    current_rate,
    credits_per_usdt,
    CASE
        WHEN manual_override = 1 THEN manual_rate
        ELSE current_rate
    END as effective_rate,
    status,
    priority
FROM virtual_token_exchange_rate
ORDER BY priority;

-- 计算示例验证
-- 示例：充值 0.1 BNB
-- 0.1 BNB * 600 USDT = 60 USDT
-- 60 USDT * 100 积分/USDT = 6000 积分
SELECT
    token_symbol,
    0.1 as token_amount,
    current_rate,
    (0.1 * current_rate) as usdt_equivalent,
    credits_per_usdt,
    (0.1 * current_rate * credits_per_usdt) as credits
FROM virtual_token_exchange_rate
WHERE token_symbol = 'BNB';
