<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from 'lucide-vue-next'

const { t } = useI18n()
const router = useRouter()

function handleStartFree() {
  router.push('/showcase')
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section
      style="position: relative; min-height: 100vh; display: flex; align-items: center; justify-content: center; overflow: hidden; background: transparent;"
    >
      <!-- Animated Background -->
      <div style="position: absolute; inset: 0; opacity: 0.3;">
        <div class="blob blob-1" style="position: absolute; top: 80px; left: 40px; width: 384px; height: 384px; border-radius: 50%; background: #fef3c7; filter: blur(100px);"></div>
        <div class="blob blob-2" style="position: absolute; top: 160px; right: 80px; width: 384px; height: 384px; border-radius: 50%; background: #fbbf24; filter: blur(100px); animation-delay: 2s;"></div>
        <div class="blob blob-3" style="position: absolute; bottom: 80px; left: 50%; width: 384px; height: 384px; border-radius: 50%; background: #f97316; filter: blur(100px); animation-delay: 4s;"></div>
      </div>

      <div style="position: relative; z-index: 10; max-width: 1280px; margin: 0 auto; padding: 80px 16px; text-align: center;">
        <!-- Logo/Icon -->
        <div class="hero-content" style="margin-bottom: 128px;">
          <!-- <div style="display: inline-flex; align-items: center; justify-content: center; width: 96px; height: 96px; border-radius: 50%; background: rgba(255,255,255,0.1); backdrop-filter: blur(16px); border: 1px solid rgba(255,255,255,0.3); margin-bottom: 80px;">
            <img src="/PET_1280X1280.PNG" alt="Pet AI Logo" style="width: 64px; height: 64px; object-fit: contain;" />
          </div> -->

          <h1 class="hero-title" style="font-size: 48px; font-weight: 800; color: rgba(45,28,8,0.92); margin-bottom: 64px; letter-spacing: 0.025em; line-height: 1.2;">
            {{ t('home.heroTitle') }}
          </h1>

          <p style="font-size: 20px; color: rgba(70,48,20,0.68); margin-bottom: 128px; max-width: 768px; margin-left: auto; margin-right: auto; font-weight: 500; letter-spacing: 0.025em; line-height: 1.75;">
            {{ t('home.heroSubtitle') }}
          </p>
        </div>

        <!-- CTA Buttons -->
        <div class="cta-buttons" style="display: flex; justify-content: center;">
          <a-button
            @click="handleStartFree"
            size="large"
            type="primary"
            class="primary-cta-btn"
          >
            <a-space :size="8">
              <span>{{ t('home.startFree') }}</span>
              <ArrowRight style="width: 20px; height: 20px;" />
            </a-space>
          </a-button>
        </div>

        <!-- Spacer -->
        <div style="height: 120px;"></div>

        <!-- Trust Badges -->
        <div class="trust-badges">
          <a-space :size="32" wrap style="justify-content: center; color: rgba(70,48,20,0.55); font-size: 14px;">
            <a-space :size="8">
              <span style="font-size: 20px;">⚡</span>
              <span>{{ t('home.fastGeneration') }}</span>
            </a-space>
            <a-space :size="8">
              <span style="font-size: 20px;">🎨</span>
              <span>{{ t('home.highQuality') }}</span>
            </a-space>
            <a-space :size="8">
              <span style="font-size: 20px;">🔒</span>
              <span>{{ t('home.secure') }}</span>
            </a-space>
          </a-space>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* Animations */
@keyframes blob {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.blob {
  animation: blob 7s infinite;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-content {
  animation: fadeIn 1s ease-out;
}

.cta-buttons {
  animation: fadeIn 1s ease-out 0.5s both;
}

.trust-badges {
  animation: fadeIn 1s ease-out 1s both;
}

/* CTA Button Hover Effects */
.ghost-cta-btn:hover {
  background: rgba(255,255,255,0.1) !important;
  border-color: white !important;
  transform: scale(1.05) !important;
}

/* Start Free 主按钮 - 暖杏橙渐变 */
.primary-cta-btn {
  height: 56px !important;
  padding: 0 48px !important;
  border-radius: 28px !important;
  font-size: 17px !important;
  font-weight: 600 !important;
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%) !important;
  color: #ffffff !important;
  border: none !important;
  min-width: 200px !important;
  box-shadow: 0 4px 20px rgba(212, 137, 90, 0.42) !important;
  transition: all 0.25s ease !important;
}

.primary-cta-btn:hover {
  background: linear-gradient(90deg, #c07848 0%, #d9a06a 100%) !important;
  color: #ffffff !important;
  box-shadow: 0 8px 32px rgba(212, 137, 90, 0.58) !important;
  transform: scale(1.04) translateY(-2px) !important;
}

/* Responsive title size */
@media (min-width: 768px) {
  .hero-title {
    font-size: 72px !important;
  }
}

@media (max-width: 767px) {
  .hero-title {
    font-size: 48px !important;
  }
}
</style>
