import request from '@/utils/request'

// 查询视频列表（管理后台）
export function listAdminVideos(query) {
  return request({
    url: '/admin/virtual/video/list',
    method: 'get',
    params: query
  })
}

// 封禁/解封视频
export function updateVideoAdminStatus(taskId, adminStatus) {
  return request({
    url: '/admin/virtual/video/' + taskId + '/ban',
    method: 'put',
    data: { adminStatus }
  })
}
