import request from '@/utils/request'

/** 上传图片 */
export function uploadImage(formData: FormData) {
    return request.post('/api/virtual/task/upload-image', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/** 上传文件 */
export function uploadFile(formData: FormData) {
    return request.post('/api/virtual/file/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/** 获取文件预签名URL */
export function getFileUrl(fileId: string | number) {
    return request.get(`/api/virtual/file/${fileId}/url`)
}

/** 删除文件 */
export function deleteFile(fileId: string | number) {
    return request.delete(`/api/virtual/file/${fileId}`)
}
