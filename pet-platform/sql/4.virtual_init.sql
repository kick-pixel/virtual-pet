-- =====================================================
-- Pet Virtual 平台数据库初始化脚本
-- 版本: v2.3
-- 说明: 包含所有新增表、现有表扩展、初始化数据
-- =====================================================

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------
-- 1. 平台用户表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_user;

CREATE TABLE virtual_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名（唯一标识）',
    email VARCHAR(200) DEFAULT NULL COMMENT '邮箱（OAuth/钱包首次登录可为空，后续强制绑定）',
    email_verified TINYINT(1) DEFAULT 0 COMMENT '邮箱是否验证',
    password_hash VARCHAR(255) DEFAULT NULL COMMENT '密码哈希（邮箱注册）',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    locale VARCHAR(10) DEFAULT 'en-US' COMMENT '语言偏好',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',
    status TINYINT(1) DEFAULT 1 COMMENT '状态（0禁用 1正常）',
    login_attempts INT DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until DATETIME DEFAULT NULL COMMENT '锁定截止时间',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    last_login_device VARCHAR(200) DEFAULT NULL COMMENT '最后登录设备',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '虚拟宠物平台用户表';

-- ---------------------------------------------------
-- 2. 用户 OAuth 绑定表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_user_oauth;

CREATE TABLE virtual_user_oauth (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL COMMENT '提供商（google/microsoft/twitter/telegram）',
    provider_user_id VARCHAR(200) NOT NULL COMMENT '提供商用户ID',
    provider_email VARCHAR(200) DEFAULT NULL COMMENT '提供商邮箱',
    provider_name VARCHAR(200) DEFAULT NULL COMMENT '提供商用户名',
    provider_avatar VARCHAR(500) DEFAULT NULL COMMENT '提供商头像',
    access_token TEXT DEFAULT NULL COMMENT 'Access Token（加密存储）',
    refresh_token TEXT DEFAULT NULL COMMENT 'Refresh Token（加密存储）',
    token_expires_at DATETIME DEFAULT NULL COMMENT 'Token 过期时间',
    raw_data JSON DEFAULT NULL COMMENT '原始返回数据',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_user (provider, provider_user_id),
    KEY idx_user (user_id),
    KEY idx_provider (provider)
) ENGINE = InnoDB COMMENT = '用户OAuth绑定表';

-- ---------------------------------------------------
-- 3. 用户钱包绑定表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_user_wallet;

CREATE TABLE virtual_user_wallet (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    wallet_address VARCHAR(100) NOT NULL COMMENT '钱包地址',
    wallet_type VARCHAR(50) DEFAULT 'evm' COMMENT '钱包类型（evm/solana等）',
    chain_id INT DEFAULT NULL COMMENT '绑定时的链ID',
    is_primary TINYINT(1) DEFAULT 0 COMMENT '是否主钱包',
    signature VARCHAR(500) DEFAULT NULL COMMENT '签名验证记录',
    verified_at DATETIME DEFAULT NULL COMMENT '验证时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wallet (wallet_address),
    KEY idx_user (user_id),
    KEY idx_wallet_type (wallet_type)
) ENGINE = InnoDB COMMENT = '用户钱包绑定表';

-- ---------------------------------------------------
-- 4. 用户积分表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_user_credits;

CREATE TABLE virtual_user_credits (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    balance BIGINT NOT NULL DEFAULT 0 COMMENT '当前可用积分',
    frozen BIGINT NOT NULL DEFAULT 0 COMMENT '冻结积分（任务进行中）',
    total_earned BIGINT NOT NULL DEFAULT 0 COMMENT '累计获得积分',
    total_spent BIGINT NOT NULL DEFAULT 0 COMMENT '累计消费积分',
    total_refunded BIGINT NOT NULL DEFAULT 0 COMMENT '累计退还积分',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE = InnoDB COMMENT = '用户积分表';

-- ---------------------------------------------------
-- 5. 积分交易记录表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_credits_transaction;

CREATE TABLE virtual_credits_transaction (
    tx_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tx_type VARCHAR(20) NOT NULL COMMENT '交易类型（recharge/consume/refund/reward/freeze/unfreeze/expire/adjust）',
    amount BIGINT NOT NULL COMMENT '积分数量（正数）',
    direction TINYINT(1) NOT NULL COMMENT '方向（1增加 -1减少）',
    balance_before BIGINT NOT NULL COMMENT '交易前余额',
    balance_after BIGINT NOT NULL COMMENT '交易后余额',
    business_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型（video_task/recharge/share_reward等）',
    business_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_tx_id BIGINT DEFAULT NULL COMMENT '关联交易ID（如退款关联原消费）',
    description VARCHAR(500) DEFAULT NULL COMMENT '交易描述',
    operator VARCHAR(100) DEFAULT 'system' COMMENT '操作者',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tx_id),
    KEY idx_user (user_id),
    KEY idx_type (tx_type),
    KEY idx_business (business_type, business_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '积分交易流水表';

-- ---------------------------------------------------
-- 6. 充值套餐表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_recharge_package;

CREATE TABLE virtual_recharge_package (
    package_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    credits_amount BIGINT NOT NULL COMMENT '积分数量',
    bonus_credits BIGINT DEFAULT 0 COMMENT '赠送积分',
    price_usdt DECIMAL(20, 8) NOT NULL COMMENT 'USDT价格',
    price_eth DECIMAL(20, 8) DEFAULT NULL COMMENT 'ETH价格（实时汇率计算）',
    price_bnb DECIMAL(20, 8) DEFAULT NULL COMMENT 'BNB价格（实时汇率计算）',
    badge VARCHAR(50) DEFAULT NULL COMMENT '标签（热门/推荐/限时）',
    description VARCHAR(500) DEFAULT NULL COMMENT '套餐描述',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '图标URL',
    min_purchase INT DEFAULT 1 COMMENT '最少购买次数',
    max_purchase INT DEFAULT NULL COMMENT '最多购买次数（NULL=不限）',
    valid_start DATETIME DEFAULT NULL COMMENT '有效期开始',
    valid_end DATETIME DEFAULT NULL COMMENT '有效期结束',
    status TINYINT(1) DEFAULT 1 COMMENT '状态（0下架 1上架）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (package_id),
    KEY idx_status (status, sort_order)
) ENGINE = InnoDB COMMENT = '充值套餐表';

-- ---------------------------------------------------
-- 7. 充值订单表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_recharge_order;

CREATE TABLE virtual_recharge_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT DEFAULT NULL COMMENT '套餐ID（可为空表示自定义金额）',
    credits_amount BIGINT NOT NULL COMMENT '购买积分数',
    bonus_credits BIGINT DEFAULT 0 COMMENT '赠送积分数',
    pay_network VARCHAR(50) NOT NULL COMMENT '支付网络',
    pay_token VARCHAR(20) NOT NULL COMMENT '支付代币',
    pay_amount DECIMAL(28, 18) NOT NULL COMMENT '支付金额',
    pay_amount_display VARCHAR(50) NOT NULL COMMENT '显示金额',
    wallet_address VARCHAR(100) NOT NULL COMMENT '收款钱包地址',
    from_address VARCHAR(100) DEFAULT NULL COMMENT '用户钱包地址',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态（pending/paid/completed/cancelled/expired/failed）',
    tx_hash VARCHAR(100) DEFAULT NULL COMMENT '交易哈希',
    payment_tx_id BIGINT DEFAULT NULL COMMENT '关联支付交易表ID',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    expire_at DATETIME NOT NULL COMMENT '订单过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_status (status),
    KEY idx_tx_hash (tx_hash),
    KEY idx_wallet (wallet_address),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '充值订单表';

-- ---------------------------------------------------
-- 8. 积分消费规则表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_credits_rule;

CREATE TABLE virtual_credits_rule (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则代码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL COMMENT '规则类型（consume/reward）',
    base_credits BIGINT DEFAULT 0 COMMENT '基础积分消耗',
    per_second BIGINT DEFAULT 0 COMMENT '每秒视频消耗积分',
    resolution_rate JSON DEFAULT NULL COMMENT '分辨率系数',
    reward_credits BIGINT DEFAULT 0 COMMENT '奖励积分数',
    reward_condition VARCHAR(500) DEFAULT NULL COMMENT '奖励条件描述',
    description VARCHAR(500) DEFAULT NULL COMMENT '规则说明',
    status TINYINT(1) DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_code (rule_code)
) ENGINE = InnoDB COMMENT = '积分规则表';

-- ---------------------------------------------------
-- 9. 生成选项配置表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_generation_option;

CREATE TABLE virtual_generation_option (
    option_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '选项ID',
    option_group VARCHAR(50) NOT NULL COMMENT '选项分组（duration/resolution/style等）',
    option_code VARCHAR(50) NOT NULL COMMENT '选项代码',
    option_name VARCHAR(100) NOT NULL COMMENT '选项名称',
    option_value VARCHAR(200) NOT NULL COMMENT '选项值',
    credits_modifier DECIMAL(10, 2) DEFAULT 1.0 COMMENT '积分系数',
    extra_config JSON DEFAULT NULL COMMENT '额外配置',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认',
    is_premium TINYINT(1) DEFAULT 0 COMMENT '是否高级选项',
    status TINYINT(1) DEFAULT 1 COMMENT '状态',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (option_id),
    UNIQUE KEY uk_group_code (option_group, option_code),
    KEY idx_group (option_group)
) ENGINE = InnoDB COMMENT = '生成选项配置表';

-- ---------------------------------------------------
-- 10. 宠物示例展示表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_pet_showcase;

CREATE TABLE virtual_pet_showcase (
    showcase_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '展示ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT DEFAULT NULL COMMENT '描述',
    media_type VARCHAR(20) NOT NULL COMMENT '媒体类型（image/video）',
    media_url VARCHAR(500) NOT NULL COMMENT '媒体URL',
    thumbnail_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    tags JSON DEFAULT NULL COMMENT '标签数组',
    pet_type VARCHAR(50) DEFAULT NULL COMMENT '宠物类型（cat/dog/bird等）',
    view_count BIGINT DEFAULT 0 COMMENT '浏览次数',
    like_count BIGINT DEFAULT 0 COMMENT '点赞次数',
    share_count BIGINT DEFAULT 0 COMMENT '分享次数',
    favorite_count BIGINT DEFAULT 0 COMMENT '收藏次数',
    source_task_id BIGINT DEFAULT NULL COMMENT '来源任务ID',
    source_user_id BIGINT DEFAULT NULL COMMENT '来源用户ID',
    is_featured TINYINT(1) DEFAULT 0 COMMENT '是否精选',
    is_official TINYINT(1) DEFAULT 1 COMMENT '是否官方（0用户 1官方）',
    status TINYINT(1) DEFAULT 1 COMMENT '状态',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (showcase_id),
    KEY idx_category (category),
    KEY idx_pet_type (pet_type),
    KEY idx_featured (is_featured, status),
    KEY idx_sort (sort_order)
) ENGINE = InnoDB COMMENT = '宠物示例展示表';

-- ---------------------------------------------------
-- 11. 用户收藏表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_user_favorite;

CREATE TABLE virtual_user_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(50) NOT NULL COMMENT '收藏类型（showcase/task）',
    target_id BIGINT NOT NULL COMMENT '收藏目标ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_target (
        user_id,
        target_type,
        target_id
    ),
    KEY idx_target (target_type, target_id)
) ENGINE = InnoDB COMMENT = '用户收藏表';

-- ---------------------------------------------------
-- 12. 分享记录表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_share_record;

CREATE TABLE virtual_share_record (
    share_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    platform VARCHAR(50) NOT NULL COMMENT '分享平台（tiktok/twitter/telegram/facebook）',
    share_url VARCHAR(500) DEFAULT NULL COMMENT '分享链接',
    share_content TEXT DEFAULT NULL COMMENT '分享文案',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '分享状态（pending/success/failed）',
    reward_credits BIGINT DEFAULT 0 COMMENT '奖励积分',
    reward_tx_id BIGINT DEFAULT NULL COMMENT '奖励积分交易ID',
    error_message VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    shared_at DATETIME DEFAULT NULL COMMENT '分享成功时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (share_id),
    KEY idx_user (user_id),
    KEY idx_task (task_id),
    KEY idx_platform (platform),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT = '视频分享记录表';

-- ---------------------------------------------------
-- 13. 用户通知表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_notification;

CREATE TABLE virtual_notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型（task_completed/task_failed/recharge_success/credits_low/share_reward/system_announcement）',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT DEFAULT NULL COMMENT '通知内容',
    extra_data JSON DEFAULT NULL COMMENT '扩展数据',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    read_at DATETIME DEFAULT NULL COMMENT '阅读时间',
    channel VARCHAR(50) DEFAULT 'in_app' COMMENT '通知渠道（in_app/email/telegram）',
    channel_sent TINYINT(1) DEFAULT 0 COMMENT '是否已发送到渠道',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_type (type),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '用户通知表';

-- ---------------------------------------------------
-- 14. Telegram 用户绑定表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_telegram_binding;

CREATE TABLE virtual_telegram_binding (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID（绑定后填入）',
    telegram_user_id BIGINT NOT NULL COMMENT 'Telegram用户ID',
    telegram_username VARCHAR(200) DEFAULT NULL COMMENT 'Telegram用户名',
    telegram_chat_id BIGINT NOT NULL COMMENT 'Telegram聊天ID',
    bind_code VARCHAR(64) DEFAULT NULL COMMENT '绑定验证码',
    bind_code_expire DATETIME DEFAULT NULL COMMENT '验证码过期时间',
    bot_state VARCHAR(50) DEFAULT 'idle' COMMENT 'Bot会话状态',
    bot_state_data JSON DEFAULT NULL COMMENT 'Bot会话临时数据',
    status TINYINT(1) DEFAULT 1 COMMENT '状态（0解绑 1正常）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_telegram_user (telegram_user_id),
    KEY idx_user (user_id),
    KEY idx_chat_id (telegram_chat_id),
    KEY idx_bind_code (bind_code)
) ENGINE = InnoDB COMMENT = 'Telegram用户绑定表';

-- ---------------------------------------------------
-- 15. 邮件发送日志表
-- ---------------------------------------------------
DROP TABLE IF EXISTS virtual_email_log;

CREATE TABLE virtual_email_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    recipient_email VARCHAR(200) NOT NULL COMMENT '收件人邮箱',
    template_type VARCHAR(50) NOT NULL COMMENT '邮件模板类型',
    subject VARCHAR(200) NOT NULL COMMENT '邮件主题',
    template_vars JSON DEFAULT NULL COMMENT '模板变量（JSON）',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '发送状态（pending/sent/failed）',
    error_message TEXT DEFAULT NULL COMMENT '失败原因',
    sent_at DATETIME DEFAULT NULL COMMENT '发送时间',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retries INT DEFAULT 3 COMMENT '最大重试次数',
    provider VARCHAR(50) DEFAULT 'smtp' COMMENT '发送方式（smtp/sendgrid/ses）',
    message_id VARCHAR(200) DEFAULT NULL COMMENT '邮件消息ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    KEY idx_user (user_id),
    KEY idx_email (recipient_email),
    KEY idx_status (status),
    KEY idx_template (template_type),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB COMMENT = '邮件发送日志表';

-- ---------------------------------------------------
-- 16. 扩展现有表：ai_video_task
-- ---------------------------------------------------
ALTER TABLE ai_video_task
ADD COLUMN virtual_user_id BIGINT DEFAULT NULL COMMENT '虚拟宠物平台用户ID' AFTER user_id,
ADD COLUMN credits_cost BIGINT DEFAULT NULL COMMENT '消耗积分数' AFTER cost_amount,
ADD COLUMN credits_frozen BIGINT DEFAULT NULL COMMENT '冻结积分数' AFTER credits_cost,
ADD COLUMN credits_tx_id BIGINT DEFAULT NULL COMMENT '积分交易ID' AFTER credits_frozen,
ADD COLUMN generation_options JSON DEFAULT NULL COMMENT '生成选项配置' AFTER remark,
ADD KEY idx_virtual_user (virtual_user_id);

-- ---------------------------------------------------
-- 17. 扩展现有表：pet_payment_transaction
-- ---------------------------------------------------
ALTER TABLE pet_payment_transaction
ADD COLUMN virtual_user_id BIGINT DEFAULT NULL COMMENT '虚拟宠物平台用户ID' AFTER user_id,
ADD COLUMN recharge_order_id BIGINT DEFAULT NULL COMMENT '关联充值订单ID' AFTER business_id,
ADD KEY idx_virtual_user (virtual_user_id),
ADD KEY idx_recharge_order (recharge_order_id);

-- =====================================================
-- 初始化数据
-- =====================================================

-- 积分规则
INSERT INTO
    virtual_credits_rule (
        rule_code,
        rule_name,
        rule_type,
        base_credits,
        per_second,
        resolution_rate,
        description
    )
VALUES (
        'video_generation',
        'Video Generation',
        'consume',
        10,
        10,
        '{"480p":0.5,"720p":0.8,"1080p":1.0,"4k":2.0}',
        'Base 100 credits + 10 credits/sec × resolution multiplier'
    );


UPDATE virtual_credits_rule
SET
    reward_credits = 50
WHERE
    rule_code = 'register_reward';


INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(1, 'duration', '5s', '5 Seconds', '5', 0.50, NULL, 0, 0, 1, 1, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(2, 'duration', '10s', '10 Seconds', '10', 1.00, NULL, 1, 0, 1, 2, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(3, 'duration', '15s', '15 Seconds', '15', 1.50, NULL, 0, 0, 1, 3, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(5, 'resolution', '480p', '480P', '480p', 0.50, NULL, 0, 0, 1, 1, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(6, 'resolution', '720p', '720P', '720p', 0.80, NULL, 0, 0, 1, 2, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(9, 'aspectRatio', '16:9', '16:9', '16:9', 1.00, NULL, 1, 0, 1, 1, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(10, 'aspectRatio', '9:16', '9:16', '9:16', 1.00, NULL, 0, 0, 1, 2, '2026-02-12 11:46:39');
INSERT INTO virtual_generation_option (option_id, option_group, option_code, option_name, option_value, credits_modifier, extra_config, is_default, is_premium, status, sort_order, create_time) VALUES(11, 'aspectRatio', '1:1', '1:1', '1:1', 1.00, NULL, 0, 0, 1, 3, '2026-02-12 11:46:39');

-- 充值套餐
INSERT INTO app_db.virtual_recharge_package (package_id, package_name, credits_amount, bonus_credits, price_usdt, price_eth, price_bnb, badge, description, icon_url, min_purchase, max_purchase, valid_start, valid_end, status, sort_order, create_time, update_time) VALUES(1, 'Starter', 300, 0, 5.00000000, NULL, NULL, NULL, 'Perfect for trying out', NULL, 1, NULL, NULL, NULL, 1, 1, '2026-02-12 11:46:39', '2026-02-28 20:57:59');
INSERT INTO app_db.virtual_recharge_package (package_id, package_name, credits_amount, bonus_credits, price_usdt, price_eth, price_bnb, badge, description, icon_url, min_purchase, max_purchase, valid_start, valid_end, status, sort_order, create_time, update_time) VALUES(2, 'Popular', 1200, 0, 20.00000000, NULL, NULL, 'Hot', 'Most popular package', NULL, 1, NULL, NULL, NULL, 1, 2, '2026-02-12 11:46:39', '2026-02-28 20:58:00');
INSERT INTO app_db.virtual_recharge_package (package_id, package_name, credits_amount, bonus_credits, price_usdt, price_eth, price_bnb, badge, description, icon_url, min_purchase, max_purchase, valid_start, valid_end, status, sort_order, create_time, update_time) VALUES(3, 'Premium', 3000, 0, 50.00000000, NULL, NULL, 'Best Value', 'Best value for your money', NULL, 1, NULL, NULL, NULL, 1, 3, '2026-02-12 11:46:39', '2026-02-28 20:58:01');
SET FOREIGN_KEY_CHECKS = 1;