<template>
  <el-dropdown class="lang-select" @command="handleSetLanguage" trigger="hover">
    <div class="lang-select-wrapper">
      <svg-icon icon-class="language" class="lang-icon" />
      <span class="lang-text">{{ currentLangName }}</span>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item :disabled="language === 'zh-CN'" command="zh-CN">
          简体中文
        </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'en-US'" command="en-US">
          English
        </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'ko-KR'" command="ko-KR">
          한국어
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed, getCurrentInstance } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { locale } = useI18n()

const language = computed(() => locale.value)

const languageNames = {
  'zh-CN': '简体中文',
  'en-US': 'English',
  'ko-KR': '한국어'
}

const currentLangName = computed(() => languageNames[language.value] || '简体中文')

function handleSetLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)

  ElMessage.success(lang === 'zh-CN' ? '语言切换成功' : 'Language switched successfully')

  // 刷新页面使 Element Plus 语言包生效
  setTimeout(() => {
    location.reload()
  }, 500)
}
</script>

<style lang="scss" scoped>
.lang-select {
  display: inline-block;
  cursor: pointer;

  .lang-select-wrapper {
    display: flex;
    align-items: center;
    height: 50px;
    padding: 0 8px;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }

    .lang-icon {
      font-size: 18px;
      margin-right: 5px;
    }

    .lang-text {
      font-size: 14px;
    }
  }
}

/* 深色主题适配 */
html.dark .lang-select .lang-select-wrapper:hover {
  background: rgba(255, 255, 255, 0.05);
}
</style>
