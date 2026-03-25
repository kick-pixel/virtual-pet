import request from '@/utils/request'

// 查询区块链网络列表
export function listBlockchain(query) {
  return request({
    url: '/web3/blockchain/list',
    method: 'get',
    params: query
  })
}

// 查询区块链网络详细
export function getBlockchain(configId) {
  return request({
    url: '/web3/blockchain/' + configId,
    method: 'get'
  })
}

// 新增区块链网络
export function addBlockchain(data) {
  return request({
    url: '/web3/blockchain',
    method: 'post',
    data: data
  })
}

// 修改区块链网络
export function updateBlockchain(data) {
  return request({
    url: '/web3/blockchain',
    method: 'put',
    data: data
  })
}

// 删除区块链网络
export function delBlockchain(configId) {
  return request({
    url: '/web3/blockchain/' + configId,
    method: 'delete'
  })
}

// 测试RPC连接
export function testRpcConnection(configId) {
  return request({
    url: '/web3/blockchain/testConnection/' + configId,
    method: 'get'
  })
}

// 获取当前区块高度
export function getCurrentBlock(configId) {
  return request({
    url: '/web3/blockchain/block/' + configId,
    method: 'get'
  })
}
