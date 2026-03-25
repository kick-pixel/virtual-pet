<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCreditsStore } from '@/store/credits'
import { formatCredits } from '@/utils/format'
import { Diamond, Snowflake, TrendingDown, Wallet, FileText, ArrowRight } from 'lucide-vue-next'

const { t } = useI18n()
const creditsStore = useCreditsStore()

onMounted(() => creditsStore.fetchBalance())
</script>

<template>
  <div style="max-width: 896px; margin: 0 auto; padding: 32px 16px;">
    <h1 style="font-size: 30px; font-weight: 700; margin-bottom: 32px;">{{ t('credits.balanceTitle') }}</h1>

    <a-row :gutter="[24, 24]" style="margin-bottom: 32px;">
      <a-col :xs="24" :sm="8">
        <a-card style="position: relative; overflow: hidden;">
          <div style="position: absolute; top: 0; right: 0; width: 128px; height: 128px; background: rgba(124, 58, 237, 0.1); border-radius: 50%; transform: translate(50%, -50%);"></div>
          <template #title>
            <a-space :size="8" style="font-size: 14px; font-weight: 500; color: #6b7280;">
              <Diamond style="height: 16px; width: 16px; color: #7c3aed;" />
              {{ t('credits.balance') }}
            </a-space>
          </template>
          <div class="gradient-text" style="font-size: 30px; font-weight: 800; margin-bottom: 8px;">{{ formatCredits(creditsStore.balance) }}</div>
          <a-button type="text" style="padding: 0;">
            <router-link to="/credits/recharge" style="display: flex; align-items: center; gap: 4px; color: #7c3aed; text-decoration: none;">
              <Wallet style="height: 16px; width: 16px;" />
              {{ t('nav.recharge') }}
              <ArrowRight style="height: 12px; width: 12px;" />
            </router-link>
          </a-button>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="8">
        <a-card style="position: relative; overflow: hidden;">
          <div style="position: absolute; top: 0; right: 0; width: 128px; height: 128px; background: rgba(234, 179, 8, 0.1); border-radius: 50%; transform: translate(50%, -50%);"></div>
          <template #title>
            <a-space :size="8" style="font-size: 14px; font-weight: 500; color: #6b7280;">
              <Snowflake style="height: 16px; width: 16px; color: #eab308;" />
              {{ t('credits.frozen') }}
            </a-space>
          </template>
          <div style="font-size: 30px; font-weight: 800; color: #ca8a04; margin-bottom: 8px;">{{ formatCredits(creditsStore.frozen) }}</div>
          <p style="font-size: 12px; color: #6b7280; margin: 0;">Reserved for active tasks</p>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="8">
        <a-card style="position: relative; overflow: hidden;">
          <div style="position: absolute; top: 0; right: 0; width: 128px; height: 128px; background: rgba(239, 68, 68, 0.1); border-radius: 50%; transform: translate(50%, -50%);"></div>
          <template #title>
            <a-space :size="8" style="font-size: 14px; font-weight: 500; color: #6b7280;">
              <TrendingDown style="height: 16px; width: 16px; color: #ef4444;" />
              {{ t('credits.totalSpent') }}
            </a-space>
          </template>
          <div style="font-size: 30px; font-weight: 800; margin-bottom: 8px;">{{ formatCredits(creditsStore.totalSpent) }}</div>
          <a-button type="text" style="padding: 0;">
            <router-link to="/credits/history" style="display: flex; align-items: center; gap: 4px; color: #7c3aed; text-decoration: none;">
              <FileText style="height: 16px; width: 16px;" />
              {{ t('credits.historyTitle') }}
              <ArrowRight style="height: 12px; width: 12px;" />
            </router-link>
          </a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12">
        <router-link
          to="/credits/recharge"
          class="quick-link"
          style="display: flex; align-items: center; gap: 16px; padding: 20px; border-radius: 12px; border: 1px solid #e5e7eb; background: white; text-decoration: none; transition: all 0.2s;"
        >
          <div class="icon-wrapper" style="display: flex; height: 48px; width: 48px; align-items: center; justify-content: center; border-radius: 12px; background: rgba(124, 58, 237, 0.1); color: #7c3aed; transition: all 0.3s;">
            <Diamond style="height: 24px; width: 24px;" />
          </div>
          <div style="flex: 1;">
            <div style="font-weight: 600; color: #111827;">{{ t('credits.rechargeTitle') }}</div>
            <div style="font-size: 12px; color: #6b7280;">Purchase credits with crypto</div>
          </div>
          <ArrowRight style="height: 20px; width: 20px; color: #6b7280;" class="arrow" />
        </router-link>
      </a-col>
      <a-col :xs="24" :sm="12">
        <router-link
          to="/credits/rules"
          class="quick-link"
          style="display: flex; align-items: center; gap: 16px; padding: 20px; border-radius: 12px; border: 1px solid #e5e7eb; background: white; text-decoration: none; transition: all 0.2s;"
        >
          <div class="icon-wrapper" style="display: flex; height: 48px; width: 48px; align-items: center; justify-content: center; border-radius: 12px; background: #f3f4f6; color: #6b7280; transition: all 0.3s;">
            <FileText style="height: 24px; width: 24px;" />
          </div>
          <div style="flex: 1;">
            <div style="font-weight: 600; color: #111827;">{{ t('credits.rulesTitle') }}</div>
            <div style="font-size: 12px; color: #6b7280;">View credit pricing details</div>
          </div>
          <ArrowRight style="height: 20px; width: 20px; color: #6b7280;" class="arrow" />
        </router-link>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.gradient-text {
  background: linear-gradient(135deg, #7c3aed, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.quick-link:hover {
  background: #f9fafb !important;
  border-color: rgba(124, 58, 237, 0.3) !important;
}

.quick-link:hover .icon-wrapper {
  background: #7c3aed !important;
  color: white !important;
}

.quick-link:hover .arrow {
  color: #7c3aed !important;
}
</style>
