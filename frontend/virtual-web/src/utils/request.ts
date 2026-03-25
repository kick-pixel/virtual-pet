import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken, getLocale } from './storage'

export const AUTH_EVENTS = {
    UNAUTHORIZED: 'auth:unauthorized'
} as const

const service: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API as string,
    timeout: 30000,
    headers: { 'Content-Type': 'application/json' }
})

service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = getToken()
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        config.headers['Accept-Language'] = getLocale()
        return config
    },
    (error) => Promise.reject(error)
)

service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data
        if (res.code !== undefined && res.code !== 200) {
            if (res.code === 401) {
                removeToken()
                window.dispatchEvent(new CustomEvent(AUTH_EVENTS.UNAUTHORIZED))
                return Promise.reject(new Error('Login expired, please sign in again'))
            }
            // 统一透传后端 msg（业务错误如积分不足、系统错误均已由后端处理为用户可读文案）
            return Promise.reject(new Error(res.msg || 'Request failed'))
        }
        return res
    },
    (error) => {
        if (error.response?.status === 401) {
            removeToken()
            window.dispatchEvent(new CustomEvent(AUTH_EVENTS.UNAUTHORIZED))
        }
        // HTTP 5xx 不透传后端原始错误
        if (error.response?.status >= 500) {
            return Promise.reject(new Error('Server error, please try again later'))
        }
        const message = error.response?.data?.msg || error.message || 'Network request failed'
        return Promise.reject(new Error(message))
    }
)

export default service
