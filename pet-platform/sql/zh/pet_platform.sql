-- ----------------------------
-- PEI AI Platform - 业务模块数据库脚本
-- 版本: 1.0.0
-- 日期: 2024
-- 说明: 包含文件管理、Web3支付、AI视频生成三个模块的表结构
-- ----------------------------

-- ----------------------------
-- 1、文件信息表
-- ----------------------------
drop table if exists sys_file_info;
create table sys_file_info (
  file_id           bigint(20)      not null auto_increment    comment '文件ID',
  file_name         varchar(255)    not null                   comment '文件名称',
  file_key          varchar(500)    not null                   comment '存储Key（路径）',
  file_url          varchar(500)    not null                   comment '文件访问URL',
  file_size         bigint(20)      not null                   comment '文件大小（字节）',
  file_type         varchar(50)     default null               comment '文件类型（image/video/document）',
  mime_type         varchar(100)    default null               comment 'MIME类型',
  file_ext          varchar(20)     default null               comment '文件扩展名',
  storage_provider  varchar(50)     default 's3'               comment '存储提供商（s3/aliyun/minio）',
  bucket_name       varchar(100)    not null                   comment '存储桶名称',
  upload_user_id    bigint(20)      default null               comment '上传用户ID',
  upload_user_type  varchar(20)     default null               comment '用户类型（admin/app_user）',
  business_type     varchar(50)     default null               comment '业务类型',
  business_id       bigint(20)      default null               comment '关联业务ID',
  status            char(1)         default '0'                comment '状态（0正常 1删除）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (file_id),
  unique key uk_file_key (file_key),
  key idx_upload_user (upload_user_id),
  key idx_business (business_type, business_id),
  key idx_storage_provider (storage_provider),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = '文件信息表';

-- ----------------------------
-- 2、区块链网络配置表
-- ----------------------------
drop table if exists sys_blockchain_config;
create table sys_blockchain_config (
  config_id           bigint(20)      not null auto_increment    comment '配置ID',
  network_type        varchar(50)     not null                   comment '网络类型（ethereum_mainnet/ethereum_testnet/bsc_mainnet/bsc_testnet）',
  network_name        varchar(100)    not null                   comment '网络名称',
  chain_id            int(11)         not null                   comment '链ID',
  rpc_url             varchar(500)    not null                   comment 'RPC节点URL',
  explorer_url        varchar(500)    default null               comment '区块链浏览器URL',
  wallet_address      varchar(100)    not null                   comment '平台收款钱包地址',
  scan_start_block    bigint(20)      default 0                  comment '扫描起始区块高度',
  scan_current_block  bigint(20)      default 0                  comment '当前扫描到的区块高度',
  scan_batch_size     int(11)         default 100                comment '每次扫描区块数量',
  min_confirmations   int(11)         default 12                 comment '最小确认数',
  status              char(1)         default '1'                comment '状态（0停用 1启用）',
  create_by           varchar(64)     default ''                 comment '创建者',
  create_time         datetime                                   comment '创建时间',
  update_by           varchar(64)     default ''                 comment '更新者',
  update_time         datetime                                   comment '更新时间',
  remark              varchar(500)    default null               comment '备注',
  primary key (config_id),
  unique key uk_network_type (network_type)
) engine=innodb auto_increment=100 comment = '区块链网络配置表';

-- ----------------------------
-- 初始化-区块链网络配置
-- ----------------------------
insert into sys_blockchain_config values(1, 'ethereum_mainnet', '以太坊主网',     1,         'https://mainnet.infura.io/v3/', 'https://etherscan.io',          '', 0, 0, 100, 12, '0', 'admin', sysdate(), '', null, '以太坊主网配置');
insert into sys_blockchain_config values(2, 'ethereum_testnet', '以太坊测试网',   11155111,  'https://sepolia.infura.io/v3/', 'https://sepolia.etherscan.io',  '', 0, 0, 100, 12, '1', 'admin', sysdate(), '', null, '以太坊Sepolia测试网');
insert into sys_blockchain_config values(3, 'bsc_mainnet',      'BSC主网',        56,        'https://bsc-dataseed1.binance.org', 'https://bscscan.com',       '', 0, 0, 100, 15, '0', 'admin', sysdate(), '', null, '币安智能链主网');
insert into sys_blockchain_config values(4, 'bsc_testnet',      'BSC测试网',      97,        'https://data-seed-prebsc-1-s1.binance.org:8545', 'https://testnet.bscscan.com', '', 0, 0, 100, 15, '1', 'admin', sysdate(), '', null, '币安智能链测试网');

-- ----------------------------
-- 3、代币配置表
-- ----------------------------
drop table if exists sys_token_config;
create table sys_token_config (
  token_id          bigint(20)      not null auto_increment    comment '代币ID',
  network_type      varchar(50)     not null                   comment '所属网络类型',
  token_symbol      varchar(20)     not null                   comment '代币符号（USDT/BNB/ETH）',
  token_name        varchar(100)    not null                   comment '代币名称',
  contract_address  varchar(100)    default null               comment '合约地址（原生币为NULL）',
  decimals          int(11)         not null default 18        comment '小数位数',
  is_native         char(1)         default '0'                comment '是否原生币（0否 1是）',
  logo_url          varchar(500)    default null               comment 'Logo图片URL',
  status            char(1)         default '1'                comment '状态（0停用 1启用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (token_id),
  unique key uk_network_token (network_type, token_symbol)
) engine=innodb auto_increment=100 comment = '代币配置表';

-- ----------------------------
-- 初始化-代币配置
-- ----------------------------
-- 以太坊主网代币
insert into sys_token_config values(1, 'ethereum_mainnet', 'ETH',  'Ethereum',   null,                                          18, '1', null, '0', 'admin', sysdate(), '', null, '以太坊原生币');
insert into sys_token_config values(2, 'ethereum_mainnet', 'USDT', 'Tether USD', '0xdAC17F958D2ee523a2206206994597C13D831ec7', 6,  '0', null, '0', 'admin', sysdate(), '', null, 'USDT稳定币(ERC20)');
-- 以太坊测试网代币
insert into sys_token_config values(3, 'ethereum_testnet', 'ETH',  'Ethereum',   null,                                          18, '1', null, '1', 'admin', sysdate(), '', null, '以太坊测试网原生币');
insert into sys_token_config values(4, 'ethereum_testnet', 'USDT', 'Tether USD', '',                                            6,  '0', null, '1', 'admin', sysdate(), '', null, 'USDT测试代币');
-- BSC主网代币
insert into sys_token_config values(5, 'bsc_mainnet', 'BNB',  'Binance Coin', null,                                          18, '1', null, '0', 'admin', sysdate(), '', null, 'BNB原生币');
insert into sys_token_config values(6, 'bsc_mainnet', 'USDT', 'Tether USD',   '0x55d398326f99059fF775485246999027B3197955', 18, '0', null, '0', 'admin', sysdate(), '', null, 'USDT稳定币(BEP20)');
-- BSC测试网代币
insert into sys_token_config values(7, 'bsc_testnet', 'BNB',  'Binance Coin', null,                                          18, '1', null, '1', 'admin', sysdate(), '', null, 'BNB测试网原生币');
insert into sys_token_config values(8, 'bsc_testnet', 'USDT', 'Tether USD',   '',                                            18, '0', null, '1', 'admin', sysdate(), '', null, 'USDT测试代币');

-- ----------------------------
-- 4、支付交易记录表
-- ----------------------------
drop table if exists pet_payment_transaction;
create table pet_payment_transaction (
  tx_id             bigint(20)       not null auto_increment    comment '交易ID',
  tx_hash           varchar(100)     not null                   comment '交易哈希',
  network_type      varchar(50)      not null                   comment '网络类型',
  block_number      bigint(20)       not null                   comment '区块高度',
  block_timestamp   bigint(20)       not null                   comment '区块时间戳',
  from_address      varchar(100)     not null                   comment '发送方地址',
  to_address        varchar(100)     not null                   comment '接收方地址',
  token_symbol      varchar(20)      not null                   comment '代币符号',
  token_address     varchar(100)     default null               comment '代币合约地址',
  amount            decimal(36, 18)  not null                   comment '转账金额（原始值）',
  amount_display    varchar(50)      not null                   comment '显示金额',
  gas_price         varchar(50)      default null               comment 'Gas价格',
  gas_used          varchar(50)      default null               comment 'Gas消耗',
  tx_fee            varchar(50)      default null               comment '交易手续费',
  status            varchar(20)      default 'pending'          comment '交易状态（pending/confirmed/failed）',
  confirmations     int(11)          default 0                  comment '确认数',
  user_id           bigint(20)       default null               comment '关联用户ID',
  business_type     varchar(50)      default null               comment '业务类型（recharge/purchase）',
  business_id       bigint(20)       default null               comment '关联业务ID',
  is_processed      char(1)          default '0'                comment '是否已处理（0否 1是）',
  process_time      datetime         default null               comment '处理时间',
  remark            varchar(500)     default null               comment '备注',
  create_time       datetime         not null default current_timestamp comment '创建时间',
  update_time       datetime         not null default current_timestamp on update current_timestamp comment '更新时间',
  primary key (tx_id),
  unique key uk_tx_hash (tx_hash),
  key idx_network_block (network_type, block_number),
  key idx_to_address (to_address),
  key idx_from_address (from_address),
  key idx_user (user_id),
  key idx_status (status, is_processed),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = '支付交易记录表';

-- ----------------------------
-- 5、AI视频生成任务表
-- ----------------------------
drop table if exists ai_video_task;
create table ai_video_task (
  task_id           bigint(20)      not null auto_increment    comment '任务ID',
  task_uuid         varchar(100)    not null                   comment '任务UUID',
  provider          varchar(50)     default 'openai'           comment 'AI提供商（openai/runway/stable/aliyun）',
  provider_task_id  varchar(200)    default null               comment '提供商任务ID',
  user_id           bigint(20)      not null                   comment '用户ID',
  prompt_text       text            default null               comment '文本提示词',
  prompt_image_url  varchar(500)    default null               comment '图片提示URL',
  prompt_image_file_id bigint(20)   default null               comment '图片提示文件ID',
  model_name        varchar(50)     default 'sora-1.0'         comment '模型名称',
  video_duration    int(11)         default 10                 comment '视频时长（秒）',
  video_resolution  varchar(20)     default '1080p'            comment '视频分辨率',
  video_aspect_ratio varchar(20)    default '16:9'             comment '视频宽高比',
  status            varchar(20)     default 'pending'          comment '任务状态（pending/processing/completed/failed/cancelled）',
  progress          int(11)         default 0                  comment '生成进度（0-100）',
  provider_video_url varchar(500)   default null               comment '提供商视频URL',
  oss_video_url     varchar(500)    default null               comment 'OSS视频URL',
  file_id           bigint(20)      default null               comment '关联文件ID',
  error_code        varchar(50)     default null               comment '错误代码',
  error_message     text            default null               comment '错误信息',
  retry_count       int(11)         default 0                  comment '重试次数',
  max_retry         int(11)         default 3                  comment '最大重试次数',
  estimated_time    int(11)         default null               comment '预计完成时间（秒）',
  cost_amount       decimal(10, 4)  default null               comment '消费金额（USD）',
  started_at        datetime        default null               comment '开始时间',
  completed_at      datetime        default null               comment '完成时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (task_id),
  unique key uk_task_uuid (task_uuid),
  key idx_user (user_id),
  key idx_status (status),
  key idx_provider (provider),
  key idx_create_time (create_time)
) engine=innodb auto_increment=100 comment = 'AI视频生成任务表';

-- ----------------------------
-- 6、AI模型配置表
-- ----------------------------
drop table if exists ai_model_config;
create table ai_model_config (
  model_id          bigint(20)      not null auto_increment    comment '模型ID',
  provider          varchar(50)     not null                   comment 'AI提供商',
  model_name        varchar(100)    not null                   comment '模型名称',
  model_display_name varchar(100)   not null                   comment '模型显示名称',
  model_type        varchar(50)     not null                   comment '模型类型（text_to_video/image_to_video）',
  api_endpoint      varchar(500)    default null               comment 'API端点',
  price_per_second  decimal(10, 4)  default 0                  comment '每秒价格（USD）',
  max_duration      int(11)         default 60                 comment '最大视频时长（秒）',
  supported_resolutions varchar(200) default null              comment '支持的分辨率（逗号分隔）',
  status            char(1)         default '1'                comment '状态（0停用 1启用）',
  sort_order        int(11)         default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (model_id),
  unique key uk_provider_model (provider, model_name)
) engine=innodb auto_increment=100 comment = 'AI模型配置表';

-- ----------------------------
-- 初始化-AI模型配置
-- ----------------------------
insert into ai_model_config values(1, 'openai', 'sora-1.0',     'OpenAI Sora',          'text_to_video',  null, 0.0500, 60, '480p,720p,1080p', '1', 1, 'admin', sysdate(), '', null, 'OpenAI Sora视频生成模型');
insert into ai_model_config values(2, 'openai', 'sora-1.0-hd',  'OpenAI Sora HD',       'text_to_video',  null, 0.0800, 60, '1080p,4k',        '1', 2, 'admin', sysdate(), '', null, 'OpenAI Sora高清模型');
insert into ai_model_config values(3, 'runway', 'gen-3-alpha',  'Runway Gen-3 Alpha',   'text_to_video',  null, 0.0500, 10, '720p,1080p',      '0', 3, 'admin', sysdate(), '', null, 'Runway Gen-3 Alpha模型');
insert into ai_model_config values(4, 'runway', 'gen-3-alpha-turbo', 'Runway Gen-3 Turbo', 'image_to_video', null, 0.0300, 10, '720p,1080p',  '0', 4, 'admin', sysdate(), '', null, 'Runway Gen-3 Turbo模型');

-- ----------------------------
-- 7、菜单SQL - Web3管理
-- ----------------------------
-- Web3管理主菜单（一级菜单）
insert into sys_menu values('2000', 'Web3管理', '0', 5, 'web3', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'menu.web3.title', 'admin', sysdate(), '', null, 'Web3管理目录');

-- 区块链网络配置
insert into sys_menu values('2001', '区块链配置', '2000', 1, 'blockchain', 'web3/blockchain/index', '', '', 1, 0, 'C', '0', '0', 'system:blockchain:list', 'link', 'menu.web3.blockchain', 'admin', sysdate(), '', null, '区块链网络配置');

-- 代币配置
insert into sys_menu values('2002', '代币配置', '2000', 2, 'token', 'web3/token/index', '', '', 1, 0, 'C', '0', '0', 'system:token:list', 'component', 'menu.web3.token', 'admin', sysdate(), '', null, '代币配置');

-- 交易记录
insert into sys_menu values('2003', '交易记录', '2000', 3, 'transaction', 'web3/transaction/index', '', '', 1, 0, 'C', '0', '0', 'system:transaction:list', 'list', 'menu.web3.transaction', 'admin', sysdate(), '', null, '支付交易记录');

-- ----------------------------
-- 8、字典类型
-- ----------------------------
insert into sys_dict_type values(100, '文件类型',   'sys_file_type',      '0', 'admin', sysdate(), '', null, '文件类型列表');
insert into sys_dict_type values(101, '存储提供商', 'sys_storage_provider', '0', 'admin', sysdate(), '', null, '文件存储提供商');
insert into sys_dict_type values(102, '区块链网络', 'sys_blockchain_network', '0', 'admin', sysdate(), '', null, '区块链网络类型');
insert into sys_dict_type values(103, '交易状态',   'sys_tx_status',      '0', 'admin', sysdate(), '', null, '区块链交易状态');
insert into sys_dict_type values(104, 'AI任务状态', 'ai_task_status',     '0', 'admin', sysdate(), '', null, 'AI任务状态');
insert into sys_dict_type values(105, 'AI提供商',   'ai_provider',        '0', 'admin', sysdate(), '', null, 'AI服务提供商');

-- ----------------------------
-- 9、字典数据
-- ----------------------------
-- 文件类型
insert into sys_dict_data values(1001, 1, '图片',   'image',    'sys_file_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '图片文件');
insert into sys_dict_data values(1002, 2, '视频',   'video',    'sys_file_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '视频文件');
insert into sys_dict_data values(1003, 3, '文档',   'document', 'sys_file_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '文档文件');
insert into sys_dict_data values(1004, 4, '其他',   'other',    'sys_file_type', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '其他文件');

-- 存储提供商
insert into sys_dict_data values(1011, 1, 'S3兼容存储', 's3',     'sys_storage_provider', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, 'S3兼容存储（AWS/R2/MinIO）');
insert into sys_dict_data values(1012, 2, '阿里云OSS',  'aliyun', 'sys_storage_provider', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '阿里云OSS存储');
insert into sys_dict_data values(1013, 3, 'MinIO',      'minio',  'sys_storage_provider', '', 'info',    'N', '0', 'admin', sysdate(), '', null, 'MinIO自建存储');

-- 区块链网络
insert into sys_dict_data values(1021, 1, '以太坊主网', 'ethereum_mainnet', 'sys_blockchain_network', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1022, 2, '以太坊测试网', 'ethereum_testnet', 'sys_blockchain_network', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1023, 3, 'BSC主网',    'bsc_mainnet', 'sys_blockchain_network', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1024, 4, 'BSC测试网',  'bsc_testnet', 'sys_blockchain_network', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- 交易状态
insert into sys_dict_data values(1031, 1, '待确认', 'pending',   'sys_tx_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1032, 2, '已确认', 'confirmed', 'sys_tx_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1033, 3, '失败',   'failed',    'sys_tx_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');

-- AI任务状态
insert into sys_dict_data values(1041, 1, '待处理',  'pending',    'ai_task_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1042, 2, '处理中',  'processing', 'ai_task_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1043, 3, '已完成',  'completed',  'ai_task_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1044, 4, '失败',    'failed',     'ai_task_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(1045, 5, '已取消',  'cancelled',  'ai_task_status', '', 'default', 'N', '0', 'admin', sysdate(), '', null, '');

-- AI提供商
insert into sys_dict_data values(1051, 1, 'OpenAI',  'openai',  'ai_provider', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, 'OpenAI Sora');
insert into sys_dict_data values(1052, 2, 'Runway',  'runway',  'ai_provider', '', 'success', 'N', '0', 'admin', sysdate(), '', null, 'Runway ML');
insert into sys_dict_data values(1053, 3, 'Stable',  'stable',  'ai_provider', '', 'info',    'N', '0', 'admin', sysdate(), '', null, 'Stable Video');
insert into sys_dict_data values(1054, 4, '阿里云',  'aliyun',  'ai_provider', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '阿里云视觉智能');
