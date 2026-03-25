# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于若依(RuoYi) v3.9.1 框架的前后端分离项目 - **PEI AI PLATFORM**，包含两个独立 Web 应用和三大核心业务模块。

| 应用 | 端口 | 前端目录 | 技术栈 |
|------|------|----------|--------|
| **ruoyi-admin** (管理后台) | 8181 | `frontend/admin-web` | Vue 3 + Element Plus + SCSS |
| **pet-virtual** (虚拟宠物平台) | 8182 | `frontend/virtual-web` | Vue 3 + TypeScript + Ant Design Vue + Radix Vue + Web3 (wagmi + viem) |

三大核心业务模块（两个应用共享）：

- **pet-file**: 文件存储 (AWS S3 SDK v2，兼容 R2/MinIO/S3)
- **pay-web3**: Web3 区块链支付 (EVM 链交易扫描)
- **pet-ai**: AI 视频生成 (OpenAI Sora / 火山引擎 Seedance / Grok)

## 开发环境要求

- **后端**: Java 21, Maven 3.8+, MySQL 8.0+, Redis 6.0+, Spring Boot 3.5.8
- **前端**: Node.js 18+, npm

## 常用命令

### 后端

```bash
# 编译（在 pet-platform/ 目录下）
cd pet-platform && mvn clean package -DskipTests

# 启动管理后台 (端口 8181)
cd pet-platform/ruoyi-admin && mvn spring-boot:run

# 启动虚拟平台 (端口 8182)
java -jar pet-platform/pet-virtual/target/pet-virtual.jar --spring.profiles.active=dev

# 生产环境管理脚本（在 pet-platform/ 目录下执行）
./ry.sh start|stop|restart|status|tail         # 管理后台 (ruoyi-admin)
./pet-virtual.sh start|stop|restart|status|tail # 虚拟平台 (pet-virtual)

# Windows 环境脚本
./ry.bat
./pet-virtual.bat

# 运行单个测试
cd pet-platform && mvn test -Dtest=YourTestClass
cd pet-platform && mvn test -Dtest=YourTestClass#testMethod
```

### 前端 - 管理后台 (admin-web)

```bash
cd frontend/admin-web
npm install
npm run dev          # 开发 (代理 /dev-api -> localhost:8181)
npm run build:prod   # 生产构建
npm run build:stage  # 测试环境构建
```

### 前端 - 虚拟宠物平台 (virtual-web)

```bash
cd frontend/virtual-web
npm install
npm run dev          # 开发 (端口 3000，代理 /api -> localhost:8182)
npm run build        # 生产构建 (vue-tsc 类型检查 + vite build)
```

## 项目架构

### 后端模块结构

```text
pet-platform/
├── ruoyi-admin/          # 管理后台 Web 应用 (端口 8181)
├── pet-virtual/          # 虚拟宠物平台 Web 应用 (端口 8182)
├── ruoyi-common/         # 通用工具、注解、异常处理、事件定义
├── ruoyi-framework/      # 框架核心 (Security, Redis, Swagger, AOP)
├── ruoyi-system/         # 系统模块 + 所有业务 Entity/Mapper/Service
├── ruoyi-quartz/         # 定时任务
├── ruoyi-generator/      # 代码生成器
├── pet-file/             # 文件存储模块 (S3)
├── pay-web3/             # Web3 支付模块
└── pet-ai/               # AI 视频生成模块
```

**关键约定**：新增模块的 Entity/Mapper/Service 统一放在 `ruoyi-system`，Controller 放在各 Web 应用模块中。

### pet-virtual 模块（虚拟平台独立应用）

独立于 ruoyi-admin 的完整 Web 应用，有自己的 Security 体系：

- **Controller 层** (10 个): VirtualAuth, VirtualCheckin, VirtualConfig, VirtualCredits, VirtualNotification, VirtualPayment, VirtualShare, VirtualShowcase, VirtualTask, TelegramWebhook
- **Security**: 独立的 JWT 认证 (`VirtualJwtAuthenticationFilter`, `VirtualTokenService`, `WalletSignatureVerifier`)
- **OAuth 登录**: Google, Microsoft, Twitter (位于 `oauth/provider/`)
- **Telegram Bot**: 完整命令系统 (`telegram/handler/` - Start, Login, Buy, Generate, Balance, Command)
- **社交分享**: TikTok, Twitter (位于 `share/`)
- **事件监听器**: PaymentCreditsListener, VideoCompletedListener, VideoFailedListener, UserRegisteredListener
- **定时任务**: BlockchainScanTask, AiVideoTaskScheduler（从 pet-virtual 自身的 task/ 包运行）

### 事件驱动架构

所有事件类位于 `ruoyi-common/src/main/java/com/ruoyi/common/event/`：

- **AI 事件** (`ai/`): VideoGenerationCreatedEvent → VideoGenerationCompletedEvent / VideoGenerationFailedEvent
- **Web3 事件** (`web3/`): TransactionDetectedEvent → TransactionConfirmedEvent → PaymentReceivedEvent

```java
// 监听器必须异步执行
@EventListener
@Async("eventExecutor")
public void handlePaymentReceived(PaymentReceivedEvent event) { ... }
```

### 视频处理队列

IO 密集操作（视频下载+OSS上传）通过 Redisson `RBlockingQueue` 异步处理：

```text
定时任务 → 状态变completed → 队列消息 → 消费者线程（下载+上传） → 发布事件
```

核心组件位于 `pet-ai/queue/`: VideoProcessQueueProducer, VideoProcessQueueConsumer

### 后端配置文件

位于 ruoyi-admin 或 pet-virtual 的 `src/main/resources/` 下：

- `application.yml`: 主配置（Redis、JWT、端口等）
- `application-druid.yml`: 数据源配置
- `application-pet.yml`: 三大模块配置（S3、Web3、AI）
- `logback.xml`: 日志配置

**日志文件**: `app.log` (全量), `error.log` (错误), `task.log` (定时任务+队列), `user.log` (操作日志)

## 开发规范

### 后端规范

- **API 风格**: `@RestController` + `AjaxResult` 统一响应
- **分层**: Controller → Service → Mapper (MyBatis XML 映射在 `resources/mapper/`)
- **权限**: `@PreAuthorize("@ss.hasPermi('system:user:list')")` + `@Log` 操作日志
- **分页**: Controller 继承 `BaseController`，调用 `startPage()` 启动分页
- **定时任务**: 所有 `@Scheduled` 必须加 `@DistributedLock` 防并发
- **事件监听**: 必须 `@Async("eventExecutor")`，保证幂等性

### 前端 (virtual-web)

- **TypeScript 模板限制**: 模板中禁止使用 `as` 类型断言，需在 `<script setup>` 中定义辅助函数
- **v-model 指令**: 使用 `:prop` + `@update:prop` 模式替代 `v-model:visible`
- **i18n 消息** (vue-i18n v9 `legacy: false`): 禁止半角 `|` `{` `}` `@:` `@.`，用全角字符替代
- **路由国际化**: 使用 `meta: { titleKey: 'route.xxx' }`，不硬编码标题
- **Web3 钱包**: wagmi + viem + @web3modal/wagmi，封装在 `composables/useWallet.ts`
- **数据请求**: @tanstack/vue-query 管理服务端状态
- **瀑布流**: @yeger/vue-masonry-wall + vue-waterfall-plugin-next
- **视频播放**: Plyr
- **网络/代币配置**: 从后端动态加载，不硬编码

### 前端 (admin-web)

- **权限指令**: `v-hasPermi="['system:user:add']"` 控制按钮
- **路由**: `constantRoutes`（静态）+ `dynamicRoutes`（根据后端菜单动态加载）
- **API 代理**: `/dev-api` → `http://localhost:8181`

## 数据库

### 核心表

- **系统表**: `sys_user`, `sys_role`, `sys_menu`, `sys_dept`
- **文件**: `sys_file_info`
- **Web3**: `sys_blockchain_config`, `sys_token_config`, `pet_payment_transaction`
- **AI**: `ai_model_config`, `ai_video_task`
- **Virtual 平台**: `virtual_user`, `virtual_user_oauth`, `virtual_user_wallet`, `virtual_user_credits`, `virtual_credits_transaction`, `virtual_credits_rule`, `virtual_recharge_package`, `virtual_recharge_order`, `virtual_generation_option`, `virtual_pet_showcase`, `virtual_notification`, `virtual_email_log`, `virtual_task_like`, `virtual_daily_checkin`

### 初始化

```bash
mysql -u root -p -e "CREATE DATABASE pet_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
# 中文版（推荐）
mysql -u root -p pet_platform < pet-platform/sql/zh/pet_platform.sql
mysql -u root -p pet_platform < pet-platform/sql/zh/pet_web3_menu_zh.sql
mysql -u root -p pet_platform < pet-platform/sql/zh/quartz.sql
mysql -u root -p pet_platform < pet-platform/sql/4.virtual_init.sql
```

### 增量迁移

迭代脚本放在 `pet-platform/sql/`，命名规则：`N.feature_description_YYYYMMDD.sql`（如 `9.admin_panel_20260301.sql`）。使用 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` 保证幂等。

## 核心业务流程

### Web3 充值流程

#### 前端流程（`frontend/virtual-web/src/views/credits/Recharge.vue`）

1. 页面加载时并行获取：套餐列表、平台收款地址（按 chainId）、支持的网络、可用代币
2. 用户选择套餐 + 代币 → 连接 MetaMask（`<w3m-button />`）
3. `handlePay()` 触发：
   - `POST /api/virtual/payment/order` 创建订单，携带 `{ packageId, chainId, payToken, walletAddress }`
   - 后端按 `chainId` 查 `sys_blockchain_config`，返回 `{ orderNo, payAmount, receiveAddress, creditsAmount }`
   - `sendTransactionAsync({ to: 平台地址, value: parseEther(payAmount) })` 发起链上转账
   - `POST /api/virtual/payment/order/{orderNo}/tx-hash` 将 `txHash` 提交给后端（网络抖动时允许失败，有兜底逻辑）
   - 订单状态变为 `paid`，启动轮询（每 5 秒，最多 60 次 / 5 分钟）
4. 轮询 `GET /api/virtual/payment/order/{orderNo}` → 状态变 `completed` 时刷新积分余额

#### 后端区块链监听（`BlockchainScanTask` → `TransactionScanServiceImpl`）

`BlockchainScanTask` 在 `ApplicationReadyEvent` 后延迟启动，守护线程持续循环：

```text
loop:
  1. scanAllNetworks()  — 并行扫描所有已启用网络的新区块
     - 每个网络独立线程（scanExecutor，min(4, CPU核数)）
     - 从 sys_blockchain_config.scan_current_block 记录进度
     - 新交易 → 入库 pet_payment_transaction (status=pending) → publishTransactionDetectedEvent
  2. updatePendingTransactions() — 并行轮询待确认交易
     - RPC 查询确认数 >= min_confirmations → 验证链上是否成功
     - 成功 → status=confirmed → 匹配充值订单（见下）→ completeOrder()
     - 失败 → status=failed → publishTransactionFailedEvent
  3. 有新活动 → sleep 500ms；否则 sleep 2000ms（跟上 BSC ~3s 出块）
  多实例：Redisson tryLock (SCAN_LOCK / UPDATE_LOCK)，抢不到直接跳过本轮
```

#### 订单匹配逻辑（`processSinglePendingTx`）

交易确认后按优先级匹配充值订单：

1. **精确匹配**：`getOrderByTxHash(txHash)` — 前端成功提交了 txHash 时命中
2. **兜底匹配**：`findMatchingPendingOrder(fromAddress, tokenSymbol, networkType, txHash)` — 按发款地址+代币+网络查最近 pending 订单，应对前端未能提交 txHash 的情况

匹配成功 → `completeOrder(orderId, txHash, txId)`：更新订单 `status=completed`，调用 `creditsService.addCredits()` 按套餐面值（creditsAmount + bonusCredits）充值积分。

未匹配到订单但 `userId != null` → 走旧路径 `publishPaymentReceivedEvent` → `PaymentCreditsListener.onPaymentReceived()` → 按代币固定汇率估算积分（BNB×10000、USDT×100、ETH×20000）。

#### 订单状态流转

```text
pending（已创建）→ paid（前端已提交txHash）→ completed（链上确认 + 积分到账）
                                             → cancelled（用户取消）
                                             → expired（超时未支付）
```

### 视频生成流程

#### 前端流程（`frontend/virtual-web/src/views/generate/`）

1. `Create.vue` 页面加载时：获取生成选项（`GET /api/virtual/task/options`，来自 `virtual_generation_option` 表），实时估算积分消耗（`GET /api/virtual/credits/estimate`）
2. 可选上传参考图（`POST /api/virtual/task/upload-image` → 返回 `fileId`）
3. 提交 `POST /api/virtual/task/create`，携带 `{ prompt, fileId?, duration, resolution, aspectRatio }`：
   - 校验账号状态（`status=1` 且 `genDisabled!=1`）
   - 检查积分余额 >= cost（当前固定 30 积分）
   - 调用 AI 提供商创建任务，返回 `{ taskId, taskUuid, providerTaskId, status }`
   - **冻结积分**：`freezeCredits(userId, cost, "video_task", taskId)` — 积分从可用余额移入冻结区，不扣除
4. 跳转 `Progress.vue`，每 3 秒轮询 `GET /api/virtual/task/{taskId}/progress`
5. 状态变 `completed` → 跳转结果页；状态变 `failed` → 显示错误信息

#### 后端任务同步（`AiVideoTaskScheduler`，每 7 秒）

```text
@Scheduled(fixedDelay=7000) + @DistributedLock("schedule:ai:video:sync")
  syncProcessingTasks():
    查询所有 status=processing 的任务
    → aiVideoService.syncTaskStatus(taskId)   // RPC 轮询 AI 提供商
    → status 变为 completed → videoProcessQueueProducer.send(taskId)
    → status 变为 failed    → publishVideoGenerationFailedEvent
```

#### 视频后处理队列（`VideoProcessQueueConsumer`）

`@PostConstruct` 启动守护线程，阻塞监听 Redisson `RBlockingQueue`（最多 3 次重试）：

```text
队列消息 → processCompletedTask(taskId):
  1. 流式下载 providerVideoUrl → 写入本地临时文件（8KB buffer，不占堆内存）
  2. 流式上传临时文件 → OSS（pet-file 模块）→ 得到 fileId
  3. 提取视频首帧 → 上传为封面图（videoPicFileId）
  4. 更新 ai_video_task（ossVideoUrl、fileId、videoPicFileId）
  5. publishVideoGenerationCompletedEvent
  6. 删除临时文件（finally 块保证）
```

#### 积分结算（事件监听，`@Async("eventExecutor")`）

- **完成** → `VideoCompletedListener.onVideoCompleted()`：
  `confirmSpend(userId, actualCost, "video_completed", taskId)` — 从冻结区正式扣除，并 `+1 userStats.totalGenCount`

- **失败** → `VideoFailedListener.onVideoFailed()`：
  防重复退还检查 → 查原始 freeze 交易获取金额 → `refundFrozen(userId, amount, "video_task", taskId)` — 积分从冻结区归还到可用余额

#### 积分状态流转

```text
余额(balance) ──freezeCredits──> 冻结(frozen_balance)
                                    │
                 confirmSpend <─────┤ 任务成功（从冻结扣除）
                 refundFrozen <─────┘ 任务失败（归还到余额）
```

#### 任务状态流转

```text
pending → processing（AI 提供商接受）→ completed（提供商完成）→ [队列处理后] ossVideoUrl 已填充
                                      → failed（提供商失败或同步超时）
```

## 注意事项

- 两个 Web 应用有**独立的 Security 配置**：ruoyi-admin 用框架自带的，pet-virtual 用自己的 `VirtualJwtAuthenticationFilter`（各自有独立的 `application.yml`, `application-druid.yml`, `application-pet.yml`）
- **AI Provider 切换**: 环境变量 `AI_PROVIDER` 可选 `grok`（默认）/ `openai` / `ark`（火山引擎 Seedance）
- Web3 收款地址配置在 `sys_blockchain_config.wallet_address`
- 开发环境跨域通过 Vite proxy 解决，生产需配 Nginx CORS
- 菜单权限标识要前后端一致
- API 测试脚本: `script/test-api.http` (VS Code REST Client)
- **pet-virtual Controller 取当前用户**: 使用 `VirtualSecurityUtils.getCurrentUserId()` / `getCurrentUser()`，不用 Spring Security 的 `SecurityContextHolder`
- **S3 文件 URL**: 使用 `fileService.getPresignedUrl(fileId, 7 * 24 * 60)` 生成 7 天有效期预签名 URL，禁止将永久公开 URL 存入数据库
- **后端 i18n**: 使用 `MessageUtils.message("key", args...)` 返回多语言错误信息，消息文件位于 `ruoyi-common/src/main/resources/i18n/messages*.properties`
- **OAuth 弹窗模式**: OAuth 回调页 (`/auth/callback`) 通过 `window.opener.postMessage` 将 token 传回父窗口，父窗口监听 `message` 事件完成登录
- **每日签到积分规则**: Day1-7 依次奖励 3/5/10/3/3/5/50 积分，连续签到断签后从 Day1 重置

## 文档参考

`docs/` 目录：

- [1-ark快速入门.md](docs/1-ark快速入门.md) - 火山引擎 Ark 快速入门
- [2-Seedance 模型.md](docs/2-Seedance%20模型.md) - Seedance 模型说明
- [3-火山引擎Ark视频生成集成指南.md](docs/3-火山引擎Ark视频生成集成指南.md) - 火山引擎集成指南
