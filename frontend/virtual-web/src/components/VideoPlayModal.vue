<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Link, X, Camera, Music2 } from 'lucide-vue-next'
import { useShare } from '@/composables/useShare'
import { createShare } from '@/api/share'

const props = defineProps<{
  open: boolean
  item: any
}>()

const emit = defineEmits<{ (e: 'update:open', val: boolean): void }>()

const { t } = useI18n()
const { copyLink, copied, shareToTwitter } = useShare()

// Per-button copied/opened states
const insCopied = ref(false)
const tiktokCopied = ref(false)
const sharing = ref(false)

const videoUrl = computed(() => props.item?.ossVideoUrl || '')
const taskId = computed(() => props.item?.taskId || props.item?.showcaseId || '')
const hasVideo = computed(() => !!videoUrl.value)

// Promotional share message (not prompt text)
const sharePromoText = computed(() => t('generate.sharePromoText'))

async function handleCopyLink() {
  if (!hasVideo.value) return
  await copyLink(videoUrl.value)
}

async function handleShareX() {
  if (!hasVideo.value) return
  sharing.value = true
  try {
    try { await createShare(taskId.value, { platform: 'twitter' }) } catch (_) { /* ignore */ }
    shareToTwitter(sharePromoText.value, videoUrl.value)
  } finally {
    sharing.value = false
  }
}

async function handleShareIns() {
  if (!hasVideo.value) return
  await copyLink(videoUrl.value)
  window.open('https://www.instagram.com/', '_blank')
  insCopied.value = true
  setTimeout(() => { insCopied.value = false }, 3000)
  try { await createShare(taskId.value, { platform: 'instagram' }) } catch (_) { /* ignore */ }
}

async function handleShareTiktok() {
  if (!hasVideo.value) return
  await copyLink(videoUrl.value)
  window.open('https://www.tiktok.com/upload', '_blank')
  tiktokCopied.value = true
  setTimeout(() => { tiktokCopied.value = false }, 3000)
  try { await createShare(taskId.value, { platform: 'tiktok' }) } catch (_) { /* ignore */ }
}
</script>

<template>
  <a-modal
    :open="props.open"
    @update:open="emit('update:open', $event)"
    @cancel="emit('update:open', false)"
    :title="null"
    :footer="null"
    :width="840"
    centered
    wrapClassName="glass-video-modal"
    destroyOnClose
    :body-style="{ padding: 0 }"
    :mask-style="{ background: 'rgba(18, 12, 6, 0.82)' }"
  >
    <div class="vpm-layout">
      <!-- Left: Video Player -->
      <div class="vpm-video-area">
        <video
          v-if="hasVideo"
          :src="videoUrl"
          controls
          autoplay
          loop
          class="vpm-video"
        />
        <div v-else class="vpm-no-video">
          <Music2 style="width: 40px; height: 40px; color: rgba(255,255,255,0.3);" />
          <p style="color: rgba(255,255,255,0.4); font-size: 13px; margin: 12px 0 0;">No video available</p>
        </div>
      </div>

      <!-- Right: Share Buttons -->
      <div class="vpm-share-area">
        <p class="vpm-share-title">{{ t('generate.share') }}</p>

        <!-- Copy Link -->
        <button class="vpm-share-btn" :disabled="!hasVideo" @click="handleCopyLink">
          <Link class="vpm-btn-icon" />
          <span>{{ copied ? t('generate.copied') : t('generate.copyLink') }}</span>
        </button>

        <!-- Share to X -->
        <button class="vpm-share-btn vpm-x-btn" :disabled="!hasVideo || sharing" @click="handleShareX">
          <X class="vpm-btn-icon" />
          <span>{{ t('generate.shareToX') }}</span>
        </button>

        <!-- Share to Instagram -->
        <button class="vpm-share-btn vpm-ins-btn" :disabled="!hasVideo" @click="handleShareIns">
          <Camera class="vpm-btn-icon" />
          <span>{{ insCopied ? t('generate.copied') : t('generate.shareToIns') }}</span>
        </button>

        <!-- Share to TikTok -->
        <button class="vpm-share-btn vpm-tiktok-btn" :disabled="!hasVideo" @click="handleShareTiktok">
          <Music2 class="vpm-btn-icon" />
          <span>{{ tiktokCopied ? t('generate.copied') : t('generate.shareToTiktok') }}</span>
        </button>
      </div>
    </div>
  </a-modal>
</template>

<style scoped>
.vpm-layout {
  display: flex;
  min-height: 360px;
  border-radius: 16px;
  overflow: hidden;
  /* 直接使用侧边栏渲染后的视觉色值（rgba(30,25,25,0.25)叠加暖棕页面背景≈rgb(97,74,51)） */
  background: rgba(55, 42, 28, 0.88);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 20px rgba(251, 146, 60, 0.08);
}

.vpm-video-area {
  flex: 1 1 65%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  overflow: hidden;
}

.vpm-video {
  width: 100%;
  height: 100%;
  max-height: 70vh;
  object-fit: contain;
  display: block;
}

.vpm-no-video {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.vpm-share-area {
  flex: 0 0 220px;
  padding: 24px 20px;
  background: rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-left: 1px solid rgba(255, 255, 255, 0.07);
}

.vpm-share-title {
  font-size: 11px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.35);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0 0 4px;
}

.vpm-share-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  white-space: nowrap;
}

.vpm-share-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.vpm-share-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.vpm-x-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.28);
}

.vpm-ins-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, rgba(254,95,1,0.2), rgba(193,53,132,0.2));
  border-color: rgba(193, 53, 132, 0.45);
}

.vpm-tiktok-btn:hover:not(:disabled) {
  background: rgba(0, 240, 200, 0.08);
  border-color: rgba(0, 240, 200, 0.3);
}

.vpm-btn-icon {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

/* We manage our own rounded layout and background so remove all Ant borders */
:deep(.ant-modal-content) {
  padding: 0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  background: transparent !important;
  box-shadow: none !important;
}
:deep(.ant-modal-body) {
  padding: 0 !important;
}
:deep(.ant-modal-close) {
  color: rgba(255, 255, 255, 0.45);
  top: 10px;
  inset-inline-end: 10px;
  z-index: 100;
}
:deep(.ant-modal-close:hover) {
  color: white;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
}

/* Responsive: mobile stack */
@media (max-width: 640px) {
  .vpm-layout {
    flex-direction: column;
  }

  .vpm-share-area {
    flex: none;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  .vpm-share-title {
    width: 100%;
  }

  .vpm-share-btn {
    flex: 1 1 calc(50% - 4px);
    min-width: 120px;
  }
}
</style>

<style>
/* Global styles for the video modal wrapper to remove all Ant design background layers */
.glass-video-modal .ant-modal-content {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
}
</style>
