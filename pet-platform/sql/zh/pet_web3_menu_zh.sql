-- ----------------------------
-- Web3 管理菜单和权限数据 (中文版)
-- 在初始化基础系统表后运行此脚本
-- ----------------------------

-- ----------------------------
-- Web3管理目录 (一级菜单)
-- menu_id 从 2000 开始以避免冲突
-- ----------------------------
INSERT INTO sys_menu VALUES('2000', 'Web3管理', '0', '4', 'web3', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'web3.menu.title', 'admin', sysdate(), '', NULL, 'Web3区块链管理目录');

-- ----------------------------
-- Web3管理菜单 (二级菜单)
-- ----------------------------
-- 区块链网络配置
INSERT INTO sys_menu VALUES('2001', '区块链网络', '2000', '1', 'blockchain', 'web3/blockchain/index', '', '', 1, 0, 'C', '0', '0', 'web3:blockchain:list', 'link', 'web3.menu.blockchain', 'admin', sysdate(), '', NULL, '区块链网络配置菜单');

-- 代币配置
INSERT INTO sys_menu VALUES('2002', '代币配置', '2000', '2', 'token', 'web3/token/index', '', '', 1, 0, 'C', '0', '0', 'web3:token:list', 'coin', 'web3.menu.token', 'admin', sysdate(), '', NULL, '代币配置菜单');

-- 交易记录
INSERT INTO sys_menu VALUES('2003', '交易记录', '2000', '3', 'transaction', 'web3/transaction/index', '', '', 1, 0, 'C', '0', '0', 'web3:transaction:list', 'documentation', 'web3.menu.transaction', 'admin', sysdate(), '', NULL, '交易记录菜单');

-- ----------------------------
-- 区块链网络管理按钮 (三级)
-- ----------------------------
INSERT INTO sys_menu VALUES('2010', '区块链查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2011', '区块链新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:add', '#', 'button.add', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2012', '区块链修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:edit', '#', 'button.edit', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2013', '区块链删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:remove', '#', 'button.remove', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2014', '区块链导出', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2015', '测试连接', '2001', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:test', '#', 'web3.blockchain.testConnection', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 代币配置按钮 (三级)
-- ----------------------------
INSERT INTO sys_menu VALUES('2020', '代币查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2021', '代币新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:add', '#', 'button.add', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2022', '代币修改', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:edit', '#', 'button.edit', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2023', '代币删除', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:remove', '#', 'button.remove', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2024', '代币导出', '2002', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 交易记录按钮 (三级)
-- ----------------------------
INSERT INTO sys_menu VALUES('2030', '交易查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2031', '交易导出', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2032', '触发扫描', '2003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:scan', '#', 'web3.transaction.scanNow', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- 为普通角色分配Web3权限 (role_id = 2)
-- 授予只读访问权限
-- ----------------------------
INSERT INTO sys_role_menu VALUES ('2', '2000');
INSERT INTO sys_role_menu VALUES ('2', '2001');
INSERT INTO sys_role_menu VALUES ('2', '2002');
INSERT INTO sys_role_menu VALUES ('2', '2003');
INSERT INTO sys_role_menu VALUES ('2', '2010');
INSERT INTO sys_role_menu VALUES ('2', '2020');
INSERT INTO sys_role_menu VALUES ('2', '2030');

-- ----------------------------
-- Web3模块字典类型
-- ----------------------------
INSERT INTO sys_dict_type VALUES(100, '区块链网络类型', 'web3_network_type', '0', 'admin', sysdate(), '', NULL, '区块链网络类型列表');
INSERT INTO sys_dict_type VALUES(101, '交易状态', 'web3_tx_status', '0', 'admin', sysdate(), '', NULL, '区块链交易状态列表');
INSERT INTO sys_dict_type VALUES(102, '代币类型', 'web3_token_type', '0', 'admin', sysdate(), '', NULL, '代币类型列表（原生/ERC20）');

-- ----------------------------
-- Web3模块字典数据
-- ----------------------------
-- 网络类型
INSERT INTO sys_dict_data VALUES(100, 1, '以太坊主网', 'ethereum', 'web3_network_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'Ethereum主网');
INSERT INTO sys_dict_data VALUES(101, 2, '以太坊测试网', 'ethereum_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Ethereum Sepolia测试网');
INSERT INTO sys_dict_data VALUES(102, 3, '币安智能链', 'bsc', 'web3_network_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'BSC主网');
INSERT INTO sys_dict_data VALUES(103, 4, '币安测试网', 'bsc_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'BSC测试网');
INSERT INTO sys_dict_data VALUES(104, 5, 'Polygon主网', 'polygon', 'web3_network_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'Polygon主网');
INSERT INTO sys_dict_data VALUES(105, 6, 'Polygon测试网', 'polygon_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Polygon Mumbai测试网');

-- 交易状态
INSERT INTO sys_dict_data VALUES(110, 1, '待确认', '0', 'web3_tx_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, '交易待确认');
INSERT INTO sys_dict_data VALUES(111, 2, '已确认', '1', 'web3_tx_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', NULL, '交易已确认');
INSERT INTO sys_dict_data VALUES(112, 3, '失败', '2', 'web3_tx_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, '交易失败');

-- 代币类型
INSERT INTO sys_dict_data VALUES(120, 1, '原生代币', '1', 'web3_token_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, '原生区块链代币（ETH/BNB/MATIC）');
INSERT INTO sys_dict_data VALUES(121, 2, 'ERC20代币', '0', 'web3_token_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, 'ERC20/BEP20合约代币');
