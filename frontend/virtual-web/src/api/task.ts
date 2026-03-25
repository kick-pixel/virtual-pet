import request from '@/utils/request'

/** 获取生成选项 */
export function getOptions() {
    return request.get('/api/virtual/task/options')
}

/** 获取生成选项（别名） */
export function getGenerationOptions() {
    return request.get('/api/virtual/task/options')
}

/** 上传宠物图片 */
export function uploadImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/virtual/task/upload-image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/** 创建生成任务 */
export function createTask(data: {
    prompt?: string
    promptText?: string
    imageUrl?: string
    fileId?: string | number
    requirements?: string
    duration?: number
    videoDuration?: number | null
    resolution?: string
    videoResolution?: string | null
    aspectRatio?: string
    videoAspectRatio?: string | null
    model?: string
}) {
    return request.post('/api/virtual/task/create', data)
}

/** 获取任务列表 */
export function listTasks(params?: { pageNum?: number; pageSize?: number; status?: string }) {
    return request.get('/api/virtual/task/list', { params })
}

/** 获取任务详情 */
export function getTaskDetail(taskId: string | number) {
    return request.get(`/api/virtual/task/${taskId}`)
}

/** 获取任务进度（轮询方式） */
export function getTaskProgress(taskId: string | number) {
    return request.get(`/api/virtual/task/${taskId}/progress`)
}

/** 取消任务 */
export function cancelTask(taskId: string | number) {
    return request.delete(`/api/virtual/task/${taskId}`)
}

/** 重试失败任务 */
export function retryTask(taskId: string | number) {
    return request.post(`/api/virtual/task/${taskId}/retry`)
}

/** 获取任务状态 */
export function getTaskStatus(taskId: string | number) {
    return request.get(`/api/virtual/task/${taskId}`)
}

/** 点赞/取消点赞（toggle） */
export function toggleTaskLike(taskId: string | number) {
    return request.post(`/api/virtual/task/${taskId}/like`)
}

/** 获取用户已点赞的 taskId 列表 */
export function getLikedTaskIds() {
    return request.get('/api/virtual/task/liked-ids')
}

/** 记录视频浏览量（fire-and-forget） */
export function recordView(taskId: string | number) {
    return request.post(`/api/virtual/task/${taskId}/view`)
}

/** 获取展示列表（已完成的视频） */
export function getShowcaseList(params?: { pageNum?: number; pageSize?: number }) {
    return request.get('/api/virtual/task/showcase', { params })
}
