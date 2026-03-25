import request from '@/utils/request'

// 查询代币配置列表
export function listToken(query) {
  return request({
    url: '/web3/token/list',
    method: 'get',
    params: query
  })
}

// 查询代币配置详细
export function getToken(tokenId) {
  return request({
    url: '/web3/token/' + tokenId,
    method: 'get'
  })
}

// 新增代币配置
export function addToken(data) {
  return request({
    url: '/web3/token',
    method: 'post',
    data: data
  })
}

// 修改代币配置
export function updateToken(data) {
  return request({
    url: '/web3/token',
    method: 'put',
    data: data
  })
}

// 删除代币配置
export function delToken(tokenId) {
  return request({
    url: '/web3/token/' + tokenId,
    method: 'delete'
  })
}

// 根据网络ID获取代币列表
export function listTokenByNetwork(networkId) {
  return request({
    url: '/web3/token/network/' + networkId,
    method: 'get'
  })
}
