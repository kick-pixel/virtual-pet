<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getTransactions } from '@/api/credits'
import { formatDate, formatCredits } from '@/utils/format'
import { ArrowUpRight, ArrowDownLeft } from 'lucide-vue-next'

const { t } = useI18n()
const transactions = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await getTransactions({ pageNum: 1, pageSize: 50 })
    transactions.value = res.data?.rows || res.rows || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>

<template>
  <div style="max-width: 1024px; margin: 0 auto; padding: 32px 16px;">
    <h1 style="font-size: 30px; font-weight: 700; margin-bottom: 32px;">{{ t('credits.historyTitle') }}</h1>

    <a-card>
      <template #title>
        <div style="font-size: 18px; font-weight: 500;">{{ t('credits.transactionHistory') || 'Transaction History' }}</div>
      </template>
      <div v-if="loading">
        <a-space direction="vertical" :size="16" style="width: 100%;">
          <a-skeleton v-for="i in 5" :key="i" :active="true" />
        </a-space>
      </div>
      <div v-else-if="transactions.length === 0" style="padding: 48px 0; text-align: center;">
        <div style="font-size: 48px; margin-bottom: 12px;">📭</div>
        <p style="color: #6b7280;">{{ t('common.noData') }}</p>
      </div>
      <div v-else>
        <a-space direction="vertical" :size="12" style="width: 100%;">
          <div
            v-for="tx in transactions"
            :key="tx.transactionId"
            style="display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; background: white; transition: background 0.2s;"
            class="tx-item"
          >
            <div style="display: flex; align-items: center; gap: 16px;">
              <div
                style="height: 40px; width: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center;"
                :style="{ background: tx.amount > 0 ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)', color: tx.amount > 0 ? '#22c55e' : '#ef4444' }"
              >
                <ArrowUpRight v-if="tx.amount > 0" style="height: 20px; width: 20px;" />
                <ArrowDownLeft v-else style="height: 20px; width: 20px;" />
              </div>
              <div>
                <p style="font-weight: 500; margin: 0 0 4px; text-transform: capitalize;">{{ tx.type || tx.transactionType }}</p>
                <p style="font-size: 12px; color: #6b7280; margin: 0;">{{ tx.remark || tx.description || '-' }}</p>
              </div>
            </div>
            <div style="text-align: right;">
              <p style="font-weight: 700; margin: 0 0 4px;" :style="{ color: tx.amount > 0 ? '#22c55e' : '#ef4444' }">
                {{ tx.amount > 0 ? '+' : '' }}{{ formatCredits(tx.amount) }}
              </p>
              <p style="font-size: 12px; color: #6b7280; margin: 0;">{{ formatDate(tx.createTime) }}</p>
            </div>
          </div>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.tx-item:hover {
  background: #f9fafb !important;
}
</style>
