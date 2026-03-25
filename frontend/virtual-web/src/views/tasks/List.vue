<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { listTasks } from '@/api/task'
import { formatDate, formatCredits } from '@/utils/format'

import { Sparkles, Clock, Monitor, Diamond, ExternalLink, AlertCircle, Loader2, CheckCircle, XCircle } from 'lucide-vue-next'

const { t } = useI18n()
const router = useRouter()
const tasks = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const statusConfig: Record<string, { label: string; color: string; icon: any }> = {
  pending: { label: 'tasks.statusPending', color: 'default', icon: Clock },
  processing: { label: 'tasks.statusProcessing', color: 'processing', icon: Loader2 },
  completed: { label: 'tasks.statusCompleted', color: 'success', icon: CheckCircle },
  failed: { label: 'tasks.statusFailed', color: 'error', icon: XCircle },
  cancelled: { label: 'tasks.statusCancelled', color: 'warning', icon: XCircle }
}

async function fetchTasks() {
  loading.value = true
  try {
    const res: any = await listTasks({ pageNum: pageNum.value, pageSize: pageSize.value })
    tasks.value = res.data?.rows || res.rows || []
    total.value = res.data?.total || res.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function goToTask(task: any) {
  if (task.status === 'completed') router.push(`/generate/result/${task.taskId}`)
  else if (task.status === 'processing' || task.status === 'pending') router.push(`/generate/progress/${task.taskId}`)
}

onMounted(fetchTasks)
</script>

<template>
  <div style="max-width: 1536px; margin: 0 auto; padding: 32px 16px;">
    <div style="margin-bottom: 32px; display: flex; align-items: center; justify-content: space-between;">
      <div>
        <h1 style="font-size: 30px; font-weight: 700; margin: 0 0 4px;">{{ t('tasks.title') }}</h1>
        <p style="color: #6b7280; margin: 0;">Manage your generation tasks</p>
      </div>
      <a-button type="primary">
        <template #icon>
          <Sparkles style="height: 16px; width: 16px;" />
        </template>
        <router-link to="/generate/create" style="display: flex; align-items: center; gap: 8px; color: inherit; text-decoration: none;">
          {{ t('nav.generate') }}
        </router-link>
      </a-button>
    </div>

    <a-card>
      <div v-if="loading" style="padding: 32px;">
        <a-space direction="vertical" :size="16" style="width: 100%;">
          <a-skeleton v-for="i in 5" :key="i" :active="true" />
        </a-space>
      </div>
      <div v-else-if="tasks.length === 0" style="padding: 48px 0; text-align: center;">
        <div style="display: inline-flex; align-items: center; justify-content: center; height: 64px; width: 64px; border-radius: 50%; background: #f3f4f6; margin-bottom: 16px;">
          <AlertCircle style="height: 32px; width: 32px; color: #9ca3af;" />
        </div>
        <p style="color: #6b7280; margin: 0 0 16px;">{{ t('common.noData') }}</p>
        <a-button type="default">
          <template #icon>
            <Sparkles style="height: 16px; width: 16px;" />
          </template>
          <router-link to="/generate/create" style="display: flex; align-items: center; gap: 8px; color: inherit; text-decoration: none;">
            Create your first task
          </router-link>
        </a-button>
      </div>
      <div v-else style="overflow-x: auto;">
        <table style="width: 100%; font-size: 14px;">
          <thead>
            <tr style="border-bottom: 1px solid #e5e7eb; background: #f9fafb;">
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">ID</th>
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">{{ t('tasks.status') }}</th>
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">{{ t('tasks.duration') }}</th>
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">{{ t('tasks.cost') }}</th>
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">{{ t('tasks.createdAt') }}</th>
              <th style="padding: 12px 16px; text-align: left; font-weight: 500; color: #6b7280;">{{ t('tasks.actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="task in tasks"
              :key="task.taskId"
              class="task-row"
              style="border-bottom: 1px solid #e5e7eb; cursor: pointer; transition: background 0.2s;"
              @click="goToTask(task)"
            >
              <td style="padding: 16px; font-family: monospace; font-size: 12px; color: #6b7280;">{{ task.taskId }}</td>
              <td style="padding: 16px;">
                <a-tag :color="statusConfig[task.status]?.color">
                  <template #icon>
                    <component :is="statusConfig[task.status]?.icon" :class="task.status === 'processing' ? 'spinning-icon' : ''" style="height: 12px; width: 12px; margin-right: 4px;" />
                  </template>
                  {{ t(statusConfig[task.status]?.label || task.status) }}
                </a-tag>
              </td>
              <td style="padding: 16px; color: #6b7280;">
                <div style="display: flex; align-items: center; gap: 4px;">
                  <Clock style="height: 12px; width: 12px;" />
                  {{ task.duration }}s
                </div>
              </td>
              <td style="padding: 16px;">
                <a-tag color="default" style="font-weight: 500;">
                  <Diamond style="height: 12px; width: 12px; margin-right: 4px;" />
                  {{ formatCredits(task.creditsCost) }}
                </a-tag>
              </td>
              <td style="padding: 16px; color: #6b7280;">{{ formatDate(task.createTime) }}</td>
              <td style="padding: 16px;">
                <a-button
                  v-if="task.status === 'completed'"
                  type="text"
                  size="small"
                  style="color: #7c3aed;"
                >
                  <template #icon>
                    <ExternalLink style="height: 12px; width: 12px;" />
                  </template>
                  {{ t('tasks.viewResult') }}
                </a-button>
                <a-button
                  v-else-if="task.status === 'processing'"
                  type="text"
                  size="small"
                >
                  <template #icon>
                    <Loader2 style="height: 12px; width: 12px;" class="spinning-icon" />
                  </template>
                  {{ t('tasks.viewProgress') }}
                </a-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.task-row:last-child {
  border-bottom: none;
}

.task-row:hover {
  background: #f9fafb;
}

.spinning-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
