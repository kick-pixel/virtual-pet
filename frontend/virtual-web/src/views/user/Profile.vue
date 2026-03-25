<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCreditsStore } from '@/store/credits'
import { getTransactions } from '@/api/credits'
import { listTasks, getLikedTaskIds, toggleTaskLike, recordView } from '@/api/task'
import { updateProfile, uploadAvatar } from '@/api/auth'
import { formatDate, shortenAddress } from '@/utils/format'
import { message } from 'ant-design-vue'
import RechargeModal from '@/components/RechargeModal.vue'
import VideoPlayModal from '@/components/VideoPlayModal.vue'
import {
  User,
  Wallet,
  Clock,
  Loader2,
  CheckCircle,
  XCircle,
  Sparkles,
  Camera,
  Pencil,
  Check,
  X as XIcon,
  Play,
  Heart
} from 'lucide-vue-next'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const creditsStore = useCreditsStore()

// Recharge Modal
const showRechargeModal = ref(false)

// Transaction Modal
const showTxModal = ref(false)

// Video Play Modal
const showVideoModal = ref(false)
const currentVideoItem = ref<any>(null)

// Avatar & Nickname editing
const avatarFileInput = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)
const editingNickname = ref(false)
const nicknameInput = ref('')
const updatingNickname = ref(false)

// Transactions
const transactions = ref<any[]>([])
const txLoading = ref(false)
const txPageNum = ref(1)
const txPageSize = ref(10)
const txTotal = ref(0)

// Video Tasks (masonry)
const videoTasks = ref<any[]>([])
const videoLoading = ref(false)
const videoLoadingMore = ref(false)
const videoPageNum = ref(1)
const videoPageSize = ref(12)
const videoTotal = ref(0)
const hasMoreVideos = ref(true)
const sentinelRef = ref<HTMLElement | null>(null)
let videoObserver: IntersectionObserver | null = null

// Like state
const likedTaskIds = ref<number[]>([])
const togglingLikeSet = new Set<number>()

// Masonry
const columnCount = ref(3)

function updateColumnCount() {
  const width = window.innerWidth
  if (width <= 640) columnCount.value = 1
  else if (width <= 1000) columnCount.value = 2
  else columnCount.value = 3
}

const videoColumns = computed(() => {
  const cols: any[][] = Array.from({ length: columnCount.value }, () => [])
  videoTasks.value.forEach((item, i) => {
    cols[i % columnCount.value]!.push(item)
  })
  return cols
})

function getCardStyle(aspectRatioStr: string | undefined) {
  if (!aspectRatioStr) return { aspectRatio: '3/4', width: '100%', overflow: 'hidden' }
  const parts = aspectRatioStr.split(':')
  if (parts.length !== 2) return { aspectRatio: '3/4', width: '100%', overflow: 'hidden' }
  return { aspectRatio: `${parts[0]}/${parts[1]}`, width: '100%', overflow: 'hidden' }
}

const statusConfig: Record<string, { label: string; color: string; icon: any }> = {
  pending: { label: 'tasks.statusPending', color: 'default', icon: Clock },
  processing: { label: 'tasks.statusProcessing', color: 'processing', icon: Loader2 },
  completed: { label: 'tasks.statusCompleted', color: 'success', icon: CheckCircle },
  failed: { label: 'tasks.statusFailed', color: 'error', icon: XCircle },
  cancelled: { label: 'tasks.statusCancelled', color: 'warning', icon: XCircle }
}

const txColumns = computed(() => [
  {
    title: t('credits.type'),
    dataIndex: 'txType',
    key: 'txType',
    customRender: ({ record }: any) => {
      // Prioritize translated txType, fallback to raw txType or '—'
      let text = '—'
      if (record.txType) {
        text = t(`credits.txTypes.${record.txType}`)
        if (text === `credits.txTypes.${record.txType}`) {
           // If translation key is missing, just capitalize it
           text = record.txType.charAt(0).toUpperCase() + record.txType.slice(1)
        }
      }
      return h('div', {
        style: {
          color: 'rgba(255, 255, 255, 0.85)',
          fontWeight: '500'
        }
      }, text)
    }
  },
  {
    title: t('credits.time'),
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
    customRender: ({ text }: any) => {
       return h('div', { style: { color: 'rgba(255, 255, 255, 0.6)', fontSize: '13px', whiteSpace: 'nowrap' } }, formatDate(text))
    }
  },
  {
    title: t('credits.amount'),
    dataIndex: 'amount',
    key: 'amount',
    width: 120,
    align: 'right'
  }
])

const displayName = computed(() =>
  userStore.nickname || userStore.username || userStore.email || 'User'
)

const userInitial = computed(() => displayName.value.charAt(0).toUpperCase())

onMounted(async () => {
  creditsStore.fetchBalance()
  updateColumnCount()
  window.addEventListener('resize', updateColumnCount)
  await Promise.all([loadTransactions(), loadVideoTasks(), loadLikedIds()])
  // 首页加载完成后，若存在下一页则自动预取一次
  if (hasMoreVideos.value) {
    await loadMoreVideos()
  }
  setTimeout(setupVideoInfiniteScroll, 500)
})

onUnmounted(() => {
  if (videoObserver) videoObserver.disconnect()
  window.removeEventListener('resize', updateColumnCount)
})

function setupVideoInfiniteScroll() {
  videoObserver = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry && entry.isIntersecting && !videoLoadingMore.value && hasMoreVideos.value && videoTasks.value.length > 0) {
        loadMoreVideos()
      }
    },
    { threshold: 0.1 }
  )
  if (sentinelRef.value) videoObserver.observe(sentinelRef.value)
}

async function loadVideoTasks() {
  videoLoading.value = true
  try {
    const res: any = await listTasks({ pageNum: videoPageNum.value, pageSize: videoPageSize.value })
    const rows = res.data?.rows || res.rows || (Array.isArray(res.data) ? res.data : [])
    const total = res.data?.total ?? res.total ?? rows.length
    videoTasks.value = rows
    videoTotal.value = total
    hasMoreVideos.value = rows.length < total
  } catch (e) {
    console.error('Failed to load video tasks:', e)
  } finally {
    videoLoading.value = false
  }
}

async function loadMoreVideos() {
  if (videoLoadingMore.value || !hasMoreVideos.value) return
  videoLoadingMore.value = true
  videoPageNum.value++
  try {
    const res: any = await listTasks({ pageNum: videoPageNum.value, pageSize: videoPageSize.value })
    const rows = res.data?.rows || res.rows || (Array.isArray(res.data) ? res.data : [])
    videoTasks.value = [...videoTasks.value, ...rows]
    hasMoreVideos.value = videoTasks.value.length < videoTotal.value
  } catch (e) {
    console.error('Failed to load more videos:', e)
    videoPageNum.value--
  } finally {
    videoLoadingMore.value = false
  }
}

async function loadTransactions() {
  txLoading.value = true
  try {
    const res: any = await getTransactions({ pageNum: txPageNum.value, pageSize: txPageSize.value })
    transactions.value = res.rows || res.data || []
    txTotal.value = res.total || 0
  } catch (e) {
    console.error('Failed to load transactions:', e)
  } finally {
    txLoading.value = false
  }
}

function onRechargeSuccess() {
  showRechargeModal.value = false
  creditsStore.fetchBalance()
  loadTransactions()
}

function handleTxTableChange(pagination: any) {
  txPageNum.value = pagination.current
  txPageSize.value = pagination.pageSize
  loadTransactions()
}

function isNegative(record: any) {
  return record.direction === -1 || ['freeze', 'consume', 'spend'].includes(record.txType)
}

function getAmountColor(record: any) {
  return isNegative(record) ? '#ff4d4f' : '#52c41a'
}

function formatAmount(record: any) {
  const amount = record.amount
  return isNegative(record) ? `-${amount}` : `+${amount}`
}

async function loadLikedIds() {
  try {
    const res: any = await getLikedTaskIds()
    const ids = res.data || res || []
    likedTaskIds.value = Array.isArray(ids) ? ids.map(Number) : []
  } catch (e) {
    console.error('Failed to load liked ids:', e)
  }
}

async function handleToggleLike(task: any) {
  const taskId = Number(task.taskId)
  if (togglingLikeSet.has(taskId)) return
  const isLiked = likedTaskIds.value.includes(taskId)
  if (isLiked) {
    likedTaskIds.value = likedTaskIds.value.filter(id => id !== taskId)
    task.likeCount = Math.max(0, (task.likeCount || 0) - 1)
  } else {
    likedTaskIds.value = [...likedTaskIds.value, taskId]
    task.likeCount = (task.likeCount || 0) + 1
  }
  togglingLikeSet.add(taskId)
  try {
    await toggleTaskLike(taskId)
  } catch (_) {
    if (isLiked) {
      likedTaskIds.value = [...likedTaskIds.value, taskId]
      task.likeCount = (task.likeCount || 0) + 1
    } else {
      likedTaskIds.value = likedTaskIds.value.filter(id => id !== taskId)
      task.likeCount = Math.max(0, (task.likeCount || 0) - 1)
    }
  } finally {
    togglingLikeSet.delete(taskId)
  }
}

function playVideo(task: any) {
  recordView(task.taskId).catch(() => {})
  router.push({
    path: `/showcase/post/${task.taskId}`,
    query: { from: route.fullPath },
    state: {
      item: JSON.parse(JSON.stringify(task)),
      from: route.fullPath
    }
  })
}

function onCardEnter(e: Event) {
  const video = (e.currentTarget as HTMLElement).querySelector('video')
  video?.play().catch(() => {})
}

function onCardLeave(e: Event) {
  const video = (e.currentTarget as HTMLElement).querySelector('video')
  if (video) {
    video.pause()
    video.currentTime = 0
  }
}

// Avatar upload
function handleAvatarClick() {
  avatarFileInput.value?.click()
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    message.error('Please select an image file')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    message.error('Image must be smaller than 5MB')
    return
  }
  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await uploadAvatar(formData)
    await userStore.fetchProfile()
    message.success(t('profile.avatarUpdated'))
  } catch (e: any) {
    message.error(e?.response?.data?.msg || 'Failed to upload avatar')
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

// Nickname editing
function startEditNickname() {
  nicknameInput.value = userStore.nickname || userStore.username || ''
  editingNickname.value = true
}

function cancelEditNickname() {
  editingNickname.value = false
  nicknameInput.value = ''
}

async function saveNickname() {
  if (!nicknameInput.value.trim()) {
    message.warning('Nickname cannot be empty')
    return
  }
  updatingNickname.value = true
  try {
    await updateProfile({ nickname: nicknameInput.value.trim() })
    await userStore.fetchProfile()
    message.success(t('profile.nicknameUpdated'))
    editingNickname.value = false
  } catch (e: any) {
    message.error(e?.response?.data?.msg || 'Failed to update nickname')
  } finally {
    updatingNickname.value = false
  }
}
</script>

<template>
  <div style="min-height: 100vh; background: transparent;">
    <div style="padding: 24px; max-width: 1400px; margin: 0 auto;">

      <!-- User Info Card -->
      <a-card :bordered="false" style="margin-bottom: 24px; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.2), 0 0 20px rgba(251,146,60,0.1); background: rgba(30, 25, 25, 0.25); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.08);">
        <a-row :gutter="24" align="middle">
          <!-- Avatar with upload -->
          <a-col>
            <div class="avatar-wrapper" @click="handleAvatarClick">
              <a-avatar
                :size="80"
                :src="userStore.avatar || undefined"
                style="background: linear-gradient(135deg, #fb923c, #f59e0b); font-size: 32px; font-weight: 700; cursor: pointer;"
              >
                {{ userStore.avatar ? '' : userInitial }}
              </a-avatar>
              <div class="avatar-overlay">
                <Loader2 v-if="uploadingAvatar" style="width: 20px; height: 20px; color: white;" class="spinning-icon" />
                <Camera v-else style="width: 20px; height: 20px; color: white;" />
              </div>
              <input
                ref="avatarFileInput"
                type="file"
                accept="image/*"
                style="display: none;"
                @change="handleAvatarChange"
              />
            </div>
          </a-col>

          <!-- Name & Info -->
          <a-col :flex="1" style="min-width: 0;">
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; flex-wrap: wrap;">
              <div v-if="!editingNickname" style="display: flex; align-items: center; gap: 8px;">
                <h1 style="font-size: 22px; font-weight: 700; margin: 0; color: rgba(255,255,255,0.9); line-height: 1.2; word-break: break-all;">{{ displayName }}</h1>
                <a-button type="text" size="small" @click="startEditNickname" style="color: rgba(255,255,255,0.5); padding: 4px;">
                  <Pencil style="width: 14px; height: 14px;" />
                </a-button>
              </div>
              <div v-else style="display: flex; align-items: center; gap: 8px;">
                <a-input
                  :value="nicknameInput"
                  @update:value="nicknameInput = $event"
                  size="small"
                  class="dark-input"
                  style="width: 180px; border-radius: 8px;"
                  @press-enter="saveNickname"
                  :max-length="30"
                />
                <a-button
                  type="primary"
                  size="small"
                  :loading="updatingNickname"
                  @click="saveNickname"
                  style="background: #ea580c; border-color: #ea580c;"
                >
                  <Check style="width: 14px; height: 14px;" />
                </a-button>
                <a-button ghost size="small" @click="cancelEditNickname">
                  <XIcon style="width: 14px; height: 14px;" />
                </a-button>
              </div>
            </div>

            <a-space :size="12" style="flex-wrap: wrap; width: 100%;">
              <span v-if="userStore.email" style="display: inline-flex; align-items: center; gap: 4px; font-size: 13px; font-family: monospace; background: rgba(255,255,255,0.1); padding: 4px 10px; border-radius: 6px; color: rgba(255,255,255,0.75);">
                <User style="width: 14px; height: 14px;" /> {{ userStore.email }}
              </span>
              <span v-if="userStore.walletAddress" style="display: inline-flex; align-items: center; gap: 4px; font-size: 13px; font-family: monospace; background: rgba(255,255,255,0.1); padding: 4px 10px; border-radius: 6px; color: rgba(255,255,255,0.75);">
                <Wallet style="width: 14px; height: 14px;" /> {{ shortenAddress(userStore.walletAddress) }}
              </span>
            </a-space>
          </a-col>

          <!-- Balance & Actions -->
          <a-col>
            <div style="display: flex; align-items: center; gap: 24px;">
              <!-- Available Credits -->
              <div
                style="display: flex; align-items: center; gap: 6px; cursor: pointer; transition: transform 0.2s;"
                @click="showTxModal = true"
                class="clickable-balance"
                :title="t('credits.details')"
              >
                <Sparkles style="height: 24px; width: 24px; color: #fb923c;" />
                <span style="font-size: 32px; font-weight: 700; color: #eab308; line-height: 1; font-family: 'SF Pro Display', system-ui, sans-serif; text-shadow: 0 0 16px rgba(251, 146, 60, 0.3);">
                  {{ creditsStore.balance !== null && creditsStore.balance !== undefined ? creditsStore.balance.toLocaleString() : '0' }}
                </span>
              </div>

              <!-- Recharge Button -->
              <a-button
                type="primary"
                size="large"
                @click="showRechargeModal = true"
                style="background: linear-gradient(135deg, #fb923c, #f59e0b); border: none; font-weight: 600; border-radius: 8px; padding: 0 24px;"
              >
                {{ t('nav.recharge') }}
              </a-button>
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- Video Masonry Grid -->
      <a-card
        :bordered="false"
        style="border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,0.2), 0 0 20px rgba(251,146,60,0.05); background: rgba(30, 25, 25, 0.25); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.08);"
      >
        <template #title>
          <span style="font-weight: 700; color: rgba(255,255,255,0.85);">{{ t('profile.myVideos') }}</span>
        </template>

        <a-spin :spinning="videoLoading">
          <!-- Empty state -->
          <div v-if="videoTasks.length === 0 && !videoLoading" style="text-align: center; padding: 48px 0; color: #9ca3af;">
            <Sparkles style="width: 40px; height: 40px; margin: 0 auto 12px; opacity: 0.4;" />
            <p style="font-size: 14px; margin: 0 0 16px;">{{ t('profile.noVideosHint') }}</p>
            <a-button type="primary" @click="router.push('/generate/create')" style="background: #ea580c; border-color: #ea580c;">
              {{ t('profile.goGenerate') }}
            </a-button>
          </div>

          <!-- Masonry columns -->
          <div v-else class="profile-masonry-grid">
            <div v-for="(col, colIndex) in videoColumns" :key="colIndex" class="profile-masonry-col">
              <div
                v-for="task in col"
                :key="task.taskId"
                class="profile-video-card is-completed"
                :style="getCardStyle(task.videoAspectRatio)"
                @click="playVideo(task)"
                @mouseenter="onCardEnter"
                @mouseleave="onCardLeave"
              >
                <!-- Video (hover autoplay for completed) -->
                <video
                  v-if="(task.status === 'completed' || task.status === 'succeeded') && task.ossVideoUrl"
                  :src="task.ossVideoUrl"
                  :poster="task.videoPicUrl || task.promptImageUrl || undefined"
                  muted
                  loop
                  preload="none"
                  playsinline
                  style="width: 100%; height: 100%; object-fit: cover; display: block;"
                />
                <!-- Static thumbnail -->
                <img
                  v-else-if="task.videoPicUrl || task.promptImageUrl"
                  :src="task.videoPicUrl || task.promptImageUrl"
                  style="width: 100%; height: 100%; object-fit: cover; display: block;"
                  loading="lazy"
                />
                <div v-else style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a1a1a, #2a2a2a);">
                  <Sparkles style="width: 28px; height: 28px; color: rgba(255,255,255,0.2);" />
                </div>

                <!-- Play overlay -->
                <div v-if="task.status === 'completed' || task.status === 'succeeded'" class="thumb-play-overlay">
                  <div style="width: 40px; height: 40px; background: rgba(255,255,255,0.25); backdrop-filter: blur(6px); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                    <Play style="width: 18px; height: 18px; color: #fff; margin-left: 2px;" />
                  </div>
                </div>

                <!-- Status badge (top-right) -->
                <div class="card-status-badge">
                  <a-tag :color="statusConfig[task.status]?.color || 'default'" style="font-size: 10px; margin: 0; background: rgba(0,0,0,0.6); border: none; color: #fff;">
                    {{ t(statusConfig[task.status]?.label || task.status) }}
                  </a-tag>
                </div>

                <!-- Like button (bottom-left) -->
                <div class="thumb-like-btn" @click.stop="handleToggleLike(task)">
                  <Heart
                    style="width: 13px; height: 13px; flex-shrink: 0; transition: all 0.2s;"
                    :style="likedTaskIds.includes(Number(task.taskId))
                      ? { fill: '#ef4444', color: '#ef4444' }
                      : { color: 'rgba(255,255,255,0.85)' }"
                  />
                  <span v-if="(task.likeCount || 0) > 0" style="font-size: 11px; color: rgba(255,255,255,0.9); font-weight: 600; line-height: 1;">{{ task.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-spin>

        <!-- Infinite scroll sentinel -->
        <div ref="sentinelRef" style="height: 48px; display: flex; align-items: center; justify-content: center;">
          <a-spin v-if="videoLoadingMore" />
        </div>
      </a-card>

    </div>

    <!-- Transaction History Modal -->
    <a-modal
      :open="showTxModal"
      @update:open="showTxModal = $event"
      @cancel="showTxModal = false"
      :title="t('credits.allRecords')"
      :footer="null"
      :width="720"
      centered
      wrapClassName="dark-modal"
    >
      <a-table
        :dataSource="transactions"
        :columns="txColumns"
        :loading="txLoading"
        :pagination="{ current: txPageNum, pageSize: txPageSize, total: txTotal }"
        @change="handleTxTableChange"
        rowKey="transactionId"
        size="small"
        class="dark-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span :style="{ color: getAmountColor(record), fontWeight: 'bold' }">
              {{ formatAmount(record) }}
            </span>
            <span style="font-size: 12px; color: rgba(255,255,255,0.4); margin-left: 4px;">{{ t('credits.amount') }}</span>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- Video Play Modal -->
    <VideoPlayModal
      :open="showVideoModal"
      :item="currentVideoItem"
      @update:open="showVideoModal = $event"
    />

    <!-- Recharge Modal -->
    <RechargeModal
      :visible="showRechargeModal"
      @update:visible="showRechargeModal = $event"
      @success="onRechargeSuccess"
    />
  </div>
</template>

<style scoped>
.avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  border-radius: 50%;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}


.clickable-balance:hover {
  transform: scale(1.05);
  opacity: 0.9;
}

/* Masonry layout */
.profile-masonry-grid {
  display: flex;
  gap: 14px;
  margin-bottom: 8px;
  overflow: hidden;
}

.profile-masonry-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-video-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
  background: rgba(30, 25, 25, 0.4);
  transition: all 0.2s;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  cursor: default;
  position: relative;
}

.profile-video-card.is-completed {
  cursor: pointer;
}

.profile-video-card.is-completed:hover {
  box-shadow: 0 8px 28px rgba(251, 146, 60, 0.35), 0 0 16px rgba(251, 146, 60, 0.15);
  transform: translateY(-3px);
  border-color: rgba(251, 146, 60, 0.5);
}

.thumb-play-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.35), transparent, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.profile-video-card.is-completed:hover .thumb-play-overlay {
  opacity: 1;
}

/* Status badge (top-right, visible on hover) */
.card-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.profile-video-card:hover .card-status-badge {
  opacity: 1;
}

/* Like button */
.thumb-like-btn {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 4px 9px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 3;
}
.thumb-like-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.05);
}

.spinning-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

:deep(.ant-card) {
  border-radius: 12px;
}

:deep(.ant-statistic-title) {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
}

:deep(.ant-statistic-content-value) {
  font-weight: 700;
  color: rgba(255,255,255,0.9);
}

/* Dark input for nickname edit */
.dark-input {
  background: rgba(0, 0, 0, 0.2) !important;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}
.dark-input::placeholder {
  color: rgba(255, 255, 255, 0.4) !important;
}
</style>

<style>
/* Global styles for dark modal injected from Profile.vue */
.dark-modal .ant-modal-content {
  background: rgba(40, 35, 35, 0.85) !important;
  backdrop-filter: blur(24px) !important;
  border: 1px solid rgba(255,255,255,0.1) !important;
  border-radius: 16px !important;
  box-shadow: 0 12px 48px rgba(0,0,0,0.5) !important;
}

.dark-modal .ant-modal-header {
  background: transparent !important;
  border-bottom: 1px solid rgba(255,255,255,0.08) !important;
}

.dark-modal .ant-modal-title {
  color: rgba(255,255,255,0.9) !important;
  font-weight: 700 !important;
}

.dark-modal .ant-modal-close {
  color: rgba(255,255,255,0.5) !important;
}

.dark-modal .ant-modal-close:hover {
  color: #fff !important;
}

/* Dark Table Styles */
.dark-table .ant-table {
  background: transparent !important;
  color: rgba(255,255,255,0.8) !important;
}

.dark-table .ant-table-thead > tr > th {
  background: rgba(0,0,0,0.2) !important;
  border-bottom: 1px solid rgba(255,255,255,0.08) !important;
  color: rgba(255,255,255,0.6) !important;
}

.dark-table .ant-table-tbody > tr > td {
  border-bottom: 1px solid rgba(255,255,255,0.05) !important;
  vertical-align: middle;
  padding: 16px 16px !important;
}

.dark-table .ant-table-tbody > tr:hover > td {
  background: rgba(255,255,255,0.05) !important;
}

.dark-table .ant-pagination-item {
  background: rgba(0,0,0,0.2) !important;
  border-color: rgba(255,255,255,0.1) !important;
}
.dark-table .ant-pagination-item a {
  color: rgba(255,255,255,0.6) !important;
}
.dark-table .ant-pagination-item-active {
  background: rgba(251,146,60,0.2) !important;
  border-color: #fb923c !important;
}
.dark-table .ant-pagination-item-active a {
  color: #fb923c !important;
}
.dark-table .ant-pagination-prev .ant-pagination-item-link,
.dark-table .ant-pagination-next .ant-pagination-item-link {
  background: rgba(0,0,0,0.2) !important;
  border-color: rgba(255,255,255,0.1) !important;
  color: rgba(255,255,255,0.6) !important;
}

.dark-table .ant-spin-nested-loading > div > .ant-spin {
  max-height: initial;
}
</style>
