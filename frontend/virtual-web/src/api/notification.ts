import request from '@/utils/request'

/** 获取通知列表 */
export function getNotifications(params?: { pageNum?: number; pageSize?: number }) {
    return request.get('/api/virtual/notification/list', { params })
}

/** 获取未读数量 */
export function getUnreadCount() {
    return request.get('/api/virtual/notification/unread-count')
}

/** 标记已读 */
export function markRead(id: string | number) {
    return request.post(`/api/virtual/notification/${id}/read`)
}

/** 全部标记已读 */
export function readAll() {
    return request.post('/api/virtual/notification/read-all')
}
