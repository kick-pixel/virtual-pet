import request from '@/utils/request'

/** 获取分享平台列表 */
export function getPlatforms() {
    return request.get('/api/virtual/share/platforms')
}

/** 创建分享记录（后端同时返回 shareUrl） */
export function createShare(taskId: string | number, data: { platform: string }) {
    return request.post(`/api/virtual/share/${taskId}`, data)
}

/** 获取分享记录 */
export function getShareRecords(params?: { pageNum?: number; pageSize?: number }) {
    return request.get('/api/virtual/share/records', { params })
}
