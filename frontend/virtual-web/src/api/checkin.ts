import request from '@/utils/request'

/** 获取签到状态（含7天展示数据） */
export function getCheckinStatus() {
    return request.get('/api/virtual/checkin/status')
}

/** 领取今日签到积分 */
export function claimCheckin() {
    return request.post('/api/virtual/checkin/claim')
}

/** 获取签到所需最小支付金额（bnb / usdt） */
export function getCheckinFee() {
    return request.get('/api/virtual/checkin/fee')
}
