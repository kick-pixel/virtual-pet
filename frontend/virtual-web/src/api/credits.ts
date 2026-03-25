import request from '@/utils/request'

/** 获取积分余额 */
export function getBalance() {
    return request.get('/api/virtual/credits/balance')
}

/** 获取积分规则 */
export function getRules() {
    return request.get('/api/virtual/credits/rules')
}

/** 获取积分交易记录 */
export function getTransactions(params?: { pageNum?: number; pageSize?: number; type?: string }) {
    return request.get('/api/virtual/credits/transactions', { params })
}

/** 预估任务积分消耗 */
export function estimateCost(data: { duration: number; resolution: string; aspectRatio?: string }) {
    return request.get('/api/virtual/credits/estimate', { params: data })
}
