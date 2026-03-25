import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import i18n from './lang'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import './assets/styles/globals.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(Antd)

import { VueQueryPlugin } from '@tanstack/vue-query'
import { WagmiPlugin } from '@wagmi/vue'
import { config } from '@/composables/useWallet'

app.use(WagmiPlugin, { config })
app.use(VueQueryPlugin, {})

app.mount('#app')
