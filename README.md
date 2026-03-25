# 虚拟宠物视频平台 - Web3 版

基于 RuoYi-Vue 3.x 框架的虚拟宠物视频社交平台，集成 Web3 区块链支付功能。

## 项目简介

虚拟宠物视频平台是一个创新的社交娱乐平台，用户可以在这里创建、养育虚拟宠物，录制和分享宠物视频内容。平台集成了 Web3 区块链支付、文件管理、AI 视频生成等功能，支持以太坊、BSC、Polygon 等多条 EVM 兼容链的支付接入，为用户提供沉浸式的虚拟宠物养成体验。

### 核心功能

- 🐾 **虚拟宠物养成**：创建、养育个性化虚拟宠物
- 🎬 **视频录制分享**：录制宠物日常视频，分享到社区
- 🔗 **Web3 区块链支付**：支持多链多币种支付（ETH、BNB、USDT、USDC 等）
- 💼 **文件管理**：基于 S3 协议的文件存储（支持 Cloudflare R2、MinIO 等）
- 🤖 **AI 视频生成**：集成 AI 视频生成能力
- 🔐 **权限管理**：基于 RBAC 的细粒度权限控制
- 🌍 **国际化**：支持中英文切换

## 技术栈

### 后端

- Spring Boot 3.5.4
- MyBatis 3.5.16
- Spring Security
- Redis
- Web3j 4.9.8
- AWS SDK v2

### 前端

- Vue 3
- Element Plus
- Vite
- Pinia
- Wagmi + Viem (Web3 钱包连接)

### 区块链

- 以太坊 / BSC / Polygon
- Web3j
- ERC20 / BEP20 代币

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Maven 3.8+

### 2. 克隆项目

```bash
git clone git@github.com:kick-pixel/virtual-pet.git
cd pet-web3
```

### 3. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE pet_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据结构
mysql -u root -p pet_platform < pet-platform/sql/pet_platform.sql
```

### 4. 后端配置

编辑 `pet-platform/ruoyi-admin/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_platform?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

编辑 `application-pet.yml` 配置模块参数。

### 5. 启动后端

```bash
cd pet-platform
mvn clean install
cd ruoyi-admin
mvn spring-boot:run
```

访问：http://localhost:8181

### 6. 前端配置

```bash
cd pet-web
npm install
```

编辑 `.env.development`：

```env
VITE_APP_BASE_API = 'http://localhost:8181'
```

### 7. 启动前端

```bash
npm run dev
```

访问：http://localhost:80/admin

默认账号：

- 用户名：admin
- 密码：admin123

## 模块说明

### 文件管理模块 (pet-file)

基于 AWS S3 SDK 实现，支持任何 S3 兼容存储：

- Cloudflare R2
- AWS S3
- MinIO
- 阿里云 OSS（S3 兼容模式）

**配置示例**：

```yaml
pet:
  file:
    s3:
      access-key-id: your-access-key
      access-key-secret: your-secret-key
      endpoint: https://your-account.r2.cloudflarestorage.com
      bucket: pet-files
      public-domain: https://files.your-domain.com
```

**主要功能**：

- 文件上传/下载/删除
- 分片上传（支持大文件）
- 预签名 URL 生成
- 文件元数据管理

### AI 视频生成模块 (pet-ai)

集成 AI 视频生成 API。

**配置示例**：

```yaml
pet:
  ai:
    openai:
      api-key: sk-your-api-key
      base-url: https://api.openai.com/v1
      default-video-model: sora-1.0
```

**主要功能**：

- 文本/图片生成视频
- 任务状态监控
- 自动上传到 OSS
- 模型配置管理

### Web3 支付模块 (pay-web3)

支持多链多币种的区块链支付解决方案。

**支持的网络**：

- 以太坊（Mainnet / Sepolia）
- BSC（Mainnet / Testnet）
- Polygon（Mainnet / Mumbai）
- 其他 EVM 兼容链

**支持的代币**：

- 原生代币：ETH、BNB、MATIC
- ERC20/BEP20 代币：USDT、USDC、DAI 等

**主要功能**：

- 区块链网络配置管理
- 代币配置管理
- 自动交易扫描（每 15 秒）
- 交易确认追踪
- 支付事件触发
- 充值积分兑换

**配置示例**：

```yaml
pet:
  web3:
    scan-enabled: true
    scan-interval: 15000
    infura-project-id: your-infura-id
```

## 目录结构

```
pet-web3/
├── pet-platform/                    # 后端项目
│   ├── ruoyi-admin/               # 主应用模块
│   ├── ruoyi-system/              # 系统核心模块（Entity/Mapper/Service）
│   ├── ruoyi-framework/           # 框架核心
│   ├── ruoyi-quartz/              # 定时任务
│   ├── ruoyi-generator/           # 代码生成
│   ├── pet-virtual/               # 虚拟宠物管理模块 ⭐
│   │   ├── config/                # 模块配置
│   │   ├── domain/                # 领域对象
│   │   ├── service/               # 业务服务
│   │   ├── controller/            # REST API
│   │   └── task/                  # 定时任务
│   ├── pay-web3/                  # Web3 支付模块 ⭐
│   │   ├── config/                # Web3 配置
│   │   ├── domain/                # 领域对象
│   │   ├── service/               # 业务服务
│   │   ├── controller/            # REST API
│   │   ├── task/                  # 定时任务（交易扫描）
│   │   └── listener/              # 事件监听
│   ├── pet-file/                  # 文件管理模块 ⭐
│   │   ├── config/                # S3 配置
│   │   ├── domain/                # 领域对象
│   │   ├── service/               # 存储服务
│   │   └── controller/            # REST API
│   ├── ruoyi-ai/                  # AI 视频生成模块 ⭐
│   │   ├── config/                # OpenAI 配置
│   │   ├── domain/                # 领域对象
│   │   ├── service/               # 视频服务
│   │   ├── controller/            # REST API
│   │   └── task/                  # 定时任务
│   └── sql/                       # SQL 脚本
│       └── pet_platform.sql       # 数据库结构
│
├── pet-web/                       # 前端项目 (管理后台)
│   ├── src/
│   │   ├── api/                   # API 接口
│   │   │   └── web3/              # Web3 API ⭐
│   │   │       ├── blockchain.js
│   │   │       ├── token.js
│   │   │       └── transaction.js
│   │   ├── views/                 # 页面组件
│   │   │   ├── system/            # 系统管理
│   │   │   ├── pet/               # 虚拟宠物管理 ⭐
│   │   │   └── web3/              # Web3 管理 ⭐
│   │   │       ├── blockchain/    # 区块链网络
│   │   │       ├── token/         # 代币配置
│   │   │       └── transaction/   # 交易记录
│   │   ├── lang/                  # 国际化
│   │   │   ├── zh-CN.js          # 中文
│   │   │   └── en-US.js          # 英文
│   │   └── router/                # 路由配置
│   └── package.json
│
├── frontend/                      # 前端项目 (虚拟宠物用户端)
│   └── virtual-web/               # 虚拟宠物 Web 应用 ⭐
│       ├── src/
│       │   ├── composables/       # 组合式函数
│       │   │   └── useWallet.ts   # Web3 钱包连接
│       │   ├── views/             # 页面组件
│       │   └── ...
│       └── package.json
│
└── README.md                      # 本文件
```

## API 文档

### 区块链网络管理

```http
GET    /web3/blockchain/list        # 查询网络列表
GET    /web3/blockchain/{id}        # 查询网络详情
POST   /web3/blockchain             # 新增网络
PUT    /web3/blockchain             # 修改网络
DELETE /web3/blockchain/{id}        # 删除网络
POST   /web3/blockchain/test/{id}   # 测试 RPC 连接
GET    /web3/blockchain/block/{id}  # 获取当前区块高度
```

### 代币配置管理

```http
GET    /web3/token/list             # 查询代币列表
GET    /web3/token/{id}             # 查询代币详情
POST   /web3/token                  # 新增代币
PUT    /web3/token                  # 修改代币
DELETE /web3/token/{id}             # 删除代币
GET    /web3/token/network/{id}     # 按网络查询代币
```

### 交易记录管理

```http
GET    /web3/transaction/list       # 查询交易列表
GET    /web3/transaction/{id}       # 查询交易详情
GET    /web3/transaction/hash/{hash} # 按哈希查询
POST   /web3/scan/trigger           # 手动触发扫描
GET    /web3/scan/status            # 获取扫描状态
GET    /web3/transaction/export     # 导出交易记录
```

### 文件管理

```http
POST   /file/upload                 # 上传文件
GET    /file/download/{fileId}      # 下载文件
DELETE /file/delete/{fileId}        # 删除文件
GET    /file/list                   # 查询文件列表
GET    /file/url/{fileId}           # 获取文件 URL
```

### AI 视频生成

```http
POST   /ai/video/generate           # 生成视频任务
GET    /ai/video/task/{taskId}      # 查询任务状态
GET    /ai/video/list               # 查询任务列表
DELETE /ai/video/task/{taskId}      # 删除任务
GET    /ai/model/list               # 查询模型列表
```

## 配置指南

详细配置请参考：

- **Web3 模块**：参考 `application-pet.yml` 中的 `pet.web3` 配置
- **文件模块**：参考 `application-pet.yml` 中的 `pet.file` 配置
- **AI 模块**：参考 `application-pet.yml` 中的 `pet.ai` 配置

### Web3 支付使用流程

1. 在管理后台配置区块链网络（添加 RPC、钱包地址）
2. 配置代币（原生币或 ERC20）
3. 用户在前端连接 MetaMask 钱包
4. 选择充值套餐和支付代币
5. 系统自动扫描链上交易
6. 确认后自动到账积分

## 常用命令

### 后端

```bash
# 清理编译
mvn clean package

# 跳过测试
mvn clean package -Dmaven.test.skip=true

# 运行
cd ruoyi-admin
mvn spring-boot:run

# 打包
mvn clean package
java -jar target/ruoyi-admin.jar
```

### 前端

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产
npm run build:prod

# 预览构建
npm run preview
```

## 部署

### Docker 部署（推荐）

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 传统部署

1. 后端打包：`mvn clean package`
2. 前端构建：`npm run build:prod`
3. 上传到服务器
4. 配置 Nginx 反向代理
5. 启动服务

## 测试

### 单元测试

```bash
# 后端
mvn test

# 前端
npm run test
```

### Web3 测试流程

1. 在测试网（如 Sepolia）配置网络
2. 添加测试代币（测试网 USDT）
3. 使用测试钱包发送交易
4. 观察系统是否扫描到交易
5. 验证积分到账

获取测试币：

- Sepolia ETH: https://sepoliafaucet.com/
- BSC Testnet BNB: https://testnet.binance.org/faucet-smart
- Mumbai MATIC: https://faucet.polygon.technology/

## 安全建议

1. **配置安全**：
   - 不要提交 `application-pet.yml` 到版本控制
   - 使用环境变量管理敏感信息
   - 定期更换 API Key

2. **钱包安全**：
   - 使用冷钱包或多签钱包作为收款地址
   - 不要在代码中存储私钥
   - 定期转移资金到安全地址

3. **网络安全**：
   - 使用 HTTPS
   - 启用 CORS 白名单
   - 配置防火墙规则
   - 启用 Rate Limiting

4. **权限控制**：
   - 最小权限原则
   - 定期审计操作日志
   - 重要操作二次确认

## 性能优化

1. **数据库优化**：
   - 合理使用索引
   - 定期清理历史数据
   - 配置连接池

2. **缓存策略**：
   - Redis 缓存热点数据
   - 前端静态资源 CDN
   - API 响应缓存

3. **区块链优化**：
   - 使用可靠的 RPC 节点
   - 合理设置扫描间隔
   - 批量处理交易

4. **系统优化**：
   - 视频压缩转码优化
   - 图片懒加载
   - 数据库查询优化

## 故障排查

### 常见问题

**问题1：RPC 连接超时**

```bash
# 检查网络
curl https://mainnet.infura.io/v3/YOUR_ID

# 检查配置
grep rpcUrl application-pet.yml
```

**问题2：交易扫描不工作**

```bash
# 检查日志
tail -f logs/pay-web3.log

# 检查配置
grep scan-enabled application-pet.yml
```

**问题3：数据库连接失败**

```bash
# 检查 MySQL
mysql -u root -p

# 检查配置
grep datasource application.yml
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## 版本历史

### v1.0.0 (2026-02-07)

- ✅ 初始版本发布
- ✅ 虚拟宠物管理模块
- ✅ 视频录制分享功能
- ✅ Web3 区块链支付模块
- ✅ 文件管理模块
- ✅ AI 视频生成模块
- ✅ 区块链网络管理
- ✅ 代币配置管理
- ✅ 交易自动扫描
- ✅ 权限管理系统
- ✅ 中英文国际化

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 致谢

感谢以下开源项目：

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)
- [Vue 3](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Web3j](https://github.com/web3j/web3j)
- [Wagmi](https://wagmi.sh/)

---

**⭐ 如果这个项目对你有帮助，请给个 Star！**
