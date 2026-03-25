import request from '@/utils/request'

/** 获取 OAuth 授权 URL */
export function getOAuthUrl(provider: string, redirectUri?: string) {
    return request.get(`/api/virtual/auth/oauth/${provider}/url`, { params: { redirectUri } })
}

/** OAuth 回调处理 */
export function oauthCallback(provider: string, code: string, state?: string) {
    return request.post(`/api/virtual/auth/oauth/${provider}/callback`, { code, state })
}

/** 发送邮箱验证码 */
export function sendEmailCode(email: string) {
    return request.post('/api/virtual/auth/email/send-code', { email })
}

/** 邮箱验证码登录（新用户自动注册） */
export function emailLogin(data: { email: string; code: string }) {
    return request.post('/api/virtual/auth/email/login', data)
}

/** 获取钱包登录 nonce */
export function getWalletNonce(address: string) {
    return request.get('/api/virtual/auth/wallet/nonce', { params: { address } })
}

/** 钱包签名登录 */
export function walletLogin(data: { address: string; signature: string; walletType?: string; chainId?: number }) {
    return request.post('/api/virtual/auth/wallet/login', null, { params: data })
}

/** 绑定邮箱 */
export function bindEmail(data: { email: string; verifyCode: string }) {
    return request.post('/api/virtual/auth/bind/email', data)
}

/** 绑定钱包 */
export function bindWallet(data: { address: string; signature: string; message: string }) {
    return request.post('/api/virtual/auth/bind/wallet', data)
}

/** 获取当前用户信息 */
export function getProfile() {
    return request.get('/api/virtual/auth/profile')
}

/** 更新用户昵称 */
export function updateProfile(data: { nickname?: string }) {
    return request.put('/api/virtual/auth/profile', data)
}

/** 上传头像 */
export function uploadAvatar(formData: FormData) {
    return request.post('/api/virtual/auth/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/** 刷新 Token */
export function refreshToken() {
    return request.post('/api/virtual/auth/refresh')
}

/** 登出 */
export function logout() {
    return request.post('/api/virtual/auth/logout')
}

/** 获取开屏动画视频URL */
export function getSplashVideo() {
    return request.get('/api/virtual/config/splash')
}
