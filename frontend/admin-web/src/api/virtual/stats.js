import request from '@/utils/request'

// 查询视频API统计数据
export function getApiStats(query) {
  return request({
    url: '/admin/virtual/stats/api',
    method: 'get',
    params: query
  })
}
