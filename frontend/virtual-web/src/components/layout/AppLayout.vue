<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './Header.vue'
import Footer from './Footer.vue'

const route = useRoute()
const isHomePage = computed(() => route.path === '/')
const isShowcasePage = computed(() => route.path === '/showcase' || route.path.startsWith('/showcase/post/'))
const isProfilePage = computed(() => route.path === '/profile')
const isGeneratePage = computed(() => route.path.startsWith('/generate'))
const isAboutPage = computed(() => route.path === '/about')
const isFullBgPage = computed(() =>
  isHomePage.value || isShowcasePage.value || isProfilePage.value || isGeneratePage.value
)
</script>

<template>
  <div style="display: flex; min-height: 100vh; flex-direction: column; position: relative;">
    <!-- 全局视频背景（仅全屏页面显示，路由切换不重载） -->
    <template v-if="isFullBgPage">
      <video
        class="global-video-bg"
        autoplay
        muted
        loop
        playsinline
        poster="/video_bg_1.jpg"
      >
        <source src="/video_bg_1.webm" type="video/webm" />
        <source src="/video_bg_1.mp4" type="video/mp4" />
      </video>
      <div class="global-video-overlay" />
    </template>

    <Header />
    <main :style="{ flex: 1, background: isFullBgPage ? 'transparent' : 'white' }">
      <router-view />
    </main>
    <Footer v-if="!isFullBgPage && !isAboutPage" />
  </div>
</template>

<style scoped>
.global-video-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -2;
  pointer-events: none;
}

.global-video-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.10);
  z-index: -1;
  pointer-events: none;
}

@media (max-width: 768px) {
  .global-video-bg {
    display: none;
  }
  .global-video-overlay {
    background-image: url('/video_bg_1.jpg');
    background-size: cover;
    background-position: center;
    background-attachment: fixed;
  }
}
</style>
