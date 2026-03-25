-- =============================================================
-- 10. Check-in Payment Feature Update 2026-03-07
-- 1. Add order_type field to virtual_recharge_order
-- 2. Add check-in fee configuration to sys_config
-- =============================================================

-- 1. Add order_type field to virtual_recharge_order
ALTER TABLE virtual_recharge_order
    ADD COLUMN order_type VARCHAR(20) NOT NULL DEFAULT 'recharge'
        COMMENT 'Order type: recharge-credit recharge, checkin-check-in payment'
        AFTER status;

-- 2. Add check-in fee configuration to sys_config (Idempotent: ignore if already exists)
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'Check-in Minimum Payment Amount - BNB', 'checkin.fee.bnb', '0.0000126', 'Y', 'admin', NOW(), 'Minimum BNB amount required for daily check-in, used to generate on-chain data'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'checkin.fee.bnb');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'Check-in Minimum Payment Amount - USDT', 'checkin.fee.usdt', '0.01', 'Y', 'admin', NOW(), 'Minimum USDT amount required for daily check-in, used to generate on-chain data'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'checkin.fee.usdt');
