-- ---------------------------------------------------
-- 代币汇率配置表
-- 用于存储各代币的实时汇率和积分兑换规则
-- ---------------------------------------------------

DROP TABLE IF EXISTS virtual_token_exchange_rate;
CREATE TABLE virtual_token_exchange_rate (
  rate_id           BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '汇率ID',
  token_symbol      VARCHAR(20)     NOT NULL                   COMMENT '代币符号（BNB/ETH/USDT等）',
  token_name        VARCHAR(100)    DEFAULT NULL               COMMENT '代币名称',
  base_currency     VARCHAR(20)     NOT NULL DEFAULT 'USDT'    COMMENT '基准货币（通常是USDT）',

  -- 汇率相关
  current_rate      DECIMAL(20,8)   NOT NULL                   COMMENT '当前汇率（1代币=多少USDT）',
  rate_source       VARCHAR(50)     DEFAULT NULL               COMMENT '汇率来源（coinmarketcap/coingecko/binance）',
  last_update_time  DATETIME        DEFAULT NULL               COMMENT '汇率最后更新时间',

  -- 积分兑换规则
  credits_per_usdt  BIGINT          NOT NULL DEFAULT 100       COMMENT '每1 USDT 兑换多少积分',
  min_amount        DECIMAL(20,8)   DEFAULT 0                  COMMENT '最小充值金额',
  max_amount        DECIMAL(20,8)   DEFAULT NULL               COMMENT '最大充值金额',

  -- 手动覆盖（可选）
  manual_override   TINYINT(1)      DEFAULT 0                  COMMENT '是否手动覆盖汇率',
  manual_rate       DECIMAL(20,8)   DEFAULT NULL               COMMENT '手动设置的固定汇率',

  -- 状态
  status            TINYINT(1)      DEFAULT 1                  COMMENT '状态（0=禁用 1=启用）',
  priority          INT             DEFAULT 0                  COMMENT '优先级（数字越大优先级越高）',

  -- 时间戳
  create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by         VARCHAR(64)     DEFAULT NULL,
  update_by         VARCHAR(64)     DEFAULT NULL,
  remark            VARCHAR(500)    DEFAULT NULL               COMMENT '备注',

  PRIMARY KEY (rate_id),
  UNIQUE KEY uk_token_symbol (token_symbol),
  KEY idx_status (status),
  KEY idx_last_update (last_update_time)
) ENGINE=InnoDB COMMENT='代币汇率配置表';

-- 插入默认数据
INSERT INTO virtual_token_exchange_rate
(token_symbol, token_name, current_rate, credits_per_usdt, rate_source, last_update_time, remark)
VALUES
('USDT', 'Tether USD', 1.0, 100, 'fixed', NOW(), 'USDT作为基准货币，固定1:1'),
('USDC', 'USD Coin', 1.0, 100, 'fixed', NOW(), 'USDC稳定币，固定1:1'),
('BUSD', 'Binance USD', 1.0, 100, 'fixed', NOW(), 'BUSD稳定币，固定1:1'),
('BNB', 'Binance Coin', 600.0, 100, 'manual', NOW(), 'BNB价格，需要定期更新'),
('ETH', 'Ethereum', 3200.0, 100, 'manual', NOW(), 'ETH价格，需要定期更新'),
('BTC', 'Bitcoin', 50000.0, 100, 'manual', NOW(), 'BTC价格，需要定期更新');

-- ---------------------------------------------------
-- 汇率更新日志表（可选，用于追踪汇率变化历史）
-- ---------------------------------------------------

DROP TABLE IF EXISTS virtual_token_rate_history;
CREATE TABLE virtual_token_rate_history (
  history_id        BIGINT          NOT NULL AUTO_INCREMENT    COMMENT '历史记录ID',
  token_symbol      VARCHAR(20)     NOT NULL                   COMMENT '代币符号',
  old_rate          DECIMAL(20,8)   DEFAULT NULL               COMMENT '旧汇率',
  new_rate          DECIMAL(20,8)   NOT NULL                   COMMENT '新汇率',
  rate_source       VARCHAR(50)     DEFAULT NULL               COMMENT '汇率来源',
  update_type       VARCHAR(20)     NOT NULL                   COMMENT '更新类型（auto/manual）',
  create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_by         VARCHAR(64)     DEFAULT NULL,
  remark            VARCHAR(500)    DEFAULT NULL,

  PRIMARY KEY (history_id),
  KEY idx_token (token_symbol),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='代币汇率变更历史表';
