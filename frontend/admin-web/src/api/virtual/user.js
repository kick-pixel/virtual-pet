import request from '@/utils/request'

// 查询平台用户列表
export function listVirtualUsers(query) {
  return request({
    url: '/admin/virtual/user/list',
    method: 'get',
    params: query
  })
}

// 禁止/恢复生成视频
export function updateGenDisabled(userId, genDisabled) {
  return request({
    url: '/admin/virtual/user/' + userId + '/gen-disabled',
    method: 'put',
    data: { genDisabled }
  })
}

// 修改账号状态
export function updateUserStatus(userId, status) {
  return request({
    url: '/admin/virtual/user/' + userId + '/status',
    method: 'put',
    data: { status }
  })
}
