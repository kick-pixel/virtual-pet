<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { oauthCallback } from '@/api/auth'

const route = useRoute()
const status = ref<'processing' | 'success' | 'error'>('processing')
const errorMsg = ref('')

function closeWindow() { window.close() }

onMounted(async () => {
  const code = route.query.code as string
  const state = route.query.state as string
  const provider = (route.query.provider as string) || 'google'

  if (!code) {
    status.value = 'error'
    errorMsg.value = 'Authorization code not found'
    return
  }

  try {
    const res: any = await oauthCallback(provider, code, state)
    status.value = 'success'

    if (window.opener) {
      window.opener.postMessage(
        { type: 'oauth-callback', data: res.data },
        window.location.origin
      )
      setTimeout(() => window.close(), 500)
    } else {
      // 非弹窗模式：直接存 token 并跳转首页
      localStorage.setItem('virtual_token', res.data.token)
      window.location.href = '/'
    }
  } catch (e: any) {
    status.value = 'error'
    errorMsg.value = e?.response?.data?.msg || e.message || 'Login failed'
  }
})
</script>

<template>
  <div style="display: flex; align-items: center; justify-content: center; min-height: 100vh; background: #0a0a0a;">
    <div v-if="status === 'processing'" style="text-align: center;">
      <a-spin size="large" />
      <p style="margin-top: 16px; color: #9ca3af; font-size: 14px;">Processing login...</p>
    </div>
    <div v-else-if="status === 'success'" style="text-align: center;">
      <div style="font-size: 48px; margin-bottom: 16px;">✅</div>
      <p style="color: #4ade80; font-size: 16px;">Login successful! Closing window...</p>
    </div>
    <div v-else style="text-align: center;">
      <div style="font-size: 48px; margin-bottom: 16px;">❌</div>
      <p style="color: #f87171; font-size: 16px; margin-bottom: 16px;">{{ errorMsg }}</p>
      <a-button @click="closeWindow">Close</a-button>
    </div>
  </div>
</template>
