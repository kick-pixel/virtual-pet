<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps<{
  videoUrl: string
  duration?: number // 视频时长，默认5秒
}>()

const emit = defineEmits<{
  (e: 'ended'): void
}>()

const showSplash = ref(true)
const videoRef = ref<HTMLVideoElement>()

onMounted(() => {
  // 检查是否已经播放过（使用 sessionStorage，刷新后会重新播放）
  const hasPlayed = sessionStorage.getItem('splash-video-played')
  if (hasPlayed) {
    showSplash.value = false
    emit('ended')
    return
  }

  // 自动播放视频
  if (videoRef.value) {
    videoRef.value.play().catch(err => {
      console.error('Video autoplay failed:', err)
      // 如果自动播放失败，直接跳过
      handleVideoEnd()
    })
  }
})

function handleVideoEnd() {
  sessionStorage.setItem('splash-video-played', 'true')
  showSplash.value = false
  emit('ended')
}

function skipVideo() {
  handleVideoEnd()
}
</script>

<template>
  <Transition name="fade">
    <div
      v-if="showSplash"
      style="position: fixed; inset: 0; z-index: 9999; background: black; display: flex; align-items: center; justify-content: center;"
    >
      <!-- Video -->
      <video
        ref="videoRef"
        :src="videoUrl"
        style="width: 100%; height: 100%; object-fit: cover;"
        muted
        playsinline
        @ended="handleVideoEnd"
      />

      <!-- Skip Button -->
      <button
        @click="skipVideo"
        class="skip-btn"
      >
        Skip
      </button>
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.skip-btn {
  position: absolute;
  top: 32px;
  right: 32px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 14px;
  border-radius: 8px;
  backdrop-filter: blur(8px);
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.skip-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
