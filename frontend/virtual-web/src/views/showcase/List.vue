<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { useCreditsStore } from '@/store/credits'
import { getShowcaseList, toggleTaskLike, getLikedTaskIds, recordView } from '@/api/task'
import { message } from 'ant-design-vue'
import { Play, Clock, Heart } from 'lucide-vue-next'
import PromptBar from '@/components/PromptBar.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const creditsStore = useCreditsStore()

// --- Showcase list state ---
const items = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = 12

// --- Like state ---
const likedTaskIds = ref<number[]>([])
const togglingLikeSet = new Set<number>()

// --- Masonry column distribution ---
const columnCount = ref(4)

function updateColumnCount() {
  const width = window.innerWidth
  if (width <= 640) columnCount.value = 2
  else if (width <= 1000) columnCount.value = 3
  else if (width <= 1400) columnCount.value = 4
  else columnCount.value = 5
}

const columns = computed(() => {
  const cols: any[][] = Array.from({ length: columnCount.value }, () => [])
  items.value.forEach((item, i) => {
    cols[i % columnCount.value]!.push(item)
  })
  return cols
})

// --- Aspect ratio helper ---
function getCardStyle(aspectRatioStr: string | undefined) {
  if (!aspectRatioStr) return { aspectRatio: '3/4', width: '100%', overflow: 'hidden' }
  const parts = aspectRatioStr.split(':')
  if (parts.length !== 2) return { aspectRatio: '3/4', width: '100%', overflow: 'hidden' }

  const w = parseFloat(parts[0] || '3')
  const h = parseFloat(parts[1] || '4')
  const ratio = w / h
  let maxHeight = 'none'

  if (ratio < 0.6) maxHeight = '400px'
  else if (ratio < 0.8) maxHeight = '360px'
  else if (ratio <= 1.2) maxHeight = '280px'
  else maxHeight = '200px'

  return { aspectRatio: `${parts[0]}/${parts[1]}`, maxHeight, width: '100%', overflow: 'hidden' }
}

// Navigate to detail page
function goToDetail(item: any) {
  recordView(item.taskId).catch(() => {})
  router.push({
    path: `/showcase/post/${item.taskId}`,
    query: { from: route.fullPath },
    state: {
      item: JSON.parse(JSON.stringify(item)),
      from: route.fullPath
    }
  })
}

// Hover autoplay
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

// --- Data fetching ---
async function fetchShowcase(isLoadMore = false) {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res: any = await getShowcaseList({ pageNum: pageNum.value, pageSize })
    const newItems = res.rows || []
    const total = res.total || 0

    if (isLoadMore) {
      items.value = [...items.value, ...newItems]
    } else {
      items.value = newItems
    }

    hasMore.value = items.value.length < total
  } catch (e) {
    console.error('Failed to fetch showcase:', e)
    message.error(t('showcase.loadFailed'))
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// --- Infinite scroll ---
let observer: IntersectionObserver | null = null
const sentinelRef = ref<HTMLElement | null>(null)

function setupInfiniteScroll() {
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      if (entry?.isIntersecting && !loadingMore.value && hasMore.value && items.value.length > 0) {
        pageNum.value++
        fetchShowcase(true)
      }
    },
    { threshold: 0.1 }
  )
  if (sentinelRef.value) observer.observe(sentinelRef.value)
}

// --- Like actions ---
async function loadLikedIds() {
  if (!userStore.isLoggedIn()) return
  try {
    const res: any = await getLikedTaskIds()
    const ids = res.data || res || []
    likedTaskIds.value = Array.isArray(ids) ? ids.map(Number) : []
  } catch (e) {
    console.error('Failed to load liked ids:', e)
  }
}

async function handleToggleLike(item: any) {
  if (!userStore.isLoggedIn()) {
    userStore.showLoginModal()
    return
  }
  const taskId = Number(item.taskId)
  if (togglingLikeSet.has(taskId)) return

  const isCurrentlyLiked = likedTaskIds.value.includes(taskId)
  if (isCurrentlyLiked) {
    likedTaskIds.value = likedTaskIds.value.filter(id => id !== taskId)
    item.likeCount = Math.max(0, (item.likeCount || 0) - 1)
  } else {
    likedTaskIds.value = [...likedTaskIds.value, taskId]
    item.likeCount = (item.likeCount || 0) + 1
  }

  togglingLikeSet.add(taskId)
  try {
    await toggleTaskLike(taskId)
  } catch {
    if (isCurrentlyLiked) {
      likedTaskIds.value = [...likedTaskIds.value, taskId]
      item.likeCount = (item.likeCount || 0) + 1
    } else {
      likedTaskIds.value = likedTaskIds.value.filter(id => id !== taskId)
      item.likeCount = Math.max(0, (item.likeCount || 0) - 1)
    }
    message.error(t('showcase.likeFailed'))
  } finally {
    togglingLikeSet.delete(taskId)
  }
}

// --- Lifecycle ---
onMounted(async () => {
  await fetchShowcase()
  // 首页加载完成后，若存在下一页则自动预取一次
  if (hasMore.value) {
    pageNum.value++
    await fetchShowcase(true)
  }
  if (userStore.isLoggedIn()) {
    creditsStore.fetchBalance()
    loadLikedIds()
  }
  updateColumnCount()
  window.addEventListener('resize', updateColumnCount)
  setTimeout(setupInfiniteScroll, 1000)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  window.removeEventListener('resize', updateColumnCount)
})
</script>

<template>
  <div style="min-height: 100vh; background: transparent; padding-bottom: 110px;">
    <div style="max-width: 1600px; margin: 0 auto; padding: 24px 32px 0;">

      <!-- Loading skeleton -->
      <a-spin v-if="loading && items.length === 0" style="display: block; text-align: center; padding: 80px 0;" />

      <!-- Masonry grid -->
      <div class="masonry-grid">
        <div v-for="(col, colIndex) in columns" :key="colIndex" class="masonry-column">
          <div
            v-for="item in col"
            :key="item.taskId || item.showcaseId"
            class="video-card"
            :style="getCardStyle(item.videoAspectRatio)"
            @click="goToDetail(item)"
            @mouseenter="onCardEnter"
            @mouseleave="onCardLeave"
          >
            <!-- Video with poster (hover to play) -->
            <video
              v-if="item.ossVideoUrl"
              :src="item.ossVideoUrl"
              :poster="item.videoPicUrl || item.promptImageUrl || undefined"
              muted
              loop
              preload="none"
              playsinline
              style="width: 100%; height: 100%; object-fit: cover; display: block;"
            />
            <!-- Fallback: static image only -->
            <img
              v-else-if="item.videoPicUrl || item.promptImageUrl"
              :src="item.videoPicUrl || item.promptImageUrl"
              style="width: 100%; height: 100%; object-fit: cover; display: block;"
              loading="lazy"
            />
            <div v-else style="width: 100%; aspect-ratio: 3/4; display: flex; align-items: center; justify-content: center; background: #111;">
              <Play style="width: 40px; height: 40px; color: rgba(255,255,255,0.3);" />
            </div>

            <!-- Hover overlay -->
            <div class="video-overlay">
              <div style="width: 48px; height: 48px; background: rgba(255,255,255,0.28); backdrop-filter: blur(8px); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                <Play style="width: 24px; height: 24px; color: #fff; margin-left: 2px;" />
              </div>
            </div>

            <!-- Duration badge -->
            <div class="video-duration">
              <a-tag color="default" style="background: rgba(0,0,0,0.6); color: #fff; border: none; font-size: 10px; font-weight: 700;">
                <a-space :size="4">
                  <Clock style="width: 12px; height: 12px;" />
                  <span>{{ item.videoDuration || '5' }}s</span>
                </a-space>
              </a-tag>
            </div>

            <!-- Like button -->
            <div class="video-like-btn" @click.stop="handleToggleLike(item)">
              <Heart
                style="width: 14px; height: 14px; flex-shrink: 0; transition: all 0.2s;"
                :style="likedTaskIds.includes(Number(item.taskId))
                  ? { fill: '#ef4444', color: '#ef4444' }
                  : { color: 'rgba(255,255,255,0.85)' }"
              />
              <span
                v-if="(item.likeCount || 0) > 0"
                style="font-size: 11px; color: rgba(255,255,255,0.9); font-weight: 600; line-height: 1;"
              >{{ item.likeCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Infinite scroll sentinel -->
      <div ref="sentinelRef" style="padding: 48px 0; text-align: center; height: 96px;">
        <a-spin v-if="loadingMore" />
      </div>
    </div>

    <!-- Bottom prompt bar -->
    <PromptBar />
  </div>
</template>

<style scoped>
/* Masonry layout */
.masonry-grid {
  display: flex;
  gap: 12px;
}

.masonry-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Video card */
.video-card {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  background: #0d0d0d;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

.video-card:hover {
  transform: translateY(-4px) scale(1.015);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.6), 0 0 20px rgba(251, 146, 60, 0.2);
}

/* Hover play overlay */
.video-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.35), transparent 60%);
  opacity: 0;
  transition: opacity 0.25s ease;
  pointer-events: none;
}

.video-card:hover .video-overlay {
  opacity: 1;
}

/* Duration badge */
.video-duration {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.video-card:hover .video-duration {
  opacity: 1;
}

/* Like button */
.video-like-btn {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 5px 9px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 3;
}

.video-like-btn:hover {
  background: rgba(0, 0, 0, 0.72);
  transform: scale(1.05);
}


/* Mobile: 2 columns */
@media (max-width: 640px) {
  .masonry-grid {
    gap: 8px;
  }
  .masonry-column {
    gap: 8px;
  }
}
</style>
