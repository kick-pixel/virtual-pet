-- ============================================================
-- 功能迭代脚本 v1.4 | 日期：2026-02-27
-- 包含：视频点赞记录、每日签到记录、ai_video_task 添加点赞计数
-- ============================================================

-- 1. 视频点赞记录表
CREATE TABLE IF NOT EXISTS `virtual_task_like` (
  `like_id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id`     BIGINT      NOT NULL                COMMENT '用户ID',
  `task_id`     BIGINT      NOT NULL                COMMENT '视频任务ID',
  `create_time` DATETIME    NOT NULL DEFAULT NOW()  COMMENT '点赞时间',
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `uk_user_task` (`user_id`, `task_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频点赞记录';

-- 2. 每日签到记录表
CREATE TABLE IF NOT EXISTS `virtual_daily_checkin` (
  `checkin_id`      BIGINT   NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `user_id`         BIGINT   NOT NULL                COMMENT '用户ID',
  `checkin_date`    DATE     NOT NULL                COMMENT '签到日期',
  `day_sequence`    INT      NOT NULL DEFAULT 1      COMMENT '连续签到第几天(1-7)',
  `credits_awarded` INT      NOT NULL DEFAULT 0      COMMENT '本次奖励积分',
  `create_time`     DATETIME NOT NULL DEFAULT NOW()  COMMENT '创建时间',
  PRIMARY KEY (`checkin_id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日签到记录';

-- 3. ai_video_task 添加点赞计数字段
-- 注意：该表无 share_count 列，like_count 添加在 completed_at 之后
ALTER TABLE `ai_video_task`
  ADD COLUMN IF NOT EXISTS `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `completed_at`;
