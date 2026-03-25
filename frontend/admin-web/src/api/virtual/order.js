import request from '@/utils/request'

// 查询订单管理列表
export function listAdminOrders(query) {
  return request({
    url: '/admin/virtual/order/list',
    method: 'get',
    params: query
  })
}
