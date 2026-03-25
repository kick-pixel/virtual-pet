import request from '@/utils/request'

// 查询积分流水列表
export function listAdminCredits(query) {
  return request({
    url: '/admin/virtual/credits/list',
    method: 'get',
    params: query
  })
}
