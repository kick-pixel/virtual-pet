<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { setLocale } from '@/utils/storage'
import { Globe } from 'lucide-vue-next'

const { t, locale } = useI18n()

function toggleLocale() {
  const next = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = next
  setLocale(next)
}
</script>

<template>
  <div style="display: flex; min-height: 100vh; align-items: center; justify-content: center; padding: 32px; background: #fafafa;">
    <!-- Language Toggle (Top Right) -->
    <a-button
      @click="toggleLocale"
      type="text"
      :title="locale === 'zh-CN' ? 'English' : '中文'"
      style="position: fixed; top: 16px; right: 16px; z-index: 50;"
    >
      <template #icon>
        <Globe style="width: 16px; height: 16px;" />
      </template>
      {{ locale === 'zh-CN' ? 'EN' : '中文' }}
    </a-button>

    <div class="not-found-content" style="text-align: center;">
      <div style="font-size: 96px; margin-bottom: 24px;">🐾</div>
      <h1 class="gradient-text" style="font-size: 72px; font-weight: 800; margin: 0 0 16px;">404</h1>
      <p style="font-size: 18px; color: #6b7280; margin: 0 0 32px;">{{ t('common.pageNotFound') || 'Page not found' }}</p>
      <router-link to="/" style="text-decoration: none;">
        <a-button
          type="primary"
          size="large"
          style="border-radius: 12px; padding: 0 32px; height: 48px; background: linear-gradient(135deg, #7c3aed, #3b82f6); border: none; font-weight: 600; box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);"
        >
          {{ t('common.backToHome') || 'Back to Home' }}
        </a-button>
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.gradient-text {
  background: linear-gradient(135deg, #7c3aed, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.not-found-content {
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
