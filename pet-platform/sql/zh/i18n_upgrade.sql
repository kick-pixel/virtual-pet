-- ============================================
-- 国际化升级脚本
-- 用途：为已有数据库添加国际化支持
-- 执行前请备份数据库！
-- ============================================

-- 1. 为 sys_menu 表添加 i18n_key 字段
ALTER TABLE sys_menu ADD COLUMN i18n_key varchar(100) DEFAULT '' COMMENT '国际化KEY';

-- 2. 更新现有菜单数据的 i18n_key

-- 一级菜单（4个）
UPDATE sys_menu SET i18n_key = 'menu.system.title' WHERE menu_id = 1;
UPDATE sys_menu SET i18n_key = 'menu.monitor.title' WHERE menu_id = 2;
UPDATE sys_menu SET i18n_key = 'menu.tool.title' WHERE menu_id = 3;
UPDATE sys_menu SET i18n_key = 'menu.guide' WHERE menu_id = 4;

-- 二级菜单 - 系统管理
UPDATE sys_menu SET i18n_key = 'menu.system.user' WHERE menu_id = 100;
UPDATE sys_menu SET i18n_key = 'menu.system.role' WHERE menu_id = 101;
UPDATE sys_menu SET i18n_key = 'menu.system.menu' WHERE menu_id = 102;
UPDATE sys_menu SET i18n_key = 'menu.system.dept' WHERE menu_id = 103;
UPDATE sys_menu SET i18n_key = 'menu.system.post' WHERE menu_id = 104;
UPDATE sys_menu SET i18n_key = 'menu.system.dict' WHERE menu_id = 105;
UPDATE sys_menu SET i18n_key = 'menu.system.config' WHERE menu_id = 106;
UPDATE sys_menu SET i18n_key = 'menu.system.notice' WHERE menu_id = 107;
UPDATE sys_menu SET i18n_key = 'menu.system.log' WHERE menu_id = 108;

-- 二级菜单 - 系统监控
UPDATE sys_menu SET i18n_key = 'menu.monitor.online' WHERE menu_id = 109;
UPDATE sys_menu SET i18n_key = 'menu.monitor.job' WHERE menu_id = 110;
UPDATE sys_menu SET i18n_key = 'menu.monitor.druid' WHERE menu_id = 111;
UPDATE sys_menu SET i18n_key = 'menu.monitor.server' WHERE menu_id = 112;
UPDATE sys_menu SET i18n_key = 'menu.monitor.cache' WHERE menu_id = 113;
UPDATE sys_menu SET i18n_key = 'menu.monitor.cacheList' WHERE menu_id = 114;

-- 二级菜单 - 系统工具
UPDATE sys_menu SET i18n_key = 'menu.tool.build' WHERE menu_id = 115;
UPDATE sys_menu SET i18n_key = 'menu.tool.gen' WHERE menu_id = 116;
UPDATE sys_menu SET i18n_key = 'menu.tool.swagger' WHERE menu_id = 117;

-- 三级菜单 - 日志管理
UPDATE sys_menu SET i18n_key = 'menu.monitor.operlog' WHERE menu_id = 500;
UPDATE sys_menu SET i18n_key = 'menu.monitor.logininfor' WHERE menu_id = 501;

-- 3. 更新按钮权限的 i18n_key（用户管理按钮示例）
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1000;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1001;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1002;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1003;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1004;
UPDATE sys_menu SET i18n_key = 'button.import' WHERE menu_id = 1005;
UPDATE sys_menu SET i18n_key = 'button.resetPwd' WHERE menu_id = 1006;

-- 角色管理按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1007;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1008;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1009;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1010;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1011;

-- 菜单管理按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1012;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1013;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1014;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1015;

-- 部门管理按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1016;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1017;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1018;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1019;

-- 岗位管理按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1020;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1021;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1022;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1023;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1024;

-- 字典管理按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1025;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1026;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1027;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1028;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1029;

-- 参数设置按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1030;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1031;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1032;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1033;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1034;

-- 通知公告按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1035;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1036;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1037;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1038;

-- 操作日志按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1039;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1040;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1041;

-- 登录日志按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1042;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1043;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1044;

-- 在线用户按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1045;
UPDATE sys_menu SET i18n_key = 'button.force' WHERE menu_id = 1046;

-- 定时任务按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1047;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1048;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1049;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1050;
UPDATE sys_menu SET i18n_key = 'button.export' WHERE menu_id = 1051;

-- 代码生成按钮
UPDATE sys_menu SET i18n_key = 'button.query' WHERE menu_id = 1055;
UPDATE sys_menu SET i18n_key = 'button.add' WHERE menu_id = 1056;
UPDATE sys_menu SET i18n_key = 'button.edit' WHERE menu_id = 1057;
UPDATE sys_menu SET i18n_key = 'button.remove' WHERE menu_id = 1058;

-- 4. 验证更新结果
SELECT menu_id, menu_name, i18n_key
FROM sys_menu
WHERE i18n_key IS NOT NULL AND i18n_key != ''
ORDER BY menu_id;

-- 升级完成提示
SELECT '国际化升级脚本执行完成！' AS message;
SELECT CONCAT('已更新 ', COUNT(*), ' 条菜单记录的 i18n_key 字段') AS summary
FROM sys_menu
WHERE i18n_key IS NOT NULL AND i18n_key != '';
