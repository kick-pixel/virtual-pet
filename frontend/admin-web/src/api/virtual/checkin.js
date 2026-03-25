import request from '@/utils/request'

// 查询签到管理列表
export function listAdminCheckins(query) {
  return request({
    url: '/admin/virtual/checkin/list',
    method: 'get',
    params: query
  })
}
