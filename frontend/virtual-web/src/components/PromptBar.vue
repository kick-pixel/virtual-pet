<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { uploadImage } from '@/api/file'
import { createTask, getGenerationOptions } from '@/api/task'
import { message } from 'ant-design-vue'
import { ImagePlus, SlidersHorizontal, ArrowUp, X as XIcon, Loader2, Zap, Gift } from 'lucide-vue-next'
import CheckinModal from '@/components/CheckinModal.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const promptText = ref('')
const showCheckinModal = ref(false)

// Image upload
const fileInput = ref<HTMLInputElement | null>(null)
const uploadedImageUrl = ref('')
const uploadedFileId = ref<string | number>('')
const uploading = ref(false)

// Options panel
const showOptions = ref(false)
const optionsData = ref<any>({})
const durationOptions = computed(() => optionsData.value.duration || [])
const resolutionOptions = computed(() => optionsData.value.resolution || [])
const aspectRatioOptions = computed(() => optionsData.value.aspectRatio || [])
const selectedDuration = ref('')
const selectedResolution = ref('')
const selectedAspectRatio = ref('')

const submitting = ref(false)

async function fetchOptions() {
  try {
    const res: any = await getGenerationOptions()
    const data = res.data || res
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      optionsData.value = data
    } else if (Array.isArray(data)) {
      const grouped: any = {}
      data.forEach((opt: any) => {
        const group = opt.optionGroup || 'other'
        if (!grouped[group]) grouped[group] = []
        grouped[group].push(opt)
      })
      optionsData.value = grouped
    }
    if (durationOptions.value.length > 0 && !selectedDuration.value)
      selectedDuration.value = String(durationOptions.value[0].optionValue)
    if (resolutionOptions.value.length > 0 && !selectedResolution.value)
      selectedResolution.value = String(resolutionOptions.value[0].optionValue)
    if (aspectRatioOptions.value.length > 0 && !selectedAspectRatio.value)
      selectedAspectRatio.value = String(aspectRatioOptions.value[0].optionValue)
  } catch (e) {
    console.error('Failed to fetch options:', e)
  }
}

function handleImageClick() {
  if (!userStore.isLoggedIn()) {
    userStore.showLoginModal()
    return
  }
  fileInput.value?.click()
}

async function handleImageChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    message.error('请上传图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片不能超过 10MB')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res: any = await uploadImage(formData)
    uploadedImageUrl.value = res.data?.url || res.url
    uploadedFileId.value = res.data?.fileId || res.fileId || ''
    message.success(t('showcase.uploadSuccess'))
  } catch {
    message.error('上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function removeImage() {
  uploadedImageUrl.value = ''
  uploadedFileId.value = ''
}

// Aspect ratio visual preview (filled square, Grok style)
function getAspectVisualStyle(ratioStr: string) {
  const parts = String(ratioStr).split(':')
  const w = parseFloat(parts[0] || '1')
  const h = parseFloat(parts[1] || '1')
  const maxDim = 38
  if (w >= h) {
    return { width: `${maxDim}px`, height: `${Math.max(14, Math.round(maxDim * h / w))}px` }
  }
  return { width: `${Math.max(14, Math.round(maxDim * w / h))}px`, height: `${maxDim}px` }
}

async function handleSubmit() {
  if (!promptText.value.trim()) {
    message.warning('请输入描述词后再生成视频')
    return
  }
  if (!userStore.isLoggedIn()) {
    userStore.showLoginModal()
    return
  }
  submitting.value = true
  try {
    const payload: any = {
      prompt: promptText.value,
      duration: selectedDuration.value ? parseInt(selectedDuration.value) : null,
      resolution: selectedResolution.value || null,
      aspectRatio: selectedAspectRatio.value || null
    }
    if (uploadedFileId.value) payload.fileId = uploadedFileId.value
    if (uploadedImageUrl.value) payload.imageUrl = uploadedImageUrl.value

    const res: any = await createTask(payload)
    const taskId = res.taskId || res.data?.taskId
    if (taskId) {
      promptText.value = ''
      uploadedImageUrl.value = ''
      uploadedFileId.value = ''
      showOptions.value = false
      const queryFrom = typeof route.query.from === 'string' ? route.query.from : ''
      const fromPath = queryFrom || (route.path.startsWith('/showcase/post/') ? '/showcase' : route.fullPath)
      router.push({
        path: `/showcase/post/${taskId}`,
        query: { from: fromPath },
        state: { from: fromPath }
      })
    }
  } catch (e: any) {
    message.error(e.message || '创建任务失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOptions()
})
</script>

<template>
  <div class="pb-fixed-wrapper">
    <div class="pb-inner">
      <div class="pb-bar-wrapper">
        <!-- Options panel (floats above bar as card) -->
        <Transition name="pb-slide">
          <div v-if="showOptions" class="pb-options-panel">
            <!-- Duration -->
            <div v-if="durationOptions.length > 0" class="opt-group">
              <span class="opt-label">{{ t('showcase.duration') }}</span>
              <div class="opt-chips">
                <button
                  v-for="opt in durationOptions"
                  :key="opt.optionValue"
                  class="opt-chip"
                  :class="{ active: selectedDuration === String(opt.optionValue) }"
                  @click="selectedDuration = String(opt.optionValue)"
                >{{ opt.optionValue }}s</button>
              </div>
            </div>
            <!-- Resolution -->
            <div v-if="resolutionOptions.length > 0" class="opt-group">
              <span class="opt-label">{{ t('showcase.resolution') }}</span>
              <div class="opt-chips">
                <button
                  v-for="opt in resolutionOptions"
                  :key="opt.optionValue"
                  class="opt-chip"
                  :class="{ active: selectedResolution === String(opt.optionValue) }"
                  @click="selectedResolution = String(opt.optionValue)"
                >{{ opt.optionName }}</button>
              </div>
            </div>
            <!-- Aspect Ratio with visual indicators -->
            <div v-if="aspectRatioOptions.length > 0" class="opt-group">
              <span class="opt-label">{{ t('showcase.aspectRatio') }}</span>
              <div class="opt-chips">
                <button
                  v-for="opt in aspectRatioOptions"
                  :key="opt.optionValue"
                  class="opt-chip aspect-chip"
                  :class="{ active: selectedAspectRatio === String(opt.optionValue) }"
                  @click="selectedAspectRatio = String(opt.optionValue)"
                >
                  <span class="aspect-visual" :style="getAspectVisualStyle(String(opt.optionValue))"></span>
                  <span class="aspect-label">{{ opt.optionName }}</span>
                </button>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Daily Reward button (above bar, right-aligned, hidden when options open) -->
        <button
          v-show="!showOptions"
          class="pb-checkin-btn"
          @click="userStore.isLoggedIn() ? (showCheckinModal = true) : userStore.showLoginModal()"
        >
          <Gift style="width: 13px; height: 13px; flex-shrink: 0;" />
          <span>{{ t('checkin.headerBtn') }}</span>
          <span>›</span>
        </button>

        <!-- Main bar -->
        <div class="pb-bar">
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none;"
            @change="handleImageChange"
          />

          <!-- Image upload button -->
          <button class="pb-img-btn" @click="handleImageClick" :title="t('showcase.uploadReference')">
            <template v-if="uploadedImageUrl">
              <img :src="uploadedImageUrl" class="pb-thumb" />
              <span class="pb-thumb-remove" @click.stop="removeImage">
                <XIcon style="width: 9px; height: 9px;" />
              </span>
            </template>
            <Loader2 v-else-if="uploading" class="pb-spin" style="width: 18px; height: 18px; color: #e07010;" />
            <ImagePlus v-else style="width: 18px; height: 18px; color: rgba(60,38,12,0.55);" />
          </button>

          <!-- Prompt textarea -->
          <textarea
            v-model="promptText"
            class="pb-textarea"
            :placeholder="t('generate.promptPlaceholder')"
            rows="1"
            @keydown.enter.exact.prevent="handleSubmit"
          />

          <!-- Cost badge -->
          <div class="pb-cost-badge">
            <Zap style="width: 11px; height: 11px; flex-shrink: 0;" />
            <span>30</span>
          </div>

          <!-- Options toggle -->
          <button
            class="pb-icon-btn"
            :class="{ 'pb-icon-btn--active': showOptions }"
            @click="showOptions = !showOptions"
            :title="t('showcase.advancedOptions')"
          >
            <SlidersHorizontal style="width: 17px; height: 17px;" />
          </button>

          <!-- Send button -->
          <button
            class="pb-send-btn"
            :disabled="!promptText.trim() || submitting"
            @click="handleSubmit"
          >
            <Loader2 v-if="submitting" class="pb-spin" style="width: 17px; height: 17px;" />
            <ArrowUp v-else style="width: 17px; height: 17px;" />
          </button>
        </div>
      </div>
    </div>

    <CheckinModal
      :open="showCheckinModal"
      @update:open="showCheckinModal = $event"
    />
  </div>
</template>

<style scoped>
/* 固定在底部，无背景遮罩 */
.pb-fixed-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 200;
}

.pb-inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 16px 20px;
}

/* 关键：作为 options panel 绝对定位的参照系 */
.pb-bar-wrapper {
  position: relative;
}

/* Daily Reward button - 输入框右上方 */
.pb-checkin-btn {
  position: absolute;
  bottom: calc(100% + 8px);
  right: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 12px;
  border-radius: 16px;
  border: 1.5px solid rgba(212, 137, 90, 0.45);
  background: rgba(253, 248, 240, 0.94);
  color: #c06008;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  backdrop-filter: blur(12px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.pb-checkin-btn:hover {
  background: rgba(212, 137, 90, 0.12);
  border-color: #d4895a;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(212, 137, 90, 0.28);
}

/* Options panel - 暖米白浮动卡片，与 Header 风格统一 */
.pb-options-panel {
  position: absolute;
  bottom: calc(100% + 8px);
  right: 0;
  width: max-content;
  max-width: 100%;
  background: rgba(253, 248, 240, 0.98);
  backdrop-filter: blur(32px);
  border: 1px solid rgba(190, 155, 95, 0.25);
  border-radius: 18px;
  padding: 20px 22px 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.18), 0 8px 32px rgba(0, 0, 0, 0.12);
}

/* 每组：标题在上，选项在下 */
.opt-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.opt-label {
  font-size: 13px;
  font-weight: 700;
  color: rgba(45, 28, 8, 0.82);
  white-space: nowrap;
}

.opt-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 时长/分辨率选项 */
.opt-chip {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid rgba(100, 65, 20, 0.18);
  background: rgba(100, 65, 20, 0.06);
  color: rgba(60, 38, 12, 0.6);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  line-height: 1.5;
  display: flex;
  align-items: center;
}

.opt-chip:hover {
  background: rgba(100, 65, 20, 0.1);
  border-color: rgba(100, 65, 20, 0.28);
  color: rgba(45, 28, 8, 0.88);
}

.opt-chip.active {
  background: rgba(224, 112, 16, 0.12);
  border-color: rgba(224, 112, 16, 0.5);
  color: #c06008;
}

/* 宽高比 chip - 竖排：实心矩形 + 标签 */
.aspect-chip {
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 10px 12px;
  min-width: 52px;
  border-radius: 12px;
}

/* 实心矩形（默认暖灰色填充） */
.aspect-visual {
  background: rgba(80, 55, 20, 0.22);
  border-radius: 3px;
  flex-shrink: 0;
  border: none;
  transition: background 0.15s;
}

/* 激活时矩形变为深暖棕色 */
.aspect-chip.active .aspect-visual {
  background: rgba(45, 28, 8, 0.82);
}

.aspect-label {
  font-size: 10px;
  line-height: 1;
  font-weight: 700;
  color: rgba(60, 38, 12, 0.65);
}

.aspect-chip.active .aspect-label {
  color: rgba(45, 28, 8, 0.92);
}

/* Main bar - 暖米白，与 panel 风格统一 */
.pb-bar {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: rgba(253, 248, 240, 0.97);
  backdrop-filter: blur(32px);
  border: 1px solid rgba(190, 155, 95, 0.3);
  border-radius: 20px;
  padding: 10px 10px 10px 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.18), 0 1px 0 rgba(255, 255, 255, 0.8) inset;
}

/* Image upload button */
.pb-img-btn {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid rgba(100, 65, 20, 0.18);
  background: rgba(100, 65, 20, 0.06);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  transition: all 0.2s;
}

.pb-img-btn:hover {
  background: rgba(100, 65, 20, 0.1);
  border-color: rgba(100, 65, 20, 0.25);
}

.pb-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 9px;
}

.pb-thumb-remove {
  position: absolute;
  top: 1px;
  right: 1px;
  width: 15px;
  height: 15px;
  background: rgba(0, 0, 0, 0.65);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  line-height: 1;
}

/* Textarea - 深色文字适配暖米白背景 */
.pb-textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: rgba(45, 28, 8, 0.9);
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  min-height: 38px;
  max-height: 120px;
  padding: 8px 4px;
  font-family: inherit;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(100, 65, 20, 0.2) transparent;
  align-self: center;
}

.pb-textarea::placeholder {
  color: rgba(100, 65, 20, 0.35);
}

/* Cost badge */
.pb-cost-badge {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  height: 38px;
  padding: 0 12px;
  border-radius: 10px;
  background: rgba(224, 112, 16, 0.08);
  border: 1px solid rgba(224, 112, 16, 0.22);
  color: #c06008;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
  user-select: none;
}

/* Options icon button */
.pb-icon-btn {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: 1px solid rgba(100, 65, 20, 0.18);
  background: rgba(100, 65, 20, 0.06);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(60, 38, 12, 0.45);
  transition: all 0.2s;
}

.pb-icon-btn:hover {
  background: rgba(100, 65, 20, 0.1);
  border-color: rgba(100, 65, 20, 0.25);
  color: rgba(45, 28, 8, 0.72);
}

.pb-icon-btn--active {
  background: rgba(224, 112, 16, 0.12) !important;
  border-color: rgba(224, 112, 16, 0.4) !important;
  color: #c06008 !important;
}

/* Send button */
.pb-send-btn {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #e07010 0%, #f5a020 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 12px rgba(224, 112, 16, 0.4);
}

.pb-send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #c86010 0%, #e09010 100%);
  box-shadow: 0 4px 18px rgba(224, 112, 16, 0.55);
  transform: scale(1.06);
}

.pb-send-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  transform: none;
}

/* Spin animation */
.pb-spin {
  animation: pb-spin-kf 1s linear infinite;
}

@keyframes pb-spin-kf {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Slide-up transition（从下方滑入面板） */
.pb-slide-enter-active,
.pb-slide-leave-active {
  transition: all 0.2s ease;
}

.pb-slide-enter-from,
.pb-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* Mobile */
@media (max-width: 640px) {
  .pb-options-panel {
    padding: 16px;
    gap: 14px;
  }
  .aspect-chip {
    min-width: 44px;
    padding: 8px 10px;
  }
}
</style>
