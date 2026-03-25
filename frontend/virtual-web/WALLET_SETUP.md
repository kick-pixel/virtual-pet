# 钱包连接设置指南

## 问题修复

### ✅ 修复 1: 按钮点击无响应
- 为按钮添加了 `@click` 事件处理
- 实现了 `useWallet.openModal()` 函数
- 添加了钱包连接成功后的处理逻辑

### ✅ 修复 2: 登录弹窗遮挡钱包组件（z-index 层级问题）
- 在 [`globals.css`](src/assets/styles/globals.css) 中设置 Web3Modal z-index 为 9999
- 在 [`Header.vue`](src/components/layout/Header.vue) 中优化连接流程：
  - 点击 "Connect Wallet" 时先关闭登录弹窗
  - 等待 200ms 动画完成后再打开钱包连接
  - 避免 Modal 层级冲突

## 配置步骤

### 1. 获取 WalletConnect Project ID

1. 访问 [WalletConnect Cloud](https://cloud.walletconnect.com/)
2. 使用 GitHub 或 Google 账号登录
3. 点击 "Create New Project"
4. 填写项目信息：
   - **Project Name**: Pet AI
   - **Description**: AI Pet Video Generator
   - **Website URL**: http://localhost:3000 (开发环境)
5. 创建成功后，复制 **Project ID**

### 2. 配置环境变量

编辑 `frontend/virtual-web/.env.development` 文件：

```env
VITE_WC_PROJECT_ID=你的_PROJECT_ID
```

### 3. 安装 MetaMask 钱包

如果尚未安装 MetaMask：

1. 访问 [MetaMask 官网](https://metamask.io/)
2. 下载并安装浏览器扩展
3. 创建钱包或导入现有钱包
4. 确保浏览器允许 MetaMask 扩展运行

### 4. 重启开发服务器

```bash
cd frontend/virtual-web

# 停止当前服务 (Ctrl+C)
# 重新启动
npm run dev
```

## 测试连接

1. 访问 http://localhost:3000/showcase
2. 点击右上角 "登录" 按钮
3. 在弹窗中点击 "Connect Wallet"
4. 应该会弹出以下任一选项：
   - Web3Modal 弹窗（如果配置了 Project ID）
   - MetaMask 连接请求（Fallback 模式）

## 支持的钱包

- 🦊 **MetaMask** (推荐)
- 💳 **Coinbase Wallet**
- 🔗 **WalletConnect** (支持 200+ 钱包)

## 支持的网络

- Ethereum Mainnet (Chain ID: 1)
- Sepolia Testnet (Chain ID: 11155111) - 推荐测试使用
- BSC Mainnet (Chain ID: 56)
- BSC Testnet (Chain ID: 97)

## 常见问题

### Q: 点击按钮没有反应？

**A:** 检查以下项目：
1. MetaMask 扩展是否已安装并启用
2. 浏览器控制台是否有错误信息
3. 是否配置了 `VITE_WC_PROJECT_ID`
4. 开发服务器是否已重启

### Q: MetaMask 提示 "User rejected request"？

**A:** 这是正常的，用户拒绝了连接请求。请在 MetaMask 弹窗中点击"连接"。

### Q: 如何在测试网获取测试币？

**A:** Sepolia 测试网水龙头：
- https://sepoliafaucet.com/
- https://www.alchemy.com/faucets/ethereum-sepolia

### Q: 如何切换网络？

**A:** 在 MetaMask 扩展中：
1. 点击顶部网络名称
2. 选择目标网络
3. 如果没有显示，点击"添加网络"手动添加

## 开发者说明

### 钱包连接流程

```typescript
// 1. 用户点击 "Connect Wallet"
handleConnectWallet() {
  openModal() // 打开 Web3Modal 或直接连接 MetaMask
}

// 2. 钱包连接成功
if (isConnected.value && address.value) {
  console.log('Wallet connected:', address.value)
  // TODO: 调用后端 API 进行钱包登录/注册
}
```

### 待实现功能

- [ ] 钱包连接后自动调用后端登录 API
- [ ] 签名验证（防止钓鱼攻击）
- [ ] 钱包地址与邮箱绑定
- [ ] 多钱包账户切换
- [ ] 网络切换提示

## 相关文件

- [`useWallet.ts`](src/composables/useWallet.ts) - 钱包连接逻辑
- [`Header.vue`](src/components/layout/Header.vue) - 登录弹窗
- [`.env.development`](.env.development) - 环境配置
