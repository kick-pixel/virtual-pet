import request from '@/utils/request'

/** 获取平台收款钱包地址 */
export function getWalletAddress(network?: string) {
    return request.get('/api/virtual/payment/wallet-address', { params: { network } })
}

/** 获取支持的网络 */
export function getNetworks() {
    return request.get('/api/virtual/payment/networks')
}

/** 获取支持的代币 */
export function getTokens(params?: { networkType?: string }) {
    return request.get('/api/virtual/payment/tokens', { params })
}

/** 获取充值套餐 */
export function getPackages() {
    return request.get('/api/virtual/payment/packages')
}

/** 创建充值订单 */
export function createOrder(data: {
    packageId: number
    chainId: number      // 钱包当前链 ID，后端据此查网络配置
    payToken: string     // 'BNB' | 'USDT' | 'ETH' | 'USDC'
    walletAddress: string
}) {
    return request.post('/api/virtual/payment/order', data)
}

/** 获取订单列表 */
export function listOrders(params?: { pageNum?: number; pageSize?: number; status?: string }) {
    return request.get('/api/virtual/payment/orders', { params })
}

/** 查询订单状态 */
export function getOrder(orderNo: string) {
    return request.get(`/api/virtual/payment/order/${orderNo}`)
}

/** 取消订单 */
export function cancelOrder(orderNo: string) {
    return request.put(`/api/virtual/payment/order/${orderNo}/cancel`)
}

/** 提交交易哈希 */
export function submitTxHash(orderNo: string, txHash: string) {
    return request.post(`/api/virtual/payment/order/${orderNo}/tx-hash`, { txHash })
}

/** 创建签到支付订单（无套餐，固定小额费用产生链上数据） */
export function createCheckinOrder(data: {
    chainId: number
    payToken: string      // 'BNB' | 'USDT'
    walletAddress: string
}) {
    return request.post('/api/virtual/payment/checkin-order', data)
}

/** 获取代币汇率列表 */
export function getExchangeRates(params?: { tokenSymbol?: string }) {
    return request.get('/api/virtual/payment/exchange-rates', { params })
}

/** 获取支付交易记录 */
export function getPaymentTransactions(params?: { pageNum?: number; pageSize?: number }) {
    return request.get('/api/virtual/payment/transactions', { params })
}
