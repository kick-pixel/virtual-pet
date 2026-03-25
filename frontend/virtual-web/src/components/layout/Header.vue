<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import { useCreditsStore } from '@/store/credits'
import { useWallet } from '@/composables/useWallet'
import { getWalletNonce, walletLogin, sendEmailCode, emailLogin, getOAuthUrl } from '@/api/auth'
import { setLocale } from '@/utils/storage'
import { AUTH_EVENTS } from '@/utils/request'
import { shortenAddress } from '@/utils/format'
import { Menu, X, User, LogOut, Key, Gift, Sparkles } from 'lucide-vue-next'
import CheckinModal from '@/components/CheckinModal.vue'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const creditsStore = useCreditsStore()
const { openModal, address, isConnected, signMessageAsync, chainId, disconnect } = useWallet()

const mobileMenuOpen = ref(false)
const isConnecting = ref(false)
const showCheckinModal = ref(false)
// Fix: only auto-login when user explicitly initiated wallet connection
const manualConnecting = ref(false)

// Email login state
const emailStep = ref<1 | 2>(1)
const emailInput = ref('')
const codeInput = ref('')
const sendingCode = ref(false)
const countdown = ref(0)
const loggingIn = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 视频背景页面（Header 切换为深色玻璃态）
const isFullBgPage = computed(() => {
  const p = router.currentRoute.value.path
  return p === '/' || p === '/showcase' || p === '/profile' || p.startsWith('/generate') || p.startsWith('/showcase/post/')
})

const navLinks = computed(() => [
  { name: t('nav.home'), path: '/' },
  { name: t('nav.showcase'), path: '/showcase' },
  { name: t('nav.about'), path: '/about' }
])

const currentPath = computed(() => router.currentRoute.value.path)

const resendBtnText = computed(() =>
  countdown.value > 0
    ? `${t('auth.resendCode')} (${countdown.value}s)`
    : t('auth.resendCode')
)

// Reset email form and view when modal closes
watch(() => userStore.loginModalVisible, (visible) => {
  if (!visible) {
    resetEmailForm()
  }
})

function handleLocaleChange(menuInfo: any) {
  locale.value = menuInfo.key
  setLocale(menuInfo.key)
}

async function handleLogout() {
  try {
    await userStore.logoutUser()
  } catch (error) {
    console.error('Logout API failed:', error)
  } finally {
    userStore.loginModalVisible = false
    await router.replace('/')
  }
}

function resetEmailForm() {
  emailStep.value = 1
  emailInput.value = ''
  codeInput.value = ''
  sendingCode.value = false
  countdown.value = 0
  loggingIn.value = false
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function startCountdown() {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function handleConnectWallet() {
  try {
    userStore.loginModalVisible = false
    await new Promise<void>(resolve => setTimeout(resolve, 200))
    manualConnecting.value = true

    // Optional: If the developer/user explicitly wants to see the wallet selection UI from fresh, 
    // we disconnect the existing lingering connection so Web3Modal won't jump into the Account view.
    if (isConnected.value && address.value && !userStore.isLoggedIn()) {
      try {
        disconnect()
        await new Promise<void>(resolve => setTimeout(resolve, 300))
      } catch (e) {
        console.error('Failed to disconnect previous wallet block:', e)
      }
    }

    await openModal()
  } catch (error) {
    console.error('Failed to open wallet modal:', error)
    message.error(t('wallet.connectFailed') || 'Failed to connect wallet')
    manualConnecting.value = false
    userStore.loginModalVisible = true
  }
}

async function handleSendCode() {
  if (!emailInput.value || !emailInput.value.includes('@')) {
    message.warning(t('auth.emailRequired') || 'Please enter a valid email')
    return
  }
  sendingCode.value = true
  try {
    await sendEmailCode(emailInput.value)
    emailStep.value = 2
    message.success(t('auth.codeSent') || 'Verification code sent!')
    startCountdown()
  } catch (e: any) {
    message.error(e?.response?.data?.msg || 'Failed to send code')
  } finally {
    sendingCode.value = false
  }
}

async function handleResendCode() {
  if (countdown.value > 0) return
  sendingCode.value = true
  try {
    await sendEmailCode(emailInput.value)
    message.success(t('auth.codeSent') || 'Verification code sent!')
    startCountdown()
  } catch (e: any) {
    message.error(e?.response?.data?.msg || 'Failed to resend code')
  } finally {
    sendingCode.value = false
  }
}

async function handleEmailLogin() {
  if (!codeInput.value) {
    message.warning(t('auth.codeRequired') || 'Please enter the verification code')
    return
  }
  loggingIn.value = true
  try {
    const res: any = await emailLogin({ email: emailInput.value, code: codeInput.value })
    const data = res.data || res
    const token = data.token || data.accessToken
    if (!token) throw new Error('No token received')
    userStore.setUserToken(token)
    await userStore.fetchProfile()
    if (creditsStore.fetchBalance) creditsStore.fetchBalance()
    message.success(t('auth.loginSuccess') || 'Login successful!')
    userStore.loginModalVisible = false
  } catch (e: any) {
    message.error(e?.response?.data?.msg || t('auth.invalidCode') || 'Login failed')
  } finally {
    loggingIn.value = false
  }
}

let oauthPopup: Window | null = null

function handleOAuthMessage(event: MessageEvent) {
  if (event.origin !== window.location.origin) return
  if (event.data?.type !== 'oauth-callback') return
  const data = event.data.data
  if (data?.token) {
    userStore.setUserToken(data.token)
    userStore.fetchProfile().then(() => {
      if (creditsStore.fetchBalance) creditsStore.fetchBalance()
    })
    message.success(t('auth.loginSuccess') || 'Login successful!')
    userStore.loginModalVisible = false
  } else {
    message.error(t('auth.oauthFailed') || 'Google login failed')
  }
  window.removeEventListener('message', handleOAuthMessage)
  if (oauthPopup && !oauthPopup.closed) oauthPopup.close()
  oauthPopup = null
}

async function handleGoogleLogin() {
  try {
    const res: any = await getOAuthUrl('google')
    const url = res?.data?.url || res?.url
    if (!url) throw new Error('No OAuth URL received')
    window.addEventListener('message', handleOAuthMessage)
    const w = 500, h = 620
    const left = Math.round(window.screenX + (window.outerWidth - w) / 2)
    const top = Math.round(window.screenY + (window.outerHeight - h) / 2)
    oauthPopup = window.open(url, 'google-oauth', `width=${w},height=${h},left=${left},top=${top}`)
  } catch (e: any) {
    message.error(e?.response?.data?.msg || t('auth.oauthFailed') || 'Google login failed')
  }
}

// Fix auto-login bug: only trigger when user explicitly connected wallet
watch([isConnected, address], async ([connected, addr]) => {
  if (connected && addr && manualConnecting.value && !isConnecting.value && !userStore.isLoggedIn()) {
    isConnecting.value = true
    try {
      await handleWalletLogin(addr as string)
    } catch (error) {
      console.error('Wallet login failed:', error)
    } finally {
      isConnecting.value = false
      manualConnecting.value = false
    }
  }
})

// Handle 401 unauthorized: reset state and prompt user to log in
function handleUnauthorized() {
  userStore.resetUser()
  userStore.showLoginModal()
}

onMounted(() => {
  window.addEventListener(AUTH_EVENTS.UNAUTHORIZED, handleUnauthorized)
})

onUnmounted(() => {
  window.removeEventListener(AUTH_EVENTS.UNAUTHORIZED, handleUnauthorized)
  window.removeEventListener('message', handleOAuthMessage)
  if (countdownTimer) clearInterval(countdownTimer)
})

async function handleWalletLogin(walletAddr: string) {
  try {
    message.loading({ content: t('wallet.signing') || 'Requesting signature...', key: 'wallet-login' })

    const nonceRes: any = await getWalletNonce(walletAddr)
    const nonceData = nonceRes.data || nonceRes
    const nonce = nonceData.nonce || nonceData
    const signMsg = nonceData.message || `Welcome to Pet AI!\n\nPlease sign this message to verify your wallet ownership.\n\nNonce: ${nonce}`

    const signature = await signMessageAsync({ message: signMsg })
    message.loading({ content: t('wallet.loggingIn') || 'Logging in...', key: 'wallet-login' })

    const loginRes: any = await walletLogin({
      address: walletAddr,
      signature,
      walletType: 'metamask',
      chainId: chainId.value
    })

    const loginData = loginRes.data || loginRes
    const token = loginData.token || loginData.accessToken
    if (!token) throw new Error('No token received from server')

    userStore.setUserToken(token)
    await userStore.fetchProfile()
    message.success({ content: t('wallet.loginSuccess') || 'Login successful!', key: 'wallet-login' })
    if (creditsStore.fetchBalance) creditsStore.fetchBalance()
  } catch (error: any) {
    message.destroy('wallet-login')
    if (error.code === 4001 || error.message?.includes('User rejected')) {
      message.warning(t('wallet.signatureRejected') || 'Signature rejected')
    } else {
      message.error(t('wallet.loginFailed') || 'Login failed: ' + (error.message || 'Unknown error'))
    }
    throw error
  }
}

function getUserDisplay() {
  if (userStore.nickname) return userStore.nickname
  if (userStore.walletAddress) return shortenAddress(userStore.walletAddress)
  // Smart email shortening: yuekoux@gmail.com → yue..com
  const email = userStore.email
  if (email && email.includes('@')) {
    const atIdx = email.indexOf('@')
    const local = email.slice(0, atIdx)
    const domain = email.slice(atIdx + 1)
    const tld = domain.includes('.') ? domain.slice(domain.lastIndexOf('.') + 1) : domain
    const prefix = local.length > 3 ? local.slice(0, 3) : local
    return `${prefix}..${tld}`
  }
  return userStore.username || email || 'User'
}

function getUserInitial() {
  const name = userStore.nickname || userStore.username || userStore.email || 'U'
  return name.charAt(0).toUpperCase()
}
</script>

<template>
  <header :class="['site-header', { 'site-header-glass': isFullBgPage }]">
    <div style="max-width: 1536px; height: 64px; margin: 0 auto; padding: 0 16px;">
      <a-space style="width: 100%; height: 100%; justify-content: space-between;">
        <!-- Logo -->
        <router-link to="/" style="text-decoration: none; display: flex; align-items: center; gap: 8px;">
          <div style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">
            <img src="/PET_1280X1280.PNG?v=alpha_fix_3" alt="Pet AI Logo" style="width: 100%; height: 100%; object-fit: contain; display: block;" />
          </div>
          <span style="font-size: 20px; font-weight: 700; color: var(--ht-text-strong);">MY AI PET</span>
        </router-link>

        <!-- Desktop Nav -->
        <nav class="desktop-nav">
          <a-space :size="32">
            <router-link
              v-for="link in navLinks"
              :key="link.path"
              :to="link.path"
              class="nav-link"
              :class="{ 'nav-link-active': currentPath === link.path }"
            >
              {{ link.name }}
            </router-link>
          </a-space>
        </nav>

        <!-- Right Actions -->
        <a-space :size="12" align="center">
          <!-- Total Credits (logged in only) -->
          <div v-if="userStore.isLoggedIn()" class="credits-display">
             <Sparkles style="width: 16px; height: 16px; color: #eab308; stroke-width: 2.5;" />
             <span style="font-weight: 800; color: #eab308; font-size: 16px; font-family: 'Inter', sans-serif;">{{ creditsStore.balance?.toLocaleString() }}</span>
          </div>

          <!-- Checkin button -->
          <button
            class="checkin-header-btn"
            @click="userStore.isLoggedIn() ? (showCheckinModal = true) : userStore.showLoginModal()"
          >
            <Gift style="width: 14px; height: 14px; flex-shrink: 0;" />
            <span class="checkin-header-text">{{ t('checkin.headerBtn') }}</span>
          </button>

          <!-- Language Toggle -->
          <a-dropdown placement="bottomRight">
            <template #overlay>
              <a-menu :selected-keys="[locale]" @click="handleLocaleChange">
                <a-menu-item key="en-US">
                  <a-space :size="8" style="min-width: 120px;">
                    <span style="font-size: 16px;">🇺🇸</span>
                    <span>English</span>
                  </a-space>
                </a-menu-item>
                <a-menu-item key="zh-CN">
                  <a-space :size="8" style="min-width: 120px;">
                    <span style="font-size: 16px;">🇨🇳</span>
                    <span>简体中文</span>
                  </a-space>
                </a-menu-item>
              </a-menu>
            </template>
            <a-button type="text" style="padding: 0 12px; height: 36px; border-radius: 8px; color: var(--ht-text); display: flex; align-items: center;">
              <a-space :size="6">
                <span style="font-size: 16px;">{{ locale === 'zh-CN' ? '🇨🇳' : '🇺🇸' }}</span>
                <span class="locale-text" style="font-size: 13px; font-weight: 500; color: var(--ht-text);">{{ locale === 'zh-CN' ? '中文' : 'EN' }}</span>
              </a-space>
            </a-button>
          </a-dropdown>

          <!-- User menu (logged in) -->
          <a-dropdown v-if="userStore.isLoggedIn()">
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <router-link to="/profile" style="text-decoration: none;">
                    <a-space :size="8">
                      <User style="width: 16px; height: 16px;" />
                      <span>{{ t('nav.profile') }}</span>
                    </a-space>
                  </router-link>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" danger>
                  <div style="cursor: pointer;" @click="handleLogout">
                    <a-space :size="8">
                      <LogOut style="width: 16px; height: 16px;" />
                      <span>{{ t('nav.logout') }}</span>
                    </a-space>
                  </div>
                </a-menu-item>
              </a-menu>
            </template>
            <a-button type="text" style="padding: 0 8px; height: 36px; color: var(--ht-text-strong); display: flex; align-items: center;">
              <a-space :size="8">
                <a-avatar size="small" :src="userStore.avatar || undefined" style="background: linear-gradient(135deg, #fb923c, #f59e0b);">
                  {{ getUserInitial() }}
                </a-avatar>
                <span class="user-display" style="font-size: 13px; color: var(--ht-text-strong);">
                  {{ getUserDisplay() }}
                </span>
              </a-space>
            </a-button>
          </a-dropdown>

          <!-- Login button (not logged in) -->
          <a-button
            v-else
            @click="userStore.showLoginModal()"
            class="login-btn"
          >
            <template #icon>
              <Key style="width: 16px; height: 16px;" />
            </template>
            {{ t('nav.login') }}
          </a-button>

          <!-- Mobile menu toggle -->
          <a-button
            type="text"
            class="mobile-menu-btn"
            @click="mobileMenuOpen = !mobileMenuOpen"
            style="color: var(--ht-text);"
          >
            <template #icon>
              <X v-if="mobileMenuOpen" style="width: 20px; height: 20px;" />
              <Menu v-else style="width: 20px; height: 20px;" />
            </template>
          </a-button>
        </a-space>
      </a-space>
    </div>

    <!-- Mobile Nav -->
    <div
      v-if="mobileMenuOpen"
      class="mobile-nav"
      style="border-top: 1px solid rgba(0,0,0,0.08); background: rgba(254,250,244,0.98); animation: fadeIn 0.3s ease-in-out;"
    >
      <nav style="max-width: 1536px; margin: 0 auto; padding: 16px;">
        <a-space direction="vertical" :size="4" style="width: 100%;">
          <router-link
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            @click="mobileMenuOpen = false"
            style="text-decoration: none; display: block; padding: 10px 12px; border-radius: 8px; font-size: 14px; font-weight: 500; transition: all 0.2s;"
            :style="currentPath === link.path
              ? { background: 'rgba(234,88,12,0.1)', color: '#ea580c' }
              : { color: '#6b7280' }"
          >
            {{ link.name }}
          </router-link>
        </a-space>
      </nav>
    </div>

    <!-- Login Modal -->
    <a-modal
      :open="userStore.loginModalVisible"
      @update:open="userStore.loginModalVisible = $event"
      :title="null"
      :footer="null"
      :width="420"
      centered
      rootClassName="pet-login-modal"
    >
      <div class="login-modal-body">

        <div class="login-single-col">
          <!-- Logo + Brand -->
          <div style="text-align: center; margin-bottom: 28px;">
            <img src="/PET_1280X1280.PNG?v=alpha_fix_3" alt="logo" style="width: 44px; height: 44px; object-fit: contain; margin-bottom: 10px;" />
            <h2 style="font-size: 20px; font-weight: 800; color: rgba(45,28,8,0.92); margin: 0 0 4px;">{{ t('auth.loginTitle') }}</h2>
          </div>

          <!-- Step 1: Email input -->
          <div v-if="emailStep === 1">
            <a-input
              :value="emailInput"
              placeholder="your@email.com"
              size="large"
              style="margin-bottom: 10px; border-radius: 10px;"
              @update:value="emailInput = $event"
              @press-enter="handleSendCode"
            />
            <a-button
              type="primary"
              block
              size="large"
              :loading="sendingCode"
              @click="handleSendCode"
              style="background: linear-gradient(90deg, #e07010 0%, #f5a020 100%); border: none; border-radius: 10px; font-weight: 600; margin-bottom: 20px; height: 46px;"
            >
              {{ t('auth.continueBtn') }}
            </a-button>
          </div>

          <!-- Step 2: Code verification -->
          <div v-else>
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
              <a-button type="text" size="small" @click="emailStep = 1" style="padding: 4px; color: rgba(70,48,20,0.45);">
                ←
              </a-button>
              <span style="font-size: 13px; color: rgba(70,48,20,0.55); word-break: break-all;">
                {{ t('auth.codeSent') }}: <strong style="color: rgba(45,28,8,0.82);">{{ emailInput }}</strong>
              </span>
            </div>
            <a-input
              :value="codeInput"
              placeholder="000000"
              size="large"
              maxlength="6"
              style="margin-bottom: 10px; border-radius: 10px; letter-spacing: 10px; font-size: 22px; font-weight: 700; text-align: center;"
              @update:value="codeInput = $event"
              @press-enter="handleEmailLogin"
            />
            <a-button
              type="primary"
              block
              size="large"
              :loading="loggingIn"
              @click="handleEmailLogin"
              style="background: linear-gradient(90deg, #e07010 0%, #f5a020 100%); border: none; border-radius: 10px; font-weight: 600; margin-bottom: 10px; height: 46px;"
            >
              {{ t('auth.emailLogin') }}
            </a-button>
            <div style="text-align: center;">
              <a-button
                type="link"
                :disabled="countdown > 0"
                :loading="sendingCode"
                @click="handleResendCode"
                style="padding: 0; color: #e07010; font-size: 13px;"
              >
                {{ resendBtnText }}
              </a-button>
            </div>
          </div>

          <!-- Divider -->
          <div class="login-divider">
            <span>{{ t('auth.orDivider') }}</span>
          </div>

          <!-- Google Login -->
          <a-button
            block
            size="large"
            @click="handleGoogleLogin"
            style="border: 1.5px solid rgba(190,155,95,0.3); background: rgba(253,248,240,0.6); border-radius: 10px; font-weight: 500; color: rgba(45,28,8,0.82); height: 46px; margin-bottom: 10px;"
          >
            <a-space :size="8" style="justify-content: center;">
              <span style="font-size: 16px; font-weight: 800; color: #4285F4;">G</span>
              <span>{{ t('auth.googleLogin') }}</span>
            </a-space>
          </a-button>

          <!-- Connect Wallet button -->
          <a-button
            block
            size="large"
            @click="handleConnectWallet"
            style="background: linear-gradient(135deg, #fb923c, #f59e0b); border: none; color: white; border-radius: 10px; font-weight: 700; height: 46px;"
          >
            <a-space :size="8" style="justify-content: center;">
              <span style="font-size: 18px; line-height: 1;">🦊</span>
              <span>{{ t('auth.connectWalletTitle') }}</span>
            </a-space>
          </a-button>
        </div>

      </div>
    </a-modal>

    <!-- Checkin Modal -->
    <CheckinModal
      :open="showCheckinModal"
      @update:open="showCheckinModal = $event"
    />
  </header>
</template>

<style scoped>
/* 默认：暖米白背景 + 深色文字 */
.site-header {
  --ht-text: rgba(70, 48, 20, 0.62);
  --ht-text-strong: rgba(45, 28, 8, 0.92);
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  border-bottom: 1px solid rgba(190, 155, 95, 0.2);
  background: rgba(255, 248, 240, 0.85);
  backdrop-filter: blur(20px);
  transition: background 0.3s, border-color 0.3s;
}

/* 视频背景页面 - 使用暖米白背景，半透明 */
.site-header-glass {
  --ht-text: rgba(70, 48, 20, 0.62);
  --ht-text-strong: rgba(45, 28, 8, 0.92);
  background: rgba(255, 248, 240, 0.85) !important;
  border-bottom-color: rgba(190, 155, 95, 0.2) !important;
}

.site-header-glass .nav-link {
  color: rgba(60, 40, 15, 0.95);
}

.site-header-glass .nav-link:hover {
  color: rgba(40, 25, 8, 1) !important;
  background: rgba(180, 140, 60, 0.2) !important;
}

.site-header-glass .nav-link-active {
  background: rgba(224, 112, 16, 0.25) !important;
  color: #d06000 !important;
}

/* 登录按钮 - 暖杏橙渐变 */
.login-btn {
  height: 36px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 2px 10px rgba(212, 137, 90, 0.3) !important;
  transition: all 0.2s ease !important;
}

.login-btn:hover {
  background: linear-gradient(90deg, #c07848 0%, #d9a06a 100%) !important;
  box-shadow: 0 4px 16px rgba(212, 137, 90, 0.45) !important;
  transform: scale(1.03) !important;
}

/* 视频背景页面下的登录按钮 */
.site-header-glass .login-btn {
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%) !important;
  box-shadow: 0 2px 12px rgba(212, 137, 90, 0.38) !important;
}

/* 签到领积分按钮 */
.checkin-header-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 36px;
  padding: 0 14px;
  border-radius: 18px;
  border: 1.5px solid rgba(212, 137, 90, 0.45);
  background: rgba(224, 112, 16, 0.1);
  color: #c07000;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.credits-display {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: none;
}

.credits-display:hover {
  background: transparent;
  transform: none;
}

.checkin-header-btn:hover {
  background: rgba(224, 112, 16, 0.18);
  border-color: rgba(212, 137, 90, 0.7);
  color: #a05800;
  transform: translateY(-1px);
}

/* 小屏隐藏文字，只显示图标 */
.checkin-header-text {
  display: none;
}

@media (min-width: 640px) {
  .checkin-header-text {
    display: inline;
  }
}

/* Desktop nav */
.desktop-nav {
  display: none;
}

@media (min-width: 768px) {
  .desktop-nav {
    display: flex;
  }
}

.nav-link {
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(60, 40, 15, 0.95);
  transition: all 0.2s;
}

.nav-link:hover {
  color: rgba(40, 25, 8, 1);
  background: rgba(180, 140, 60, 0.2);
}

.nav-link-active {
  background: rgba(224, 112, 16, 0.25) !important;
  color: #d06000 !important;
  font-weight: 600 !important;
}

/* Mobile */
.mobile-menu-btn {
  display: block;
}

@media (min-width: 768px) {
  .mobile-menu-btn {
    display: none;
  }
}

.mobile-nav {
  display: block;
}

@media (min-width: 768px) {
  .mobile-nav {
    display: none;
  }
}

/* Locale text */
.locale-text {
  display: none;
}

@media (min-width: 640px) {
  .locale-text {
    display: inline;
  }
}

/* User display */
.user-display {
  display: none;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

@media (min-width: 640px) {
  .user-display {
    display: inline-block;
  }
}

/* Login modal single-column layout */
.login-modal-body {
  padding: 0;
  background: #fdf8f0;
}

.login-single-col {
  padding: 36px 32px 32px;
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 18px 0;
  gap: 12px;
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(190, 155, 95, 0.28);
}

.login-divider span {
  font-size: 12px;
  color: rgba(100, 65, 20, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .login-single-col {
    padding: 28px 20px 24px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Modal teleported to body — 必须用 :global() 才能生效 */
/* 最高优先级覆盖 - 使用三重选择器 */
:global(.pet-login-modal.pet-login-modal.pet-login-modal .ant-modal-content) {
  background-color: #fdf8f0 !important;
  background: #fdf8f0 !important;
  padding: 0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
}

/* 覆盖所有可能的 :where() 选择器（dev hash 和 prod hash 均覆盖） */
:global(:where([class*="css-"]).ant-modal .ant-modal-content),
:global([class*="css-"].ant-modal .ant-modal-content) {
  background-color: #fdf8f0 !important;
  padding: 0 !important;
}

/* 使用双重选择器提高优先级，覆盖 :where() 选择器 */
:global(.pet-login-modal.pet-login-modal .ant-modal-content) {
  border-radius: 16px !important;
  overflow: hidden !important;
  padding: 0 !important;
  background: #fdf8f0 !important;
  background-color: #fdf8f0 !important;
  border: none !important;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18), 0 2px 8px rgba(0, 0, 0, 0.08) !important;
}

/* 强制覆盖 ant-design-vue 4.x 的默认 padding */
:global(.pet-login-modal.pet-login-modal .ant-modal-content) {
  padding: 0 !important;
}

:global(.pet-login-modal.pet-login-modal .ant-modal-body) {
  padding: 0 !important;
}

/* 移除 Modal 默认的内边距和边距 - 针对 ant-design-vue 4.x */
:global(.pet-login-modal) {
  padding: 0 !important;
  margin: 0 !important;
}

:global(.pet-login-modal .ant-modal) {
  padding: 0 !important;
  margin: 0 auto !important;
  top: 0 !important;
  max-width: 100% !important;
}

:global(.pet-login-modal .ant-modal-wrap) {
  padding: 0 !important;
  overflow: auto !important;
}

/* 针对 ant-design-vue 的 modal-root 和 mask */
:global(.ant-modal-root:has(.pet-login-modal)) {
  padding: 0 !important;
  margin: 0 !important;
}

:global(.pet-login-modal) .ant-modal-centered {
  padding: 0 !important;
  margin: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:global(.pet-login-modal.ant-modal-centered .ant-modal) {
  margin: 0 !important;
  top: 0 !important;
  padding: 0 !important;
}

/* 强制移除 ant-modal 的所有内边距 */
:global(.pet-login-modal .ant-modal) {
  box-sizing: border-box !important;
}

:global(.pet-login-modal .ant-modal > *) {
  padding: 0 !important;
  margin: 0 !important;
}

:global(.pet-login-modal .ant-modal-header) {
  padding: 0 !important;
  background: #fdf8f0 !important;
  border-bottom: none !important;
  margin-bottom: 0 !important;
}

:global(.pet-login-modal .ant-modal-body) {
  padding: 0 !important;
  background: #fdf8f0 !important;
}

:global(.pet-login-modal .ant-modal-footer) {
  display: none !important;
}

:global(.pet-login-modal .ant-modal-close) {
  top: 12px !important;
  right: 12px !important;
  background: transparent !important;
  color: rgba(70, 48, 20, 0.45) !important;
  box-shadow: none !important;
}

:global(.pet-login-modal .ant-modal-close:hover) {
  color: rgba(45, 28, 8, 0.72) !important;
  background: rgba(180, 140, 60, 0.12) !important;
}
</style>

<!-- 非 scoped 全局样式块：ant-design-vue 4.x Modal teleport 到 body 后，
     scoped :global() 注入顺序在 ant-design 之前可能被覆盖，
     独立 style 块注入更晚，可以可靠地覆盖 padding/background -->
<style>
.pet-login-modal .ant-modal-content {
  padding: 0 !important;
  background: #fdf8f0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  border: none !important;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18), 0 2px 8px rgba(0, 0, 0, 0.08) !important;
}

.pet-login-modal .ant-modal-header {
  padding: 0 !important;
  background: #fdf8f0 !important;
  border-bottom: none !important;
  margin-bottom: 0 !important;
}

.pet-login-modal .ant-modal-body {
  padding: 0 !important;
  background: #fdf8f0 !important;
}

.pet-login-modal .ant-modal-footer {
  display: none !important;
}

.pet-login-modal .ant-modal-close {
  top: 12px !important;
  right: 12px !important;
  background: transparent !important;
  color: rgba(70, 48, 20, 0.45) !important;
  box-shadow: none !important;
}

.pet-login-modal .ant-modal-close:hover {
  color: rgba(45, 28, 8, 0.72) !important;
  background: rgba(180, 140, 60, 0.12) !important;
}

/* 强制覆盖 input 边框颜色（prod/dev CSS-in-JS hash 差异导致白色边框） */
.pet-login-modal .ant-input,
.pet-login-modal .ant-input-outlined,
.pet-login-modal .ant-input-affix-wrapper,
.pet-login-modal .ant-input-affix-wrapper-outlined {
  border-color: rgba(190, 155, 95, 0.35) !important;
  background: rgba(255, 251, 243, 0.9) !important;
  color: rgba(45, 28, 8, 0.9) !important;
}

.pet-login-modal .ant-input:focus,
.pet-login-modal .ant-input-outlined:focus,
.pet-login-modal .ant-input-affix-wrapper:focus,
.pet-login-modal .ant-input-affix-wrapper-focused {
  border-color: #e07010 !important;
  box-shadow: 0 0 0 2px rgba(224, 112, 16, 0.12) !important;
}

.pet-login-modal .ant-input::placeholder {
  color: rgba(100, 65, 20, 0.4) !important;
}
</style>
