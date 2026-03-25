-- ============================================================
-- 管理后台功能迭代 v1.0 | 日期：2026-03-01
-- 包含：用户管理字段、视频管理字段、统计中间表、菜单权限
--
-- 注意：MySQL 8.0 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS
--       (该语法为 MariaDB 扩展)。此处改用临时存储过程
--       + INFORMATION_SCHEMA 检查来实现幂等新增列。
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------------------------------------------
-- 工具过程：幂等添加列（脚本末尾会 DROP）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS _add_col;
DROP PROCEDURE IF EXISTS _add_idx;

DELIMITER //
CREATE PROCEDURE _add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN col_def TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = tbl
          AND COLUMN_NAME  = col
    ) THEN
        SET @_sql = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN `', col, '` ', col_def);
        PREPARE _stmt FROM @_sql;
        EXECUTE _stmt;
        DEALLOCATE PREPARE _stmt;
    END IF;
END //

CREATE PROCEDURE _add_idx(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN idx_def TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = tbl
          AND INDEX_NAME   = idx
    ) THEN
        SET @_sql = CONCAT('CREATE INDEX `', idx, '` ON `', tbl, '` ', idx_def);
        PREPARE _stmt FROM @_sql;
        EXECUTE _stmt;
        DEALLOCATE PREPARE _stmt;
    END IF;
END //
DELIMITER ;

-- ----------------------------------------------------------------
-- 1. virtual_user 新增字段
-- ----------------------------------------------------------------
CALL _add_col('virtual_user', 'register_ip',
    "VARCHAR(50) DEFAULT NULL COMMENT '注册IP' AFTER last_login_ip");

CALL _add_col('virtual_user', 'country_region',
    "VARCHAR(100) DEFAULT NULL COMMENT '国家地区（IP 解析或用户填写）' AFTER register_ip");

CALL _add_col('virtual_user', 'telegram_id',
    "VARCHAR(100) DEFAULT NULL COMMENT 'Telegram ID' AFTER country_region");

CALL _add_col('virtual_user', 'gen_disabled',
    "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '禁止生成 0-正常 1-禁止' AFTER status");

CALL _add_col('virtual_user', 'consecutive_days',
    "INT NOT NULL DEFAULT 0 COMMENT '当前连续签到天数（真实天数，不受7天奖励周期限制）' AFTER gen_disabled");

CALL _add_col('virtual_user', 'max_streak_days',
    "INT NOT NULL DEFAULT 0 COMMENT '历史最长连续签到天数' AFTER consecutive_days");

-- ----------------------------------------------------------------
-- 2. ai_video_task 新增字段
-- ----------------------------------------------------------------
CALL _add_col('ai_video_task', 'view_count',
    "INT NOT NULL DEFAULT 0 COMMENT '浏览量' AFTER like_count");

CALL _add_col('ai_video_task', 'admin_status',
    "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '管理状态 0-正常 1-封禁' AFTER view_count");

-- 清理列工具过程
DROP PROCEDURE IF EXISTS _add_col;

-- 索引（兼容 MySQL 8.0 所有小版本，不依赖 CREATE INDEX IF NOT EXISTS）
CALL _add_idx('ai_video_task', 'idx_admin_status', '(admin_status)');
CALL _add_idx('ai_video_task', 'idx_date_status',  '(create_time, status)');

-- 清理索引工具过程
DROP PROCEDURE IF EXISTS _add_idx;

-- ----------------------------------------------------------------
-- 3. 统计中间表：用户生成次数汇总（避免每次全表聚合）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS virtual_user_stats (
  user_id          BIGINT   NOT NULL COMMENT '用户ID',
  total_gen_count  INT      NOT NULL DEFAULT 0 COMMENT '累计生成次数',
  fail_gen_count   INT      NOT NULL DEFAULT 0 COMMENT '生成失败次数',
  last_gen_time    DATETIME DEFAULT NULL COMMENT '最近生成时间',
  update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                   ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='用户生成统计中间表（事件驱动+每日定时刷新）';

-- ----------------------------------------------------------------
-- 4. 视频 API 每日统计中间表（按日期预聚合，加速 API 统计页面）
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS virtual_api_daily_stats (
  stat_date       DATE          NOT NULL COMMENT '统计日期',
  total_count     INT           NOT NULL DEFAULT 0 COMMENT '调用总次数',
  success_count   INT           NOT NULL DEFAULT 0 COMMENT '成功次数',
  fail_count      INT           NOT NULL DEFAULT 0 COMMENT '失败次数',
  timeout_count   INT           NOT NULL DEFAULT 0 COMMENT '超时次数',
  avg_duration_s  DECIMAL(10,2) DEFAULT NULL COMMENT '平均生成耗时（秒）',
  success_rate    DECIMAL(5,2)  DEFAULT NULL COMMENT '成功率（百分比）',
  create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='视频API每日统计中间表（定时任务每日凌晨刷新）';

-- ----------------------------------------------------------------
-- 5. 菜单数据：平台管理（父菜单）
-- ----------------------------------------------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time, remark)
VALUES ('Virtual Platform', 0, 5, 'virtual', NULL, 1, 0, 'M', '0', '0', '', 'peoples', 'virtual.menu.title', 'admin', NOW(), 'Virtual Pet Platform Management');

SET @parent_id = LAST_INSERT_ID();

-- 1. Platform Users
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Platform Users', @parent_id, 1, 'user', 'virtual/user/index', 'VirtualUser', 1, 0, 'C', '0', '0', 'virtual:user:list', 'user', 'virtual.menu.user', 'admin', NOW());
SET @user_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time) VALUES
('Query User', @user_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:user:query',       '#', 'button.query', 'admin', NOW()),
('Disable Generate', @user_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'virtual:user:banGenerate',  '#', 'virtual.user.banGenerate', 'admin', NOW()),
('Disable Account', @user_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'virtual:user:disable',      '#', 'button.disable', 'admin', NOW()),
('Export',     @user_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'virtual:user:export',       '#', 'button.export', 'admin', NOW());

-- 2. Video Management
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Video Management', @parent_id, 2, 'video', 'virtual/video/index', 'VirtualVideo', 1, 0, 'C', '0', '0', 'virtual:video:list', 'monitor', 'virtual.menu.video', 'admin', NOW());
SET @video_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time) VALUES
('Query Video', @video_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:video:query',  '#', 'button.query', 'admin', NOW()),
('Ban Video', @video_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'virtual:video:ban',    '#', 'virtual.video.ban', 'admin', NOW()),
('Unban Video', @video_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'virtual:video:unban',  '#', 'virtual.video.unban', 'admin', NOW());

-- 3. Checkin Management
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Checkin Management', @parent_id, 3, 'checkin', 'virtual/checkin/index', 'VirtualCheckin', 1, 0, 'C', '0', '0', 'virtual:checkin:list', 'date', 'virtual.menu.checkin', 'admin', NOW());
SET @checkin_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Query Checkin', @checkin_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:checkin:query', '#', 'button.query', 'admin', NOW());

-- 4. Credits Flow
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Credits Flow', @parent_id, 4, 'credits', 'virtual/credits/index', 'VirtualCredits', 1, 0, 'C', '0', '0', 'virtual:credits:list', 'money', 'virtual.menu.credits', 'admin', NOW());
SET @credits_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Query Credits', @credits_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:credits:query', '#', 'button.query', 'admin', NOW());

-- 5. Order Management
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Order Management', @parent_id, 5, 'order', 'virtual/order/index', 'VirtualOrder', 1, 0, 'C', '0', '0', 'virtual:order:list', 'shopping', 'virtual.menu.order', 'admin', NOW());
SET @order_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Query Order', @order_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:order:query', '#', 'button.query', 'admin', NOW());

-- 6. API Stats
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('API Stats', @parent_id, 6, 'stats', 'virtual/stats/index', 'VirtualStats', 1, 0, 'C', '0', '0', 'virtual:stats:list', 'chart', 'virtual.menu.stats', 'admin', NOW());
SET @stats_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, i18n_key, create_by, create_time)
VALUES ('Query Stats', @stats_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'virtual:stats:query', '#', 'button.query', 'admin', NOW());


--  Auto-generated SQL script #202603011821
UPDATE app_db.sys_menu
	SET icon='dict'
	WHERE menu_id=2002;

-- Safety patch for users who already ran the older script version without route_name
UPDATE app_db.sys_menu SET route_name = 'VirtualUser' WHERE component = 'virtual/user/index';
UPDATE app_db.sys_menu SET route_name = 'VirtualVideo' WHERE component = 'virtual/video/index';
UPDATE app_db.sys_menu SET route_name = 'VirtualCheckin' WHERE component = 'virtual/checkin/index';
UPDATE app_db.sys_menu SET route_name = 'VirtualCredits' WHERE component = 'virtual/credits/index';
UPDATE app_db.sys_menu SET route_name = 'VirtualOrder' WHERE component = 'virtual/order/index';
UPDATE app_db.sys_menu SET route_name = 'VirtualStats' WHERE component = 'virtual/stats/index';
