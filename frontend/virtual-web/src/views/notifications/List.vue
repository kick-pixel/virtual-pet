<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getNotifications, readAll } from '@/api/notification'
import { formatDate } from '@/utils/format'

import { Bell, CheckCheck, Circle } from 'lucide-vue-next'

const { t } = useI18n()
const notifications = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await getNotifications({ pageNum: 1, pageSize: 50 })
    notifications.value = res.data?.rows || res.rows || []
  } catch { /* ignore */ }
  finally { loading.value = false }
})

async function handleReadAll() {
  try {
    await readAll()
    notifications.value.forEach((n: any) => (n.isRead = true))
  } catch { /* ignore */ }
}
</script>

<template>
  <div style="max-width: 768px; margin: 0 auto; padding: 32px 16px;">
    <div style="margin-bottom: 32px; display: flex; align-items: center; justify-content: space-between;">
      <div>
        <h1 style="font-size: 30px; font-weight: 700; margin: 0 0 4px;">{{ t('notification.title') }}</h1>
        <p style="color: #6b7280; margin: 0;">Stay updated with your activity</p>
      </div>
      <a-button type="default" size="small" @click="handleReadAll">
        <template #icon>
          <CheckCheck style="height: 16px; width: 16px;" />
        </template>
        {{ t('notification.readAll') }}
      </a-button>
    </div>

    <div v-if="loading">
      <a-space direction="vertical" :size="12" style="width: 100%;">
        <a-card v-for="i in 5" :key="i">
          <div style="padding: 16px;">
            <a-skeleton :active="true" />
          </div>
        </a-card>
      </a-space>
    </div>

    <div v-else-if="notifications.length === 0" style="padding: 64px 0; text-align: center;">
      <div style="display: inline-flex; align-items: center; justify-content: center; height: 64px; width: 64px; border-radius: 50%; background: #f3f4f6; margin-bottom: 16px;">
        <Bell style="height: 32px; width: 32px; color: #9ca3af;" />
      </div>
      <p style="color: #6b7280;">{{ t('notification.noNotifications') }}</p>
    </div>

    <div v-else>
      <a-space direction="vertical" :size="12" style="width: 100%;">
        <a-card
          v-for="notif in notifications"
          :key="notif.notificationId"
          :style="{
            opacity: notif.isRead ? '0.7' : '1',
            borderColor: notif.isRead ? '#e5e7eb' : 'rgba(124, 58, 237, 0.3)',
            background: notif.isRead ? 'white' : 'rgba(124, 58, 237, 0.05)'
          }"
        >
          <div style="padding: 16px;">
            <div style="display: flex; align-items: flex-start; justify-content: space-between;">
              <h3 style="font-size: 14px; font-weight: 500; margin: 0;">{{ notif.title }}</h3>
              <Circle v-if="!notif.isRead" style="height: 8px; width: 8px; fill: #7c3aed; color: #7c3aed; flex-shrink: 0; margin-top: 4px;" />
            </div>
            <p v-if="notif.content" style="margin: 4px 0 0; font-size: 12px; color: #6b7280; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">{{ notif.content }}</p>
            <div style="margin-top: 8px; font-size: 12px; color: #6b7280;">{{ formatDate(notif.createTime) }}</div>
          </div>
        </a-card>
      </a-space>
    </div>
  </div>
</template>
