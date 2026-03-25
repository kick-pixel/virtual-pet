-- ----------------------------
-- Web3 Management Menu and Permission Data
-- Run this script after initializing the base system tables
-- ----------------------------

-- ----------------------------
-- Web3 Management Directory (Level 1 Menu)
-- menu_id starts from 2000 to avoid conflicts
-- ----------------------------
INSERT INTO sys_menu VALUES('2000', 'Web3 Management', '0', '4', 'web3', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'web3.menu.title', 'admin', sysdate(), '', NULL, 'Web3 Blockchain Management Directory');

-- ----------------------------
-- Web3 Management Menus (Level 2 Menus)
-- ----------------------------
-- Blockchain Network Configuration
INSERT INTO sys_menu VALUES('2001', 'Blockchain Networks', '2000', '1', 'blockchain', 'web3/blockchain/index', '', '', 1, 0, 'C', '0', '0', 'web3:blockchain:list', 'link', 'web3.menu.blockchain', 'admin', sysdate(), '', NULL, 'Blockchain Network Configuration Menu');

-- Token Configuration
INSERT INTO sys_menu VALUES('2002', 'Token Config', '2000', '2', 'token', 'web3/token/index', '', '', 1, 0, 'C', '0', '0', 'web3:token:list', 'coin', 'web3.menu.token', 'admin', sysdate(), '', NULL, 'Token Configuration Menu');

-- Transaction Records
INSERT INTO sys_menu VALUES('2003', 'Transactions', '2000', '3', 'transaction', 'web3/transaction/index', '', '', 1, 0, 'C', '0', '0', 'web3:transaction:list', 'documentation', 'web3.menu.transaction', 'admin', sysdate(), '', NULL, 'Transaction Records Menu');

-- ----------------------------
-- Blockchain Network Management Buttons (Level 3)
-- ----------------------------
INSERT INTO sys_menu VALUES('2010', 'Blockchain Query', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2011', 'Blockchain Add', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:add', '#', 'button.add', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2012', 'Blockchain Edit', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:edit', '#', 'button.edit', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2013', 'Blockchain Delete', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:remove', '#', 'button.remove', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2014', 'Blockchain Export', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2015', 'Test Connection', '2001', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:blockchain:test', '#', 'web3.blockchain.testConnection', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- Token Configuration Buttons (Level 3)
-- ----------------------------
INSERT INTO sys_menu VALUES('2020', 'Token Query', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2021', 'Token Add', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:add', '#', 'button.add', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2022', 'Token Edit', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:edit', '#', 'button.edit', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2023', 'Token Delete', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:remove', '#', 'button.remove', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2024', 'Token Export', '2002', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:token:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- Transaction Records Buttons (Level 3)
-- ----------------------------
INSERT INTO sys_menu VALUES('2030', 'Transaction Query', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:query', '#', 'button.query', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2031', 'Transaction Export', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:export', '#', 'button.export', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES('2032', 'Trigger Scan', '2003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'web3:transaction:scan', '#', 'web3.transaction.scanNow', 'admin', sysdate(), '', NULL, '');

-- ----------------------------
-- Assign Web3 permissions to admin role (role_id = 1)
-- Super admin already has all permissions, but for explicit tracking
-- ----------------------------
-- Note: Super admin (role_id=1) has all permissions by default

-- ----------------------------
-- Assign Web3 permissions to common role (role_id = 2)
-- Grant read-only access to common role
-- ----------------------------
INSERT INTO sys_role_menu VALUES ('2', '2000');
INSERT INTO sys_role_menu VALUES ('2', '2001');
INSERT INTO sys_role_menu VALUES ('2', '2002');
INSERT INTO sys_role_menu VALUES ('2', '2003');
INSERT INTO sys_role_menu VALUES ('2', '2010');
INSERT INTO sys_role_menu VALUES ('2', '2020');
INSERT INTO sys_role_menu VALUES ('2', '2030');

-- ----------------------------
-- Dictionary Types for Web3 Module
-- ----------------------------
INSERT INTO sys_dict_type VALUES(100, 'Blockchain Network Type', 'web3_network_type', '0', 'admin', sysdate(), '', NULL, 'Blockchain network type list');
INSERT INTO sys_dict_type VALUES(101, 'Transaction Status', 'web3_tx_status', '0', 'admin', sysdate(), '', NULL, 'Blockchain transaction status list');
INSERT INTO sys_dict_type VALUES(102, 'Token Type', 'web3_token_type', '0', 'admin', sysdate(), '', NULL, 'Token type list (native/ERC20)');

-- ----------------------------
-- Dictionary Data for Web3 Module
-- ----------------------------
-- Network Types
INSERT INTO sys_dict_data VALUES(100, 1, 'Ethereum Mainnet', 'ethereum', 'web3_network_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', NULL, 'Ethereum Mainnet');
INSERT INTO sys_dict_data VALUES(101, 2, 'Ethereum Testnet', 'ethereum_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Ethereum Sepolia Testnet');
INSERT INTO sys_dict_data VALUES(102, 3, 'BSC Mainnet', 'bsc', 'web3_network_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'Binance Smart Chain Mainnet');
INSERT INTO sys_dict_data VALUES(103, 4, 'BSC Testnet', 'bsc_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Binance Smart Chain Testnet');
INSERT INTO sys_dict_data VALUES(104, 5, 'Polygon Mainnet', 'polygon', 'web3_network_type', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'Polygon Mainnet');
INSERT INTO sys_dict_data VALUES(105, 6, 'Polygon Testnet', 'polygon_testnet', 'web3_network_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Polygon Mumbai Testnet');

-- Transaction Status
INSERT INTO sys_dict_data VALUES(110, 1, 'Pending', '0', 'web3_tx_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', NULL, 'Transaction pending confirmation');
INSERT INTO sys_dict_data VALUES(111, 2, 'Confirmed', '1', 'web3_tx_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', NULL, 'Transaction confirmed');
INSERT INTO sys_dict_data VALUES(112, 3, 'Failed', '2', 'web3_tx_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', NULL, 'Transaction failed');

-- Token Types
INSERT INTO sys_dict_data VALUES(120, 1, 'Native Token', '1', 'web3_token_type', '', 'success', 'N', '0', 'admin', sysdate(), '', NULL, 'Native blockchain token (ETH/BNB/MATIC)');
INSERT INTO sys_dict_data VALUES(121, 2, 'ERC20 Token', '0', 'web3_token_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', NULL, 'ERC20/BEP20 contract token');
