<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getRules } from '@/api/credits'
import { Diamond, Gift, Calendar, Share2, RefreshCw } from 'lucide-vue-next'

const { t } = useI18n()
const rules = ref<any[]>([])

onMounted(async () => {
  try {
    const res: any = await getRules()
    rules.value = res.data || res.rows || []
  } catch { /* ignore */ }
})
</script>

<template>
  <div style="max-width: 896px; margin: 0 auto; padding: 32px 16px;">
    <h1 style="font-size: 30px; font-weight: 700; margin-bottom: 32px;">{{ t('credits.rulesTitle') }}</h1>

    <a-card style="margin-bottom: 32px;">
      <template #title>
        <div style="font-size: 18px; font-weight: 500;">Credit Pricing</div>
      </template>
      <a-space direction="vertical" :size="12" style="width: 100%;">
        <div v-if="rules.length > 0">
          <a-space direction="vertical" :size="12" style="width: 100%;">
            <div
              v-for="rule in rules"
              :key="rule.ruleId"
              style="display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; background: #f9fafb;"
            >
              <div>
                <p style="font-weight: 500; margin: 0 0 4px;">{{ rule.ruleName || rule.action }}</p>
                <p style="font-size: 14px; color: #6b7280; margin: 0;">{{ rule.description || rule.remark || '-' }}</p>
              </div>
              <a-tag color="default" style="font-size: 15px; padding: 4px 12px;">
                {{ rule.credits }} credits
              </a-tag>
            </div>
          </a-space>
        </div>
        <div v-else>
          <a-space direction="vertical" :size="12" style="width: 100%;">
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <Diamond style="height: 20px; width: 20px; color: #7c3aed;" />
                <span>5s video / 720p</span>
              </div>
              <a-tag color="default" style="font-size: 15px; padding: 4px 12px;">50 credits</a-tag>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <Diamond style="height: 20px; width: 20px; color: #7c3aed;" />
                <span>10s video / 1080p</span>
              </div>
              <a-tag color="default" style="font-size: 15px; padding: 4px 12px;">200 credits</a-tag>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; background: #f9fafb;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <Diamond style="height: 20px; width: 20px; color: #7c3aed;" />
                <span>15s video / 1080p</span>
              </div>
              <a-tag color="default" style="font-size: 15px; padding: 4px 12px;">400 credits</a-tag>
            </div>
          </a-space>
        </div>
      </a-space>
    </a-card>

    <a-card style="background: rgba(124, 58, 237, 0.05); border-color: rgba(124, 58, 237, 0.2);">
      <template #title>
        <div style="font-size: 18px; font-weight: 500; display: flex; align-items: center; gap: 8px;">
          <Gift style="height: 20px; width: 20px; color: #7c3aed;" />
          How Credits Work
        </div>
      </template>
      <a-space direction="vertical" :size="12" style="width: 100%;">
        <div style="display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #6b7280;">
          <Gift style="height: 16px; width: 16px; color: #7c3aed; margin-top: 2px; flex-shrink: 0;" />
          <span>New users receive <strong style="color: #7c3aed;">500 free credits</strong> upon registration</span>
        </div>
        <div style="display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #6b7280;">
          <Calendar style="height: 16px; width: 16px; color: #7c3aed; margin-top: 2px; flex-shrink: 0;" />
          <span>Daily login reward: <strong style="color: #7c3aed;">10 credits</strong></span>
        </div>
        <div style="display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #6b7280;">
          <Share2 style="height: 16px; width: 16px; color: #7c3aed; margin-top: 2px; flex-shrink: 0;" />
          <span>Share videos on social media to earn <strong style="color: #7c3aed;">50 credits</strong> per share</span>
        </div>
        <div style="display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #6b7280;">
          <RefreshCw style="height: 16px; width: 16px; color: #7c3aed; margin-top: 2px; flex-shrink: 0;" />
          <span>Credits are frozen when a task is created and consumed upon completion</span>
        </div>
        <div style="display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: #6b7280;">
          <RefreshCw style="height: 16px; width: 16px; color: #7c3aed; margin-top: 2px; flex-shrink: 0;" />
          <span>Failed tasks automatically refund all frozen credits</span>
        </div>
      </a-space>
    </a-card>
  </div>
</template>
