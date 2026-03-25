<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getTaskProgress, getTaskDetail } from '@/api/task'

import { Loader2, RefreshCw, List, AlertCircle } from 'lucide-vue-next'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const taskId = route.params.taskId as string
const task = ref<any>(null)
const progress = ref(0)
const status = ref('processing')
const errorMsg = ref('')
let pollTimer: ReturnType<typeof setInterval> | null = null

async function fetchProgress() {
  try {
    const res: any = await getTaskProgress(taskId)
    const data = res.data || res
    progress.value = data.progress ?? 0
    status.value = data.status || 'processing'

    if (status.value === 'completed') {
      stopPolling()
      router.push(`/generate/result/${taskId}`)
    } else if (status.value === 'failed') {
      stopPolling()
      errorMsg.value = data.errorMessage || 'Generation failed'
    }
  } catch {
    // retry silently
  }
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

onMounted(async () => {
  try {
    const res: any = await getTaskDetail(taskId)
    task.value = res.data || res
  } catch { /* ignore */ }
  fetchProgress()
  pollTimer = setInterval(fetchProgress, 3000)
})

onUnmounted(() => stopPolling())
</script>

<template>
  <div style="max-width: 672px; margin: 0 auto; padding: 64px 16px;">
    <div style="text-align: center;">
      <!-- Processing -->
      <div v-if="status === 'processing' || status === 'pending'">
        <div class="pulse-glow" style="display: inline-flex; align-items: center; justify-content: center; height: 96px; width: 96px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #ec4899); margin-bottom: 32px; box-shadow: 0 8px 32px rgba(124, 58, 237, 0.3);">
          <span style="font-size: 48px;">🐾</span>
        </div>
        <h2 style="font-size: 24px; font-weight: 700; margin: 0 0 8px;">{{ t('generate.progressTitle') }}</h2>
        <p style="font-size: 14px; color: #6b7280; margin: 0 0 32px;">{{ t('generate.progressHint') }}</p>

        <!-- Progress Bar -->
        <div style="max-width: 448px; margin: 0 auto;">
          <div style="display: flex; justify-content: space-between; font-size: 14px; color: #6b7280; margin-bottom: 8px;">
            <span>Progress</span>
            <span>{{ progress }}%</span>
          </div>
          <a-progress :percent="progress" :show-info="false" size="small" />
        </div>

        <!-- Task Info -->
        <a-card v-if="task" style="margin-top: 32px; max-width: 448px; margin-left: auto; margin-right: auto; text-align: left;">
          <template #title>
            <div style="font-size: 14px; font-weight: 500;">Task Details</div>
          </template>
          <a-space direction="vertical" :size="8" style="width: 100%;">
            <div style="display: flex; justify-content: space-between; font-size: 14px;">
              <span style="color: #6b7280;">Task ID</span>
              <span style="font-family: monospace; font-size: 12px;">{{ taskId }}</span>
            </div>
            <div v-if="task.duration" style="display: flex; justify-content: space-between; font-size: 14px;">
              <span style="color: #6b7280;">Duration</span>
              <span>{{ task.duration }}s</span>
            </div>
            <div v-if="task.resolution" style="display: flex; justify-content: space-between; font-size: 14px;">
              <span style="color: #6b7280;">Resolution</span>
              <span>{{ task.resolution }}</span>
            </div>
          </a-space>
        </a-card>
      </div>

      <!-- Failed -->
      <div v-else-if="status === 'failed'">
        <div style="display: inline-flex; align-items: center; justify-content: center; height: 80px; width: 80px; border-radius: 50%; background: rgba(239, 68, 68, 0.1); margin-bottom: 24px;">
          <AlertCircle style="height: 40px; width: 40px; color: #ef4444;" />
        </div>
        <h2 style="font-size: 24px; font-weight: 700; color: #ef4444; margin: 0 0 8px;">Generation Failed</h2>
        <p style="font-size: 14px; color: #6b7280; margin: 0 0 32px;">{{ errorMsg }}</p>
        <a-space :size="16">
          <a-button type="primary">
            <template #icon>
              <RefreshCw style="height: 16px; width: 16px;" />
            </template>
            <router-link to="/generate/create" style="display: flex; align-items: center; gap: 8px; color: inherit; text-decoration: none;">
              {{ t('common.retry') }}
            </router-link>
          </a-button>
          <a-button type="default">
            <template #icon>
              <List style="height: 16px; width: 16px;" />
            </template>
            <router-link to="/tasks" style="display: flex; align-items: center; gap: 8px; color: inherit; text-decoration: none;">
              {{ t('tasks.title') }}
            </router-link>
          </a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pulse-glow {
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 32px rgba(124, 58, 237, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12px 48px rgba(124, 58, 237, 0.5);
  }
}
</style>
