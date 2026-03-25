<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { bindEmail, sendEmailCode as sendVerifyCode } from '@/api/auth'
import { setLocale } from '@/utils/storage'
import { Globe, Mail, KeyRound } from 'lucide-vue-next'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()

function toggleLocale() {
  const next = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = next
  setLocale(next)
}

const form = ref({ email: '', verifyCode: '' })
const loading = ref(false)
const errorMsg = ref('')
const codeCountdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function handleSendCode() {
  if (!form.value.email) return
  try {
    await sendVerifyCode(form.value.email)
    codeCountdown.value = 60
    timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0 && timer) { clearInterval(timer); timer = null }
    }, 1000)
  } catch (e: any) {
    errorMsg.value = e.message
  }
}

async function handleBind() {
  if (!form.value.email || !form.value.verifyCode) return
  loading.value = true
  errorMsg.value = ''
  try {
    await bindEmail(form.value)
    userStore.emailVerified = true
    userStore.email = form.value.email
    router.push('/')
  } catch (e: any) {
    errorMsg.value = e.message || 'Bind failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 32px; background: #f9fafb;">
    <a-button
      type="text"
      @click="toggleLocale"
      style="position: fixed; top: 16px; right: 16px; z-index: 50;"
      :title="locale === 'zh-CN' ? 'English' : '中文'"
    >
      <template #icon>
        <Globe style="height: 20px; width: 20px;" />
      </template>
    </a-button>

    <a-card style="width: 100%; max-width: 448px;">
      <div style="text-align: center;">
        <div style="display: inline-flex; align-items: center; justify-content: center; height: 48px; width: 48px; border-radius: 50%; background: rgba(22, 119, 255, 0.1); margin-bottom: 16px;">
          <Mail style="height: 24px; width: 24px; color: #1677ff;" />
        </div>
        <h2 style="font-size: 20px; font-weight: 600; margin: 0 0 8px;">{{ t('auth.bindEmailTitle') }}</h2>
        <p style="font-size: 14px; color: #6b7280; margin: 0 0 24px;">{{ t('auth.bindEmailSubtitle') }}</p>
      </div>

      <a-alert v-if="errorMsg" type="error" show-icon style="margin-bottom: 24px;">
        {{ errorMsg }}
      </a-alert>

      <form @submit.prevent="handleBind">
        <a-space direction="vertical" :size="16" style="width: 100%;">
          <div>
            <label for="email" style="display: block; font-size: 14px; font-weight: 500; color: #374151; margin-bottom: 8px;">
              {{ t('auth.email') }}
            </label>
            <a-input
              id="email"
              v-model="form.email"
              type="email"
              required
              placeholder="you@example.com"
              size="large"
            >
              <template #prefix>
                <Mail style="height: 16px; width: 16px; color: #9ca3af;" />
              </template>
            </a-input>
          </div>

          <div>
            <label for="verifyCode" style="display: block; font-size: 14px; font-weight: 500; color: #374151; margin-bottom: 8px;">
              {{ t('auth.verifyCode') }}
            </label>
            <a-space style="width: 100%;" :size="12">
              <a-input
                id="verifyCode"
                v-model="form.verifyCode"
                type="text"
                required
                maxlength="6"
                placeholder="6-digit code"
                size="large"
                style="flex: 1;"
              >
                <template #prefix>
                  <KeyRound style="height: 16px; width: 16px; color: #9ca3af;" />
                </template>
              </a-input>
              <a-button
                type="default"
                size="large"
                @click="handleSendCode"
                :disabled="codeCountdown > 0"
                style="white-space: nowrap;"
              >
                {{ codeCountdown > 0 ? t('auth.codeSent', { seconds: codeCountdown }) : t('auth.sendCode') }}
              </a-button>
            </a-space>
          </div>

          <a-button type="primary" html-type="submit" size="large" block :loading="loading">
            {{ loading ? t('common.loading') : t('auth.bindBtn') }}
          </a-button>
        </a-space>
      </form>
    </a-card>
  </div>
</template>
