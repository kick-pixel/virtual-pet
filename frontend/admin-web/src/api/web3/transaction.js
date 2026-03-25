import request from '@/utils/request'

// 查询交易记录列表
export function listTransaction(query) {
  return request({
    url: '/web3/transaction/list',
    method: 'get',
    params: query
  })
}

// 查询交易记录详细
export function getTransaction(transactionId) {
  return request({
    url: '/web3/transaction/' + transactionId,
    method: 'get'
  })
}

// 根据交易哈希查询
export function getTransactionByHash(txHash) {
  return request({
    url: '/web3/transaction/hash/' + txHash,
    method: 'get'
  })
}

// 手动触发区块链扫描
export function triggerScan() {
  return request({
    url: '/web3/scan/trigger',
    method: 'post'
  })
}

// 获取扫描状态
export function getScanStatus() {
  return request({
    url: '/web3/scan/status',
    method: 'get'
  })
}

// 更新交易状态
export function updateTransactionStatus(transactionId, status) {
  return request({
    url: '/web3/transaction/' + transactionId + '/status',
    method: 'put',
    data: { status: status }
  })
}

// 导出交易记录
export function exportTransaction(query) {
  return request({
    url: '/web3/transaction/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
