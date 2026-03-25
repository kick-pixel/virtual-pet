-- ----------------------------
-- PEI AI Platform - Business Module Database Script (English Version)
-- Version: 1.0.0
-- Date: 2024
-- Description: Tables for File Management, Web3 Payment, AI Video Generation
-- ----------------------------

-- ----------------------------
-- 1. File Information Table
-- ----------------------------
drop table if exists sys_file_info;
create table sys_file_info (
  file_id           bigint(20)      not null auto_increment    comment 'File ID',
  file_name         varchar(255)    not null                   comment 'File Name',
  file_key          varchar(500)    not null                   comment 'Storage Key (Path)',
  file_url          varchar(500)    not null                   comment 'File Access URL',
  file_size         bigint(20)      not null                   comment 'File Size (Bytes)',
  file_type         varchar(50)     default null               comment 'File Type (image/video/document)',
  mime_type         varchar(100)    default null               comment 'MIME Type',
  file_ext          varchar(20)     default null               comment 'File Extension',
  storage_provider  varchar(50)     default 's3'               comment 'Storage Provider (s3/aliyun/minio)',
  bucket_name       varchar(100)    not null                   comment 'Bucket Name',
  upload_user_id    bigint(20)      default null               comment 'Upload User ID',
  upload_user_type  varchar(20)     default null               comment 'User Type (admin/app_user)',
  business_type     varchar(50)     default null               comment 'Business Type',
  business_id       bigint(20)      default null               comment 'Related Business ID',
  status            char(1)         default '0'                comment 'Status (0=Normal 1=Deleted)',
  remark            varchar(500)    default null               comment 'Remark',
  create_by         varchar(64)     default ''                 comment 'Created By',
  create_time       datetime                                   comment 'Created Time',
  update_by         varchar(64)     default ''                 comment 'Updated By',
  update_time       datetime                                   comment 'Updated Time',
  primary key (file_id),
  unique key uk_file_key (file_key),
  key idx_upload_user (upload_user_id),
  key idx_business (business_type, business_id),
  key idx_storage_provider (storage_provider),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = 'File Information';

-- ----------------------------
-- 2. Blockchain Network Configuration Table
-- ----------------------------
drop table if exists sys_blockchain_config;
create table sys_blockchain_config (
  config_id           bigint(20)      not null auto_increment    comment 'Config ID',
  network_type        varchar(50)     not null                   comment 'Network Type',
  network_name        varchar(100)    not null                   comment 'Network Name',
  chain_id            int(11)         not null                   comment 'Chain ID',
  rpc_url             varchar(500)    not null                   comment 'RPC Node URL',
  explorer_url        varchar(500)    default null               comment 'Block Explorer URL',
  wallet_address      varchar(100)    not null                   comment 'Platform Wallet Address',
  scan_start_block    bigint(20)      default 0                  comment 'Scan Start Block',
  scan_current_block  bigint(20)      default 0                  comment 'Current Scan Block',
  scan_batch_size     int(11)         default 100                comment 'Scan Batch Size',
  min_confirmations   int(11)         default 12                 comment 'Min Confirmations',
  status              char(1)         default '1'                comment 'Status (0=Disabled 1=Enabled)',
  create_by           varchar(64)     default ''                 comment 'Created By',
  create_time         datetime                                   comment 'Created Time',
  update_by           varchar(64)     default ''                 comment 'Updated By',
  update_time         datetime                                   comment 'Updated Time',
  remark              varchar(500)    default null               comment 'Remark',
  primary key (config_id),
  unique key uk_network_type (network_type)
) engine=innodb auto_increment=100 comment = 'Blockchain Network Configuration';

-- ----------------------------
-- Initialize Blockchain Network Configuration
-- ----------------------------
INSERT INTO sys_blockchain_config (config_id, network_type, network_name, chain_id, rpc_url, explorer_url, wallet_address, scan_start_block, scan_current_block, scan_batch_size, min_confirmations, status, create_by, create_time, update_by, update_time, remark) VALUES(1, 'ethereum_mainnet', 'Ethereum Mainnet', 1, 'https://mainnet.infura.io/v3/', 'https://etherscan.io', '', 24444730, 24444730, 100, 12, '1', 'admin', '2026-02-08 13:16:06', '', '2026-02-08 14:20:59', 'Ethereum Mainnet Configuration');
INSERT INTO sys_blockchain_config (config_id, network_type, network_name, chain_id, rpc_url, explorer_url, wallet_address, scan_start_block, scan_current_block, scan_batch_size, min_confirmations, status, create_by, create_time, update_by, update_time, remark) VALUES(2, 'ethereum_testnet', 'Ethereum Testnet', 11155111, 'https://api.zan.top/node/v1/eth/sepolia/dfb12dcea03a4c1eb71a44acbfc10618', 'https://sepolia.etherscan.io', '0x99155e3f3558A07C58ADB02e8565582FB53aaa57', 10216000, 10249118, 100, 12, '0', 'admin', '2026-02-08 13:16:06', '', '2026-02-13 10:15:17', 'Ethereum Sepolia Testnet');
INSERT INTO sys_blockchain_config (config_id, network_type, network_name, chain_id, rpc_url, explorer_url, wallet_address, scan_start_block, scan_current_block, scan_batch_size, min_confirmations, status, create_by, create_time, update_by, update_time, remark) VALUES(3, 'bsc_mainnet', 'BSC Mainnet', 56, 'https://bsc-dataseed1.binance.org', 'https://bscscan.com', '', 80894274, 80894274, 100, 15, '1', 'admin', '2026-02-08 13:16:07', '', '2026-02-08 14:21:00', 'Binance Smart Chain Mainnet');

-- ----------------------------
-- 3. Token Configuration Table
-- ----------------------------
drop table if exists sys_token_config;
create table sys_token_config (
  token_id          bigint(20)      not null auto_increment    comment 'Token ID',
  network_type      varchar(50)     not null                   comment 'Network Type',
  token_symbol      varchar(20)     not null                   comment 'Token Symbol (USDT/BNB/ETH)',
  token_name        varchar(100)    not null                   comment 'Token Name',
  contract_address  varchar(100)    default null               comment 'Contract Address (NULL for native)',
  decimals          int(11)         not null default 18        comment 'Decimals',
  is_native         char(1)         default '0'                comment 'Is Native Token (0=No 1=Yes)',
  logo_url          varchar(500)    default null               comment 'Logo URL',
  status            char(1)         default '1'                comment 'Status (0=Disabled 1=Enabled)',
  create_by         varchar(64)     default ''                 comment 'Created By',
  create_time       datetime                                   comment 'Created Time',
  update_by         varchar(64)     default ''                 comment 'Updated By',
  update_time       datetime                                   comment 'Updated Time',
  remark            varchar(500)    default null               comment 'Remark',
  primary key (token_id),
  unique key uk_network_token (network_type, token_symbol)
) engine=innodb auto_increment=100 comment = 'Token Configuration';

-- ----------------------------
-- Initialize Token Configuration
-- ----------------------------
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(1, 'ethereum_mainnet', 'ETH', 'Ethereum', NULL, 18, '1', NULL, '1', 'admin', '2026-02-08 13:16:15', '', '2026-02-08 16:08:17', 'Ethereum Native Token');
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(2, 'ethereum_mainnet', 'USDT', 'Tether USD', '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6, '0', NULL, '1', 'admin', '2026-02-08 13:16:16', '', '2026-02-08 16:08:18', 'USDT Stablecoin (ERC20)');
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(3, 'ethereum_testnet', 'ETH', 'Ethereum', NULL, 18, '1', NULL, '0', 'admin', '2026-02-08 13:16:16', '', '2026-02-08 15:22:54', 'Ethereum Testnet Native Token');
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(4, 'ethereum_testnet', 'USDT', 'Tether USD', '0xF9AB1c552cEB4665074C854B70ae9eeF72BC5e10', 6, '0', NULL, '1', 'admin', '2026-02-08 13:16:16', '', '2026-02-08 16:08:20', 'USDT Test Token');
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(5, 'bsc_mainnet', 'BNB', 'Binance Coin', NULL, 18, '1', NULL, '1', 'admin', '2026-02-08 13:16:16', '', '2026-02-08 16:08:20', 'BNB Native Token');
INSERT INTO sys_token_config (token_id, network_type, token_symbol, token_name, contract_address, decimals, is_native, logo_url, status, create_by, create_time, update_by, update_time, remark) VALUES(6, 'bsc_mainnet', 'USDT', 'Tether USD', '0x55d398326f99059fF775485246999027B3197955', 18, '0', NULL, '1', 'admin', '2026-02-08 13:16:17', '', '2026-02-08 16:08:21', 'USDT Stablecoin (BEP20)');


-- ----------------------------
-- 4. Payment Transaction Table
-- ----------------------------
drop table if exists pet_payment_transaction;
create table pet_payment_transaction (
  tx_id             bigint(20)       not null auto_increment    comment 'Transaction ID',
  tx_hash           varchar(100)     not null                   comment 'Transaction Hash',
  network_type      varchar(50)      not null                   comment 'Network Type',
  block_number      bigint(20)       not null                   comment 'Block Number',
  block_timestamp   bigint(20)       not null                   comment 'Block Timestamp',
  from_address      varchar(100)     not null                   comment 'From Address',
  to_address        varchar(100)     not null                   comment 'To Address',
  token_symbol      varchar(20)      not null                   comment 'Token Symbol',
  token_address     varchar(100)     default null               comment 'Token Contract Address',
  amount            decimal(36, 18)  not null                   comment 'Amount (Raw Value)',
  amount_display    varchar(50)      not null                   comment 'Display Amount',
  gas_price         varchar(50)      default null               comment 'Gas Price',
  gas_used          varchar(50)      default null               comment 'Gas Used',
  tx_fee            varchar(50)      default null               comment 'Transaction Fee',
  status            varchar(20)      default 'pending'          comment 'Status (pending/confirmed/failed)',
  confirmations     int(11)          default 0                  comment 'Confirmations',
  user_id           bigint(20)       default null               comment 'Related User ID',
  business_type     varchar(50)      default null               comment 'Business Type (recharge/purchase)',
  business_id       bigint(20)       default null               comment 'Related Business ID',
  is_processed      char(1)          default '0'                comment 'Is Processed (0=No 1=Yes)',
  process_time      datetime         default null               comment 'Process Time',
  remark            varchar(500)     default null               comment 'Remark',
  create_time       datetime         not null default current_timestamp comment 'Created Time',
  update_time       datetime         not null default current_timestamp on update current_timestamp comment 'Updated Time',
  primary key (tx_id),
  unique key uk_tx_hash (tx_hash),
  key idx_network_block (network_type, block_number),
  key idx_to_address (to_address),
  key idx_from_address (from_address),
  key idx_user (user_id),
  key idx_status (status, is_processed),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = 'Payment Transaction';

-- ----------------------------
-- 5. AI Video Task Table
-- ----------------------------
drop table if exists ai_video_task;
create table ai_video_task (
  task_id           bigint(20)      not null auto_increment    comment 'Task ID',
  task_uuid         varchar(100)    not null                   comment 'Task UUID',
  provider          varchar(50)     default 'openai'           comment 'AI Provider (openai/runway/stable/aliyun)',
  provider_task_id  varchar(200)    default null               comment 'Provider Task ID',
  user_id           bigint(20)      not null                   comment 'User ID',
  prompt_text       text            default null               comment 'Text Prompt',
  prompt_image_url  varchar(500)    default null               comment 'Image Prompt URL',
  prompt_image_file_id bigint(20)   default null               comment 'Image Prompt File ID',
  model_name        varchar(50)     default 'sora-1.0'         comment 'Model Name',
  video_duration    int(11)         default 10                 comment 'Video Duration (seconds)',
  video_resolution  varchar(20)     default '1080p'            comment 'Video Resolution',
  video_aspect_ratio varchar(20)    default '16:9'             comment 'Video Aspect Ratio',
  status            varchar(20)     default 'pending'          comment 'Status (pending/processing/completed/failed/cancelled)',
  progress          int(11)         default 0                  comment 'Progress (0-100)',
  provider_video_url varchar(500)   default null               comment 'Provider Video URL',
  oss_video_url     varchar(500)    default null               comment 'OSS Video URL',
  file_id           bigint(20)      default null               comment 'Related File ID',
  error_code        varchar(50)     default null               comment 'Error Code',
  error_message     text            default null               comment 'Error Message',
  retry_count       int(11)         default 0                  comment 'Retry Count',
  max_retry         int(11)         default 3                  comment 'Max Retry',
  estimated_time    int(11)         default null               comment 'Estimated Time (seconds)',
  cost_amount       decimal(10, 4)  default null               comment 'Cost Amount (USD)',
  started_at        datetime        default null               comment 'Started At',
  completed_at      datetime        default null               comment 'Completed At',
  create_by         varchar(64)     default ''                 comment 'Created By',
  create_time       datetime                                   comment 'Created Time',
  update_by         varchar(64)     default ''                 comment 'Updated By',
  update_time       datetime                                   comment 'Updated Time',
  remark            varchar(500)    default null               comment 'Remark',
  primary key (task_id),
  unique key uk_task_uuid (task_uuid),
  key idx_user (user_id),
  key idx_status (status),
  key idx_provider (provider),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = 'AI Video Task';

-- ----------------------------
-- 6. AI Model Configuration Table
-- ----------------------------
drop table if exists ai_model_config;
create table ai_model_config (
  model_id          bigint(20)      not null auto_increment    comment 'Model ID',
  provider          varchar(50)     not null                   comment 'AI Provider',
  model_name        varchar(100)    not null                   comment 'Model Name',
  model_display_name varchar(100)   not null                   comment 'Model Display Name',
  model_type        varchar(50)     not null                   comment 'Model Type (text_to_video/image_to_video)',
  api_endpoint      varchar(500)    default null               comment 'API Endpoint',
  price_per_second  decimal(10, 4)  default 0                  comment 'Price Per Second (USD)',
  max_duration      int(11)         default 60                 comment 'Max Duration (seconds)',
  supported_resolutions varchar(200) default null              comment 'Supported Resolutions (comma separated)',
  status            char(1)         default '1'                comment 'Status (0=Disabled 1=Enabled)',
  sort_order        int(11)         default 0                  comment 'Sort Order',
  create_by         varchar(64)     default ''                 comment 'Created By',
  create_time       datetime                                   comment 'Created Time',
  update_by         varchar(64)     default ''                 comment 'Updated By',
  update_time       datetime                                   comment 'Updated Time',
  remark            varchar(500)    default null               comment 'Remark',
  primary key (model_id),
  unique key uk_provider_model (provider, model_name)
) engine=innodb auto_increment=100 comment = 'AI Model Configuration';

-- ----------------------------
-- Initialize AI Model Configuration
-- ----------------------------
insert into ai_model_config values(1, 'openai', 'sora-1.0',     'OpenAI Sora',          'text_to_video',  null, 0.0500, 60, '480p,720p,1080p', '1', 1, 'admin', sysdate(), '', null, 'OpenAI Sora Video Generation Model');
insert into ai_model_config values(2, 'openai', 'sora-1.0-hd',  'OpenAI Sora HD',       'text_to_video',  null, 0.0800, 60, '1080p,4k',        '1', 2, 'admin', sysdate(), '', null, 'OpenAI Sora High Definition Model');
insert into ai_model_config values(3, 'runway', 'gen-3-alpha',  'Runway Gen-3 Alpha',   'text_to_video',  null, 0.0500, 10, '720p,1080p',      '0', 3, 'admin', sysdate(), '', null, 'Runway Gen-3 Alpha Model');
insert into ai_model_config values(4, 'runway', 'gen-3-alpha-turbo', 'Runway Gen-3 Turbo', 'image_to_video', null, 0.0300, 10, '720p,1080p',  '0', 4, 'admin', sysdate(), '', null, 'Runway Gen-3 Turbo Model');

-- 7. Menu SQL - Web3 Management
-- ----------------------------
-- Web3 Management Menu (Top Level)
insert into sys_menu values('2000', 'Web3 Management', '0', 5, 'web3', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'menu.web3.title', 'admin', sysdate(), '', null, 'Web3 Management');

-- Blockchain Network Configuration
insert into sys_menu values('2001', 'Blockchain Config', '2000', 1, 'blockchain', 'web3/blockchain/index', '', '', 1, 0, 'C', '0', '0', 'system:blockchain:list', 'link', 'menu.web3.blockchain', 'admin', sysdate(), '', null, 'Blockchain Network Configuration');

-- Token Configuration
insert into sys_menu values('2002', 'Token Config', '2000', 2, 'token', 'web3/token/index', '', '', 1, 0, 'C', '0', '0', 'system:token:list', 'component', 'menu.web3.token', 'admin', sysdate(), '', null, 'Token Configuration');

-- Transaction Records
insert into sys_menu values('2003', 'Transactions', '2000', 3, 'transaction', 'web3/transaction/index', '', '', 1, 0, 'C', '0', '0', 'system:transaction:list', 'list', 'menu.web3.transaction', 'admin', sysdate(), '', null, 'Payment Transactions');

-- ----------------------------
-- 8. Dictionary Types
-- ----------------------------
insert into sys_dict_type values(100, 'File Type',          'sys_file_type',          '0', 'admin', sysdate(), '', null, 'File Type List');
insert into sys_dict_type values(101, 'Storage Provider',   'sys_storage_provider',   '0', 'admin', sysdate(), '', null, 'File Storage Provider');
insert into sys_dict_type values(102, 'Blockchain Network', 'sys_blockchain_network', '0', 'admin', sysdate(), '', null, 'Blockchain Network Type');
insert into sys_dict_type values(103, 'Transaction Status', 'sys_tx_status',          '0', 'admin', sysdate(), '', null, 'Blockchain Transaction Status');
insert into sys_dict_type values(104, 'AI Task Status',     'ai_task_status',         '0', 'admin', sysdate(), '', null, 'AI Task Status');
insert into sys_dict_type values(105, 'AI Provider',        'ai_provider',            '0', 'admin', sysdate(), '', null, 'AI Service Provider');

-- ----------------------------
-- 9. Dictionary Data
-- ----------------------------
-- File Type
insert into sys_dict_data values(1001, 1, 'Image',    'image',    'sys_file_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, 'Image File');
insert into sys_dict_data values(1002, 2, 'Video',    'video',    'sys_file_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'Video File');
insert into sys_dict_data values(1003, 3, 'Document', 'document', 'sys_file_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, 'Document File');
insert into sys_dict_data values(1004, 4, 'Other',    'other',    'sys_file_type', '', 'default', 'N', '0', 'admin', sysdate(), '', null, 'Other File');

-- Storage Provider
insert into sys_dict_data values(1011, 1, 'S3 Compatible', 's3',     'sys_storage_provider', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, 'S3 Compatible Storage (AWS/R2/MinIO)');
insert into sys_dict_data values(1012, 2, 'Aliyun OSS',    'aliyun', 'sys_storage_provider', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'Aliyun OSS Storage');
insert into sys_dict_data values(1013, 3, 'MinIO',         'minio',  'sys_storage_provider', '', 'info',    'N', '0', 'admin', sysdate(), '', null, 'MinIO Self-hosted Storage');

-- Blockchain Network
insert into sys_dict_data values(1021, 1, 'Ethereum Mainnet', 'ethereum_mainnet', 'sys_blockchain_network', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1022, 2, 'Ethereum Testnet', 'ethereum_testnet', 'sys_blockchain_network', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1023, 3, 'BSC Mainnet',      'bsc_mainnet',      'sys_blockchain_network', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1024, 4, 'BSC Testnet',      'bsc_testnet',      'sys_blockchain_network', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- Transaction Status
insert into sys_dict_data values(1031, 1, 'Pending',   'pending',   'sys_tx_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1032, 2, 'Confirmed', 'confirmed', 'sys_tx_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1033, 3, 'Failed',    'failed',    'sys_tx_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');

-- AI Task Status
insert into sys_dict_data values(1041, 1, 'Pending',    'pending',    'ai_task_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1042, 2, 'Processing', 'processing', 'ai_task_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1043, 3, 'Completed',  'completed',  'ai_task_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1044, 4, 'Failed',     'failed',     'ai_task_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1045, 5, 'Cancelled',  'cancelled',  'ai_task_status', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');

-- AI Provider
insert into sys_dict_data values(1051, 1, 'OpenAI',       'openai',  'ai_provider', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, 'OpenAI Sora');
insert into sys_dict_data values(1052, 2, 'Runway',       'runway',  'ai_provider', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'Runway ML');
insert into sys_dict_data values(1053, 3, 'Stable Video', 'stable',  'ai_provider', '', 'info',    'N', '0', 'admin', sysdate(), '', null, 'Stable Video');
insert into sys_dict_data values(1054, 4, 'Aliyun',       'aliyun',  'ai_provider', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, 'Aliyun Vision AI');
