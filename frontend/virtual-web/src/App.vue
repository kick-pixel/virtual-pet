<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useCreditsStore } from '@/store/credits'
import { AUTH_EVENTS } from '@/utils/request'

const userStore = useUserStore()
const creditsStore = useCreditsStore()

function handleUnauthorized() {
    userStore.resetUser()
}

onMounted(() => {
    window.addEventListener(AUTH_EVENTS.UNAUTHORIZED, handleUnauthorized)
    // 页面刷新后 token 仍在，需主动恢复用户信息和积分余额
    if (userStore.isLoggedIn()) {
        userStore.fetchProfile()
        creditsStore.fetchBalance()
    }
})

onUnmounted(() => {
    window.removeEventListener(AUTH_EVENTS.UNAUTHORIZED, handleUnauthorized)
})
</script>

<template>
  <router-view />
</template>
