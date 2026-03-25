<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { getTaskDetail, toggleTaskLike, getLikedTaskIds, recordView } from '@/api/task'
import { createShare } from '@/api/share'
import { useShare } from '@/composables/useShare'
import { message } from 'ant-design-vue'
import { Heart, Link, X as XIcon, ChevronLeft, Download } from 'lucide-vue-next'
import PromptBar from '@/components/PromptBar.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()
const { copyLink, copied, shareToTwitter } = useShare()

const taskId = computed(() => route.params.taskId as string)

// Data
const item = ref<any>(null)
const loading = ref(true)
const fetchError = ref('')

// Like state
const isLiked = ref(false)
const likeCount = ref(0)
const togglingLike = ref(false)

// Share
const sharing = ref(false)

// Video player ref
const videoPlayer = ref<HTMLVideoElement | null>(null)

// Polling
let pollingTimer: ReturnType<typeof setInterval> | null = null

const status = computed(() => item.value?.status || '')
const progress = computed(() => Math.min(100, parseInt(String(item.value?.progress || 0))))
const videoUrl = computed(() => item.value?.ossVideoUrl || item.value?.videoUrl || '')
const videoPicUrl = computed(() => item.value?.videoPicUrl || item.value?.promptImageUrl || '')
const promptText = computed(() => item.value?.promptText || item.value?.prompt || '')
const isCompleted = computed(() => ['succeeded', 'completed'].includes(status.value) && !!item.value?.ossVideoUrl)
const isProcessing = computed(() => ['pending', 'processing'].includes(status.value) || (['succeeded', 'completed'].includes(status.value) && !item.value?.ossVideoUrl))
const isFailed = computed(() => status.value === 'failed')

// Video container sizing: keep video large without overlapping nav or prompt bar.
// Avoid forcing width:100% so the intrinsic aspect ratio controls sizing.
const videoContainerStyle = computed(() => ({
  // leave room for top nav + bottom prompt bar, but keep video as tall as possible
  maxHeight: 'calc(100vh - 210px)',
  maxWidth: 'calc(100vw - 360px)'
}))

const pageUrl = computed(() => `${window.location.origin}/showcase/post/${taskId.value}`)
const sharePromoText = computed(() => t('generate.sharePromoText'))
const backPath = computed(() => {
  const stateFrom = (history.state as any)?.from
  const queryFrom = typeof route.query.from === 'string' ? route.query.from : ''
  const candidate = stateFrom || queryFrom || '/showcase'
  if (typeof candidate === 'string' && candidate.startsWith('/showcase/post/')) return '/showcase'
  return candidate
})

async function fetchData() {
  try {
    const res: any = await getTaskDetail(taskId.value)
    const data = res.data || res
    item.value = { ...item.value, ...data }
    likeCount.value = data.likeCount ?? likeCount.value
  } catch {
    fetchError.value = 'Failed to load video'
  }
}

function startPolling() {
  if (pollingTimer) clearInterval(pollingTimer)
  pollingTimer = setInterval(async () => {
    const wasCompleted = isCompleted.value
    await fetchData()
    // When video becomes completed, auto play it
    if (!wasCompleted && isCompleted.value) {
      playVideo()
    }
    if (isCompleted.value || isFailed.value) {
      clearInterval(pollingTimer!)
      pollingTimer = null
    }
  }, 5000)
}

async function loadLikedState() {
  if (!userStore.isLoggedIn()) return
  try {
    const res: any = await getLikedTaskIds()
    const ids: number[] = (res.data || res || []).map(Number)
    isLiked.value = ids.includes(Number(taskId.value))
  } catch {}
}

async function handleToggleLike() {
  if (!userStore.isLoggedIn()) {
    userStore.showLoginModal()
    return
  }
  if (togglingLike.value) return
  togglingLike.value = true
  const wasLiked = isLiked.value
  isLiked.value = !wasLiked
  likeCount.value = wasLiked ? Math.max(0, likeCount.value - 1) : likeCount.value + 1
  try {
    await toggleTaskLike(taskId.value)
  } catch {
    isLiked.value = wasLiked
    likeCount.value = wasLiked ? likeCount.value + 1 : Math.max(0, likeCount.value - 1)
    message.error(t('showcase.likeFailed'))
  } finally {
    togglingLike.value = false
  }
}

async function handleCopyLink() {
  await copyLink(pageUrl.value)
}

async function handleShareX() {
  sharing.value = true
  try {
    try { await createShare(taskId.value, { platform: 'twitter' }) } catch {}
    shareToTwitter(sharePromoText.value, pageUrl.value)
  } finally {
    sharing.value = false
  }
}

function handleDownload() {
  if (!videoUrl.value) return
  // Open video in new tab for download
  window.open(videoUrl.value, '_blank')
}

function handleBack() {
  router.push(backPath.value)
}

async function playVideo() {
  await nextTick()
  if (videoPlayer.value) {
    videoPlayer.value.muted = false
    videoPlayer.value.volume = 1
    videoPlayer.value.play().catch(() => {
      // Auto-play may be blocked; user interaction might be required
    })
  }
}

async function initPage() {
  // Reset state
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  fetchError.value = ''
  loading.value = true

  // Use initial data from navigation state
  const stateItem = (history.state as any)?.item
  if (stateItem) {
    item.value = stateItem
    likeCount.value = stateItem.likeCount || 0
  } else {
    item.value = null
  }

  await fetchData()
  loading.value = false

  loadLikedState()
  recordView(taskId.value).catch(() => {})

  // Poll if task is in progress
  if (isProcessing.value) {
    startPolling()
  } else if (isCompleted.value) {
    // Auto play video when completed
    playVideo()
  }
}

// Watch for taskId changes (when user creates new task from this page)
watch(taskId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    isLiked.value = false
    initPage()
  }
})

onMounted(() => {
  initPage()
})

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})
</script>

<template>
  <div class="detail-page">
    <div class="detail-container">
      <!-- Loading (no initial data) -->
      <div v-if="loading && !item" class="detail-loading">
        <button class="back-btn-top" @click="handleBack">
          <ChevronLeft style="width: 20px; height: 20px;" />
        </button>
        <a-spin size="large" />
      </div>

      <!-- Error (no data at all) -->
      <a-result
        v-else-if="fetchError && !item"
        status="error"
        :title="fetchError"
        style="color: rgba(255,255,255,0.8);"
      >
        <template #extra>
          <a-button @click="handleBack">{{ t('common.back') }}</a-button>
        </template>
      </a-result>

      <!-- Main content -->
      <div v-else-if="item || !loading" class="detail-main">
        <!-- Video wrapper: positioning context for back button and action column -->
        <div class="video-wrapper">
          <!-- Back button -->
          <button class="back-btn" @click="handleBack">
            <ChevronLeft style="width: 22px; height: 22px;" />
          </button>
          <!-- Video section -->
          <div class="video-section">
            <!-- Processing: cinematic progress display -->
            <div v-if="isProcessing" class="progress-card">
              <div class="prog-orb-system">
                <!-- Ambient glow -->
                <div class="prog-glow-base"></div>
                <!-- Outer rotating ring -->
                <div class="prog-ring-outer"></div>
                <!-- SVG progress arc -->
                <svg class="prog-svg" viewBox="0 0 200 200">
                  <defs>
                    <linearGradient id="progGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#fb923c"/>
                      <stop offset="100%" stop-color="#f59e0b"/>
                    </linearGradient>
                    <filter id="progGlow">
                      <feGaussianBlur stdDeviation="2.5" result="coloredBlur"/>
                      <feMerge>
                        <feMergeNode in="coloredBlur"/>
                        <feMergeNode in="SourceGraphic"/>
                      </feMerge>
                    </filter>
                  </defs>
                  <!-- Track -->
                  <circle cx="100" cy="100" r="80" fill="none"
                    stroke="rgba(251,146,60,0.22)" stroke-width="2"/>
                  <!-- Progress arc -->
                  <circle cx="100" cy="100" r="80" fill="none"
                    stroke="url(#progGrad)" stroke-width="3"
                    stroke-linecap="round"
                    filter="url(#progGlow)"
                    :style="{
                      strokeDasharray: '502.65',
                      strokeDashoffset: 502.65 * (1 - progress / 100),
                      transform: 'rotate(-90deg)',
                      transformOrigin: '100px 100px',
                      transition: 'stroke-dashoffset 0.9s cubic-bezier(0.4,0,0.2,1)'
                    }"
                  />
                </svg>
                <!-- Inner orb -->
                <div class="prog-inner-orb"></div>
                <!-- Percentage -->
                <div class="prog-center">
                  <span class="prog-percent">{{ progress }}<span class="prog-percent-sign">%</span></span>
                </div>
                <!-- Orbiting particles -->
                <div class="prog-particle p1"></div>
                <div class="prog-particle p2"></div>
                <div class="prog-particle p3"></div>
                <div class="prog-particle p4"></div>
              </div>

              <div class="prog-labels">
                <p class="progress-label">{{ t('showcase.creatingMasterpiece') }}</p>
                <div class="prog-dots">
                  <span class="prog-dot d1"></span>
                  <span class="prog-dot d2"></span>
                  <span class="prog-dot d3"></span>
                </div>
              </div>
            </div>

            <!-- Failed -->
            <div v-else-if="isFailed" class="error-card">
              <a-result
                status="error"
                :title="t('showcase.failed')"
                :sub-title="item?.errorMessage || t('showcase.pleaseTryAgain')"
              />
            </div>

            <!-- Video player -->
            <video
              v-else-if="videoUrl"
              ref="videoPlayer"
              :src="videoUrl"
              :poster="videoPicUrl || undefined"
              controls
              autoplay
              loop
              playsinline
              class="detail-video"
              :style="videoContainerStyle"
            />

            <!-- Thumbnail fallback -->
            <div v-else class="video-placeholder">
              <img v-if="videoPicUrl" :src="videoPicUrl" class="video-thumb" />
              <div v-else class="video-no-thumb">
                <a-spin v-if="loading" />
                <span v-else style="color: rgba(255,255,255,0.3); font-size: 13px;">No preview available</span>
              </div>
            </div>
            <div v-if="isCompleted" class="actions-col">
              <button class="action-btn like-btn" :class="{ liked: isLiked }" @click="handleToggleLike">
                <div class="action-icon-wrap">
                  <Heart
                    style="width: 24px; height: 24px;"
                    :style="isLiked ? { fill: '#ef4444', color: '#ef4444' } : { color: '#8b5a2b' }"
                  />
                </div>
                <span class="action-label">{{ likeCount > 0 ? likeCount : t('showcase.likes') }}</span>
              </button>

              <button class="action-btn" @click="handleDownload">
                <div class="action-icon-wrap">
                  <Download style="width: 22px; height: 22px; color: #8b5a2b;" />
                </div>
                <span class="action-label">{{ t('generate.download') || 'Save' }}</span>
              </button>

              <button class="action-btn" @click="handleCopyLink">
                <div class="action-icon-wrap">
                  <Link style="width: 22px; height: 22px; color: #8b5a2b;" />
                </div>
                <span class="action-label">{{ copied ? t('generate.copied') : t('generate.copyLink') }}</span>
              </button>

              <button class="action-btn" :disabled="sharing" @click="handleShareX">
                <div class="action-icon-wrap">
                  <XIcon style="width: 22px; height: 22px; color: #8b5a2b;" />
                </div>
                <span class="action-label">{{ t('generate.shareToX') }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom prompt bar -->
    <PromptBar />
  </div>
</template>

<style scoped>
.detail-page {
  height: 100vh;
  overflow: hidden;
  padding-bottom: 0;
}

.detail-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px 0;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

.back-btn {
  position: static;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 248, 236, 0.22);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(190, 155, 95, 0.28);
  color: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(255, 248, 236, 0.45);
  color: rgba(255, 255, 255, 1);
  border-color: rgba(190, 155, 95, 0.5);
}

/* Loading state back button */
.back-btn-top {
  position: absolute;
  top: 12px;
  left: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 248, 236, 0.22);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(190, 155, 95, 0.28);
  color: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

/* Loading */
.detail-loading {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 320px;
}

.detail-main {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  padding: 8px 90px 130px;
  min-height: 0;
}

.video-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  width: fit-content;
  max-width: 100%;
  margin: 0 auto;
}

.video-section {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: transparent;
  border-radius: 0;
  max-width: 100%;
  min-width: 0;
  flex-shrink: 1;
}

.detail-video {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: calc(100vh - 190px);
  object-fit: contain;
  display: block;
  border-radius: 12px;
}

.video-thumb {
  width: 100%;
  max-height: calc(100vh - 200px);
  object-fit: contain;
  display: block;
  border-radius: 12px;
}

.video-placeholder {
  height: calc(100vh - 200px);
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-no-thumb {
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12px;
}

/* ── Progress card — cinematic orbital system ── */
.progress-card {
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 28px;
  text-align: center;
  background: transparent;
  border: none;
  box-shadow: none;
}

/* Orbital system container */
.prog-orb-system {
  position: relative;
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

/* Ambient background glow — stronger on dark */
.prog-glow-base {
  position: absolute;
  inset: -24px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%,
    rgba(251, 146, 60, 0.35) 0%,
    rgba(245, 158, 11, 0.18) 45%,
    transparent 70%);
  animation: progGlowPulse 3s ease-in-out infinite;
}

/* Outer rotating dashed ring */
.prog-ring-outer {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 1.5px dashed rgba(251, 146, 60, 0.55);
  animation: progRingRotate 12s linear infinite;
}

/* SVG arc fills the container */
.prog-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

/* Inner pulsing orb — dark fill gives number a readable backdrop */
.prog-inner-orb {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 140px;
  height: 140px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%,
    rgba(10, 6, 2, 0.72) 0%,
    rgba(20, 12, 4, 0.60) 50%,
    rgba(30, 15, 4, 0.30) 75%,
    transparent 100%);
  border: 1px solid rgba(251, 146, 60, 0.28);
  animation: progInnerPulse 2.6s ease-in-out infinite;
}

/* Percentage text centered */
.prog-center {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.prog-percent {
  font-size: 40px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -1.5px;
  line-height: 1;
  text-shadow:
    0 0 24px rgba(251, 146, 60, 0.9),
    0 0 48px rgba(251, 146, 60, 0.5),
    0 2px 12px rgba(0, 0, 0, 0.8),
    0 4px 20px rgba(0, 0, 0, 0.6);
}

.prog-percent-sign {
  font-size: 19px;
  font-weight: 500;
  color: rgba(251, 180, 100, 0.85);
  margin-left: 2px;
  vertical-align: super;
}

/* Orbiting particles */
.prog-particle {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: radial-gradient(circle, #fde68a, #fb923c);
  box-shadow: 0 0 10px rgba(251, 146, 60, 1), 0 0 22px rgba(245, 158, 11, 0.7);
  margin: -3.5px 0 0 -3.5px;
}

.prog-particle.p1 { animation: progOrbit1 4.2s linear infinite; }
.prog-particle.p2 { animation: progOrbit2 5.8s linear infinite 1.45s; }
.prog-particle.p3 {
  animation: progOrbit3 4.9s linear infinite 2.9s;
  width: 4px; height: 4px; margin: -2px 0 0 -2px; opacity: 0.7;
}
.prog-particle.p4 {
  animation: progOrbit4 7s linear infinite 0.7s;
  width: 4px; height: 4px; margin: -2px 0 0 -2px; opacity: 0.5;
}

/* Label area */
.prog-labels {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.progress-label {
  color: rgba(255, 230, 190, 0.92);
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.8px;
  margin: 0;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.6);
}

/* Animated loading dots */
.prog-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.prog-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(251, 146, 60, 0.7);
  animation: progDotBounce 1.4s ease-in-out infinite;
}
.prog-dot.d1 { animation-delay: 0s; }
.prog-dot.d2 { animation-delay: 0.22s; }
.prog-dot.d3 { animation-delay: 0.44s; }

/* ── Keyframes ── */
@keyframes progGlowPulse {
  0%, 100% { opacity: 0.7; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}

@keyframes progRingRotate {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@keyframes progInnerPulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50%       { opacity: 1;   transform: translate(-50%, -50%) scale(1.06); }
}

@keyframes progOrbit1 {
  from { transform: rotate(0deg) translateX(82px) rotate(0deg); }
  to   { transform: rotate(360deg) translateX(82px) rotate(-360deg); }
}
@keyframes progOrbit2 {
  from { transform: rotate(90deg) translateX(78px) rotate(-90deg); }
  to   { transform: rotate(450deg) translateX(78px) rotate(-450deg); }
}
@keyframes progOrbit3 {
  from { transform: rotate(180deg) translateX(86px) rotate(-180deg); }
  to   { transform: rotate(540deg) translateX(86px) rotate(-540deg); }
}
@keyframes progOrbit4 {
  from { transform: rotate(270deg) translateX(74px) rotate(-270deg); }
  to   { transform: rotate(630deg) translateX(74px) rotate(-630deg); }
}

@keyframes progDotBounce {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50%       { transform: translateY(-5px); opacity: 1; }
}

.error-card {
  padding: 40px;
}

.actions-col {
  position: absolute;
  left: calc(100% + 18px);
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 86px;
  flex-shrink: 0;
}

/* Circular icon buttons */
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  transition: transform 0.22s ease;
}

.action-btn:hover:not(:disabled) {
  transform: scale(1.08);
}

.action-btn:disabled {
  opacity: 0.38;
  cursor: not-allowed;
}

.action-icon-wrap {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  background: radial-gradient(circle at 28% 22%, rgba(255, 255, 255, 0.56), rgba(255, 247, 239, 0.24) 72%);
  backdrop-filter: blur(14px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(114, 80, 39, 0.16), inset 0 0 0 1px rgba(255, 255, 255, 0.65);
  transition: all 0.22s ease;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.action-btn:hover:not(:disabled) .action-icon-wrap {
  background: radial-gradient(circle at 28% 22%, rgba(255, 255, 255, 0.68), rgba(255, 247, 239, 0.34) 72%);
  box-shadow: 0 9px 24px rgba(114, 80, 39, 0.22), inset 0 0 0 1px rgba(255, 255, 255, 0.9);
}

.action-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(73, 46, 20, 0.88);
  letter-spacing: 0.2px;
  line-height: 1.05;
  white-space: nowrap;
}

/* Liked state */
.like-btn.liked .action-icon-wrap {
  background: radial-gradient(circle at 28% 22%, rgba(255, 234, 234, 0.66), rgba(255, 214, 214, 0.36) 74%);
  border-color: rgba(255, 223, 223, 0.95);
}

@media (max-width: 980px) {
  .detail-main {
    padding: 8px 24px 130px;
  }

  .video-wrapper {
    width: 100%;
    gap: 14px;
  }

  .actions-col {
    position: static;
    transform: none;
    width: auto;
    margin-left: 0;
  }
}
</style>
















