<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import { useCreditsStore } from '@/store/credits'
import { uploadImage, createTask, getOptions } from '@/api/task'
import { estimateCost } from '@/api/credits'
import { formatCredits } from '@/utils/format'
import ImageUpload from '@/components/ImageUpload.vue'
import RechargeModal from '@/components/RechargeModal.vue'
import { Sparkles, Diamond, Image, Settings2, FileText, Loader2, AlertCircle } from 'lucide-vue-next'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const creditsStore = useCreditsStore()

interface GenerationOption {
  optionId: number
  optionGroup: string
  optionCode: string
  optionName: string
  optionValue: string
  creditsModifier: number
  isDefault: number
  isPremium: number
  sortOrder: number
}

const optionsMap = ref<Record<string, GenerationOption[]>>({})
const optionsLoading = ref(false)

const form = ref({
  prompt: '',
  requirements: '',
  duration: '5',
  resolution: '720p',
  aspectRatio: '16:9'
})

const uploadedFiles = ref<File[]>([])
const uploadedImageUrl = ref('')
const uploadedFileId = ref<string | number>('')
const uploading = ref(false)
const creating = ref(false)
const errorMsg = ref('')
const estimatedCredits = ref(0)
const showRechargeModal = ref(false)

const promptLength = computed(() => form.value.prompt.length)
const isPromptValid = computed(() => promptLength.value > 0 && promptLength.value <= 1000)
const hasValidInput = computed(() => form.value.prompt.trim() || uploadedFiles.value.length > 0)

function getFormValue(group: string): string {
  return (form.value as Record<string, any>)[group] ?? ''
}
function setFormValue(group: string, value: string) {
  (form.value as Record<string, any>)[group] = value
}

async function loadOptions() {
  optionsLoading.value = true
  try {
    const res: any = await getOptions()
    const data = res.data || res
    if (data && typeof data === 'object') {
      optionsMap.value = data
      for (const [group, options] of Object.entries(data) as [string, GenerationOption[]][]) {
        const defaultOpt = options.find((o: GenerationOption) => o.isDefault === 1)
        const formKey = group as keyof typeof form.value
        if (defaultOpt && formKey in form.value && formKey !== 'prompt' && formKey !== 'requirements') {
          ;(form.value as any)[formKey] = defaultOpt.optionValue
        }
      }
    }
  } catch (e) {
    console.error('Failed to load generation options:', e)
  } finally {
    optionsLoading.value = false
  }
}

watch(() => [form.value.duration, form.value.resolution, form.value.aspectRatio], async () => {
  try {
    const res: any = await estimateCost({
      duration: Number(form.value.duration),
      resolution: form.value.resolution,
      aspectRatio: form.value.aspectRatio
    })
    estimatedCredits.value = res.data?.cost || res.cost || 0
  } catch {
    estimatedCredits.value = 0
  }
}, { immediate: true })

onMounted(() => {
  creditsStore.fetchBalance()
  loadOptions()
})

async function handleImageUpload(file: File) {
  if (uploadedImageUrl.value) return

  uploading.value = true
  errorMsg.value = ''
  try {
    const res: any = await uploadImage(file)
    uploadedImageUrl.value = res.data?.url || res.url || ''
    uploadedFileId.value = res.data?.fileId || res.fileId || ''
  } catch (e: any) {
    errorMsg.value = t('generate.uploadFailed')
    uploadedFiles.value = []
  } finally {
    uploading.value = false
  }
}

function handleImageRemove() {
  uploadedImageUrl.value = ''
  uploadedFileId.value = ''
}

function validate(): boolean {
  errorMsg.value = ''

  if (!userStore.token) {
    errorMsg.value = 'Please login first'
    setTimeout(() => {
      router.push('/')
    }, 1500)
    return false
  }

  if (!hasValidInput.value) {
    errorMsg.value = t('generate.validationPromptOrImage')
    return false
  }

  if (form.value.prompt && form.value.prompt.length > 1000) {
    errorMsg.value = t('generate.validationPromptTooLong')
    return false
  }

  if (estimatedCredits.value > creditsStore.balance) {
    errorMsg.value = t('generate.insufficientCredits')
    return false
  }

  return true
}

async function handleCreate() {
  if (!validate()) return

  if (uploadedFiles.value.length > 0 && !uploadedImageUrl.value && uploadedFiles.value[0]) {
    await handleImageUpload(uploadedFiles.value[0])
    if (!uploadedImageUrl.value) return
  }

  creating.value = true
  errorMsg.value = ''

  try {
    const payload: any = {
      prompt: form.value.prompt || undefined,
      requirements: form.value.requirements || undefined,
      duration: Number(form.value.duration),
      resolution: form.value.resolution,
      aspectRatio: form.value.aspectRatio
    }

    if (uploadedFileId.value) {
      payload.fileId = uploadedFileId.value
    }
    if (uploadedImageUrl.value) {
      payload.imageUrl = uploadedImageUrl.value
    }

    const res: any = await createTask(payload)

    const taskId = res.data?.taskId || res.taskId
    if (taskId) {
      router.push(`/generate/progress/${taskId}`)
    }
  } catch (e: any) {
    errorMsg.value = t('generate.createFailed')
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div style="max-width: 1536px; margin: 0 auto; padding: 32px 16px;">
    <div style="margin-bottom: 32px;">
      <h1 style="font-size: 30px; font-weight: 700; margin: 0 0 8px;">{{ t('generate.title') }}</h1>
      <p style="color: #4b5563; margin: 0;">Create stunning AI-generated pet videos</p>
    </div>

    <a-row :gutter="32">
      <!-- Left Column: Main Form (2/3) -->
      <a-col :xs="24" :lg="16">
        <a-space direction="vertical" :size="24" style="width: 100%;">
          <!-- Prompt Input -->
          <a-card>
            <template #title>
              <div style="display: flex; align-items: center; gap: 8px;">
                <Sparkles style="height: 20px; width: 20px; color: #3b82f6;" />
                {{ t('generate.promptTitle') }}
              </div>
            </template>
            <template #extra>
              <div style="font-size: 12px; color: #6b7280;">Describe your pet video idea in detail</div>
            </template>
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
              <span style="font-size: 12px; color: #6b7280;">Characters</span>
              <span style="font-size: 12px;" :style="{ color: promptLength > 1000 ? '#ef4444' : '#6b7280' }">
                {{ promptLength }} / 1000
              </span>
            </div>
            <a-textarea
              v-model="form.prompt"
              :placeholder="t('generate.promptPlaceholder')"
              :rows="6"
              :maxlength="1000"
              style="width: 100%;"
            />
            <p style="margin: 8px 0 0; font-size: 12px; color: #6b7280;">
              {{ t('generate.promptHint') }}
            </p>
          </a-card>

          <!-- Image Upload -->
          <a-card>
            <template #title>
              <div style="display: flex; align-items: center; gap: 8px;">
                <Image style="height: 20px; width: 20px; color: #3b82f6;" />
                {{ t('generate.uploadTitle') }}
              </div>
            </template>
            <template #extra>
              <div style="font-size: 12px; color: #6b7280;">Upload a reference image (optional)</div>
            </template>
            <ImageUpload
              v-model="uploadedFiles"
              :max-size="10"
              :max-count="1"
              accept="image/jpeg,image/png,image/webp"
              @upload="handleImageUpload"
              @remove="handleImageRemove"
            />
          </a-card>

          <!-- Video Requirements -->
          <a-card>
            <template #title>
              <div style="display: flex; align-items: center; gap: 8px;">
                <FileText style="height: 20px; width: 20px; color: #3b82f6;" />
                {{ t('generate.requirementsTitle') }}
              </div>
            </template>
            <template #extra>
              <div style="font-size: 12px; color: #6b7280;">Additional requirements for your video</div>
            </template>
            <a-textarea
              v-model="form.requirements"
              :placeholder="t('generate.requirementsPlaceholder')"
              :rows="3"
              style="width: 100%;"
            />
          </a-card>

          <!-- Video Specifications -->
          <a-card>
            <template #title>
              <div style="display: flex; align-items: center; gap: 8px;">
                <Settings2 style="height: 20px; width: 20px; color: #3b82f6;" />
                {{ t('generate.options') }}
              </div>
            </template>
            <template #extra>
              <div style="font-size: 12px; color: #6b7280;">Customize your video settings</div>
            </template>
            <div v-if="optionsLoading" style="display: flex; justify-content: center; padding: 32px 0;">
              <a-skeleton :active="true" />
            </div>

            <div v-else>
              <a-space direction="vertical" :size="20" style="width: 100%;">
                <div v-for="(options, group) in optionsMap" :key="group">
                  <label style="display: block; margin-bottom: 8px; font-size: 14px; font-weight: 500;">
                    {{ t(`generate.${group}`) }}
                  </label>
                  <a-space wrap :size="12">
                    <a-button
                      v-for="opt in options"
                      :key="opt.optionId"
                      :type="getFormValue(group) === opt.optionValue ? 'primary' : 'default'"
                      size="small"
                      @click="setFormValue(group, opt.optionValue)"
                      style="min-width: 80px;"
                    >
                      {{ opt.optionName }}
                      <a-tag v-if="opt.isPremium === 1" size="small" color="gold" style="margin-left: 4px;">PRO</a-tag>
                    </a-button>
                  </a-space>
                </div>
              </a-space>
            </div>
          </a-card>
        </a-space>
      </a-col>

      <!-- Right Column: Summary (1/3) -->
      <a-col :xs="24" :lg="8">
        <div style="position: sticky; top: 96px;">
          <a-card>
            <template #title>
              <div style="font-size: 16px;">Summary</div>
            </template>
            <a-space direction="vertical" :size="16" style="width: 100%;">
              <a-space direction="vertical" :size="12" style="width: 100%; font-size: 14px;">
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: #4b5563;">{{ t('generate.duration') }}</span>
                  <span style="font-weight: 500;">{{ form.duration }}s</span>
                </div>
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: #4b5563;">{{ t('generate.resolution') }}</span>
                  <span style="font-weight: 500;">{{ form.resolution }}</span>
                </div>
                <div style="display: flex; justify-content: space-between;">
                  <span style="color: #4b5563;">{{ t('generate.aspectRatio') }}</span>
                  <span style="font-weight: 500;">{{ form.aspectRatio }}</span>
                </div>

                <a-divider style="margin: 4px 0;" />

                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="color: #4b5563;">{{ t('generate.estimatedCost') }}</span>
                  <a-tag color="blue" style="font-size: 15px; padding: 4px 12px;">
                    <Diamond style="height: 12px; width: 12px; margin-right: 4px;" />
                    {{ formatCredits(estimatedCredits) }}
                  </a-tag>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="color: #4b5563;">{{ t('generate.currentBalance') }}</span>
                  <span style="font-weight: 500;" :style="{ color: creditsStore.balance >= estimatedCredits ? '#22c55e' : '#ef4444' }">
                    {{ formatCredits(creditsStore.balance) }}
                  </span>
                </div>
              </a-space>

              <a-alert v-if="errorMsg" type="error" show-icon>
                {{ errorMsg }}
              </a-alert>

              <a-button
                type="primary"
                size="large"
                block
                @click="handleCreate"
                :loading="creating || uploading"
                :disabled="!hasValidInput"
              >
                <template #icon>
                  <Loader2 v-if="creating || uploading" style="height: 16px; width: 16px;" class="spinning-icon" />
                  <Sparkles v-else style="height: 16px; width: 16px;" />
                </template>
                {{ (creating || uploading) ? t('common.loading') : t('generate.createTask') }}
              </a-button>

              <a-button
                v-if="estimatedCredits > creditsStore.balance"
                size="large"
                block
                @click="showRechargeModal = true"
              >
                {{ t('nav.recharge') }}
              </a-button>
            </a-space>
          </a-card>
        </div>
      </a-col>
    </a-row>

    <RechargeModal :visible="showRechargeModal" @update:visible="showRechargeModal = $event" />
  </div>
</template>

<style scoped>
textarea {
  font-family: inherit;
}

textarea:focus {
  outline: none;
}

.spinning-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
