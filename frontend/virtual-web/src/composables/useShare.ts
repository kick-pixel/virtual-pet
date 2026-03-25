import { ref } from 'vue'
import { createShare } from '@/api/share'

export function useShare() {
    const isSupported = ref(!!navigator.share)
    const copied = ref(false)
    const sharing = ref(false)

    const shareData = async (title: string, text: string, url: string) => {
        if (navigator.share) {
            try {
                await navigator.share({ title, text, url })
                return true
            } catch (err) {
                console.error('Share failed:', err)
                return false
            }
        } else {
            return false
        }
    }

    const copyLink = async (url: string) => {
        try {
            await navigator.clipboard.writeText(url)
            copied.value = true
            setTimeout(() => copied.value = false, 2000)
            return true
        } catch (err) {
            console.error('Copy failed:', err)
            return false
        }
    }

    const shareToTwitter = (text: string, url: string) => {
        window.open(`https://x.com/intent/tweet?text=${encodeURIComponent(text)}&url=${encodeURIComponent(url)}`, '_blank')
    }

    const shareToTelegram = (text: string, url: string) => {
        window.open(`https://t.me/share/url?url=${encodeURIComponent(url)}&text=${encodeURIComponent(text)}`, '_blank')
    }

    /**
     * 分享并记录到后端
     * 先调后端创建分享记录，再打开第三方平台分享窗口
     */
    const shareAndRecord = async (taskId: string | number, platform: string, text: string, url: string) => {
        sharing.value = true
        try {
            // 1. 调后端记录分享
            await createShare(taskId, { platform })

            // 2. 打开对应平台的分享窗口
            if (platform === 'twitter') {
                shareToTwitter(text, url)
            } else if (platform === 'telegram') {
                shareToTelegram(text, url)
            }

            return true
        } catch (err) {
            console.error('Share record failed:', err)
            // 即使后端记录失败，仍然打开分享窗口（降级处理）
            if (platform === 'twitter') {
                shareToTwitter(text, url)
            } else if (platform === 'telegram') {
                shareToTelegram(text, url)
            }
            return false
        } finally {
            sharing.value = false
        }
    }

    return {
        isSupported,
        copied,
        sharing,
        shareData,
        copyLink,
        shareToTwitter,
        shareToTelegram,
        shareAndRecord
    }
}
