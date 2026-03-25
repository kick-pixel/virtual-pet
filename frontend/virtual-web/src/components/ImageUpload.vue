<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { X } from 'lucide-vue-next'

const { t } = useI18n()

const props = defineProps<{
  maxSize?: number // MB
  maxCount?: number
  accept?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', files: File[]): void
  (e: 'upload', file: File): void
  (e: 'remove', index: number): void
}>()

const maxSizeInMB = props.maxSize || 10
const maxCount = props.maxCount || 1
const acceptTypes = props.accept || 'image/jpeg,image/png,image/webp'

const files = ref<File[]>([])
const previews = ref<string[]>([])
const isDragging = ref(false)
const error = ref('')

const canUploadMore = computed(() => files.value.length < maxCount)

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files) {
    addFiles(Array.from(input.files))
  }
}

function handleDrop(event: DragEvent) {
  isDragging.value = false
  if (event.dataTransfer?.files) {
    addFiles(Array.from(event.dataTransfer.files))
  }
}

function addFiles(newFiles: File[]) {
  error.value = ''

  // Check count
  if (files.value.length >= maxCount) {
    error.value = t('generate.uploadMaxCount', { max: maxCount })
    return
  }

  for (const file of newFiles) {
    // Check type
    if (!acceptTypes.split(',').some(type => file.type === type.trim())) {
      error.value = t('generate.uploadInvalidType')
      continue
    }

    // Check size
    if (file.size > maxSizeInMB * 1024 * 1024) {
      error.value = t('generate.uploadTooLarge', { max: maxSizeInMB })
      continue
    }

    // Add file
    files.value.push(file)

    // Create preview
    const reader = new FileReader()
    reader.onload = (e) => {
      previews.value.push(e.target?.result as string)
    }
    reader.readAsDataURL(file)

    // Emit upload event
    emit('upload', file)

    // Stop if reached max count
    if (files.value.length >= maxCount) break
  }

  emit('update:modelValue', files.value)
}

function removeFile(index: number) {
  files.value.splice(index, 1)
  previews.value.splice(index, 1)
  error.value = ''
  emit('remove', index)
  emit('update:modelValue', files.value)
}

function handleDragEnter() {
  if (canUploadMore.value) {
    isDragging.value = true
  }
}

function handleDragLeave() {
  isDragging.value = false
}

function triggerFileInput() {
  const input = document.getElementById('file-input') as HTMLInputElement
  input?.click()
}
</script>

<template>
  <a-space direction="vertical" :size="16" style="width: 100%;">
    <!-- Upload Area -->
    <div
      v-if="canUploadMore"
      class="upload-area"
      :class="{ 'dragging': isDragging }"
      @dragenter.prevent="handleDragEnter"
      @dragover.prevent
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
    >
      <input
        id="file-input"
        type="file"
        :accept="acceptTypes"
        :multiple="maxCount > 1"
        style="display: none;"
        @change="handleFileSelect"
      />

      <div
        style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 48px 24px; cursor: pointer;"
        @click="triggerFileInput"
      >
        <div style="margin-bottom: 16px; font-size: 60px;">
          {{ isDragging ? '📥' : '📸' }}
        </div>
        <p style="font-size: 18px; font-weight: 500; color: #374151; margin: 0 0 8px;">
          {{ t('generate.uploadTitle') }}
        </p>
        <p style="font-size: 14px; color: #6b7280; text-align: center; margin: 0;">
          {{ t('generate.uploadHint') }}
        </p>
        <p style="font-size: 12px; color: #9ca3af; margin: 8px 0 0;">
          {{ t('generate.uploadMaxSize', { max: maxSizeInMB }) }}
        </p>
      </div>
    </div>

    <!-- Error Message -->
    <a-alert
      v-if="error"
      type="error"
      :message="error"
      closable
      @close="error = ''"
    />

    <!-- Preview Grid -->
    <a-row v-if="files.length > 0" :gutter="[16, 16]">
      <a-col
        v-for="(preview, index) in previews"
        :key="index"
        :xs="12"
        :sm="8"
      >
        <div
          class="preview-card"
          style="position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; border: 1px solid #e5e7eb;"
        >
          <img
            :src="preview"
            :alt="`Preview ${index + 1}`"
            style="width: 100%; height: 100%; object-fit: cover;"
          />
          <div class="preview-overlay" style="position: absolute; inset: 0; background: rgba(0,0,0,0.5); opacity: 0; transition: opacity 0.3s; display: flex; align-items: center; justify-content: center;">
            <button
              @click="removeFile(index)"
              type="button"
              class="remove-btn"
            >
              <X style="width: 20px; height: 20px;" />
            </button>
          </div>
          <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(to top, rgba(0,0,0,0.6), transparent); padding: 8px;">
            <p style="font-size: 12px; color: white; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ files[index]?.name }}</p>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-space>
</template>

<style scoped>
.upload-area {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 16px;
  transition: all 0.3s;
  background: transparent;
}

.upload-area:hover {
  border-color: #a78bfa;
}

.upload-area.dragging {
  border-color: #7c3aed;
  background: rgba(124, 58, 237, 0.05);
}

.preview-card:hover .preview-overlay {
  opacity: 1;
}

.remove-btn {
  padding: 8px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-btn:hover {
  background: #dc2626;
}
</style>
