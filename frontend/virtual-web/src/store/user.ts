import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProfile, logout as apiLogout } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
    const token = ref<string | null>(getToken())
    const userId = ref<number | null>(null)
    const username = ref('')
    const nickname = ref('')
    const email = ref('')
    const emailVerified = ref(false)
    const avatar = ref('')
    const walletAddress = ref('')
    const loginType = ref('')
    const loginModalVisible = ref(false)

    const isLoggedIn = () => !!token.value

    async function fetchProfile() {
        try {
            const res: any = await getProfile()
            const data = res.data || res
            userId.value = data.userId
            username.value = data.username || ''
            nickname.value = data.nickname || ''
            email.value = data.email || ''
            emailVerified.value = data.emailVerified ?? false
            avatar.value = data.avatarUrl || data.avatar || ''
            walletAddress.value = data.walletAddress || (data.walletBindings?.[0]?.walletAddress) || ''
            loginType.value = data.loginType || ''
        } catch {
            resetUser()
        }
    }

    function setUserToken(newToken: string) {
        token.value = newToken
        setToken(newToken)
    }

    function resetUser() {
        token.value = null
        userId.value = null
        username.value = ''
        nickname.value = ''
        email.value = ''
        emailVerified.value = false
        avatar.value = ''
        walletAddress.value = ''
        loginType.value = ''
        removeToken()
    }

    async function logoutUser() {
        try {
            await apiLogout()
        } finally {
            resetUser()
        }
    }

    function showLoginModal() {
        loginModalVisible.value = true
    }

    function hideLoginModal() {
        loginModalVisible.value = false
    }

    return {
        token,
        userId,
        username,
        nickname,
        email,
        emailVerified,
        avatar,
        walletAddress,
        loginType,
        loginModalVisible,
        isLoggedIn,
        fetchProfile,
        setUserToken,
        resetUser,
        logoutUser,
        showLoginModal,
        hideLoginModal
    }
})
