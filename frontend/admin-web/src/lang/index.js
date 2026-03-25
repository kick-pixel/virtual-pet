import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import koKR from './ko-KR'

// 从 localStorage 读取语言设置，默认英文
const savedLanguage = localStorage.getItem('language') || 'en-US'

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: savedLanguage,
  fallbackLocale: 'en-US',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
    'ko-KR': koKR
  }
})

export default i18n
