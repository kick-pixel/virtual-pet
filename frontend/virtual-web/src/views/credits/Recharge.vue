<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getPackages, getWalletAddress, getNetworks, getTokens, createOrder, submitTxHash, getOrder } from '@/api/payment'
import { formatCredits } from '@/utils/format'
import { useWallet } from '@/composables/useWallet'
import { useCreditsStore } from '@/store/credits'
import { parseEther } from 'viem'
import { message } from 'ant-design-vue'

import { Diamond, Check, Wallet, Copy, ExternalLink, Loader2 } from 'lucide-vue-next'

const { t } = useI18n()
const { address, isConnected, chainId, disconnect, sendTransactionAsync, trimAddress } = useWallet()
const creditsStore = useCreditsStore()

const packages = ref<any[]>([])
const selectedPkg = ref<any>(null)
const processing = ref(false)
const polling = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const currentOrderNo = ref('')

const platformWallets = ref<any[]>([])
const networks = ref<any[]>([])
const tokens = ref<any[]>([])

const currentPlatformAddress = computed(() => {
  if (!chainId.value || platformWallets.value.length === 0) return ''
  const wallet = platformWallets.value.find((w: any) => w.chainId === chainId.value)
  return wallet?.walletAddress || platformWallets.value[0]?.walletAddress || ''
})

const payToken = ref('BNB')

function selectToken(token: string) {
  payToken.value = token
}

function getNetworkName(id: number | undefined): string {
  if (!id) return 'bsc'
  const networkMap: Record<number, string> = {
    1: 'ethereum',
    11155111: 'ethereum', // Sepolia testnet
    56: 'bsc',
    97: 'bsc', // BSC testnet
    137: 'polygon',
    80001: 'polygon' // Mumbai testnet
  }
  return networkMap[id] || 'bsc'
}

// Watch chainId changes and reload tokens
watch(chainId, async (newChainId, oldChainId) => {
  if (newChainId !== oldChainId) {
    await loadTokens()
  }
})

onMounted(async () => {
  await Promise.all([
    loadPackages(),
    loadWalletAddress(),
    loadNetworks(),
    loadTokens()
  ])
})

onUnmounted(() => {
  stopPolling()
})

async function loadPackages() {
  try {
    const res: any = await getPackages()
    packages.value = res.data || res.rows || []
  } catch { /* ignore */ }
}

async function loadWalletAddress() {
  try {
    const res: any = await getWalletAddress()
    platformWallets.value = res.data || []
  } catch (e) {
    console.error('Failed to load wallet address:', e)
  }
}

async function loadNetworks() {
  try {
    const res: any = await getNetworks()
    networks.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load networks:', e)
  }
}

async function loadTokens() {
  try {
    const networkName = getNetworkName(chainId.value)
    const res: any = await getTokens({ networkType: networkName })
    tokens.value = res.data || res.rows || []

    // Auto-select first available token if current selection is not available
    if (tokens.value.length > 0) {
      const availableSymbols = tokens.value.map((t: any) => t.tokenSymbol)
      if (!availableSymbols.includes(payToken.value)) {
        payToken.value = tokens.value[0].tokenSymbol
      }
    }
  } catch (e) {
    console.error('Failed to load tokens:', e)
  }
}

let pollTimer: ReturnType<typeof setInterval> | null = null
const pollCount = ref(0)
const MAX_POLL_COUNT = 60 // 5 minutes max (60 * 5s)

function startPolling(orderNo: string) {
  pollCount.value = 0
  polling.value = true

  pollTimer = setInterval(async () => {
    pollCount.value++

    if (pollCount.value >= MAX_POLL_COUNT) {
      stopPolling()
      errorMsg.value = t('credits.rechargeTimeout')
      return
    }

    try {
      const res: any = await getOrder(orderNo)
      const order = res.data || res

      if (order.status === 'completed') {
        stopPolling()
        const totalCredits = (order.creditsAmount || 0) + (order.bonusCredits || 0)
        successMsg.value = t('credits.rechargeSuccess') + ' ' + t('credits.creditsAdded', [totalCredits])

        // Refresh credits balance
        creditsStore.fetchBalance()

        message.success(t('credits.rechargeSuccess'))
      } else if (order.status === 'cancelled' || order.status === 'expired') {
        stopPolling()
        errorMsg.value = t('credits.rechargeFailed') + ': ' + order.status
      }
    } catch (err) {
      // Ignore polling errors, continue
    }
  }, 5000) // Poll every 5 seconds
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  polling.value = false
}

async function handlePay() {
  if (!selectedPkg.value || !address.value || !chainId.value) return
  if (!currentPlatformAddress.value) {
    errorMsg.value = t('credits.noPlatformAddress')
    return
  }

  processing.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    // 1. Create order with new parameter format
    const orderRes: any = await createOrder({
      packageId: selectedPkg.value.packageId,
      chainId: chainId.value!,
      payToken: payToken.value,
      walletAddress: address.value
    })

    const orderData = orderRes.data || orderRes
    currentOrderNo.value = orderData.orderNo
    const payAmount = orderData.payAmount || selectedPkg.value.price

    // 2. Send blockchain transaction
    const txHash = await sendTransactionAsync({
      to: currentPlatformAddress.value as `0x${string}`,
      value: parseEther(String(payAmount))
    })

    // 3. Submit tx hash to backend
    try {
      await submitTxHash(currentOrderNo.value, String(txHash))
    } catch (err) {
      console.warn('Failed to submit tx hash:', err)
    }

    // 4. Show payment sent message
    successMsg.value = t('credits.paymentSent') + ' ' + t('credits.waitingConfirmation')

    // 5. Start polling order status
    startPolling(currentOrderNo.value)

  } catch (e: any) {
    errorMsg.value = e?.shortMessage || e?.message || t('credits.rechargeFailed')
  } finally {
    processing.value = false
  }
}

function copyAddress() {
  if (currentPlatformAddress.value) {
    navigator.clipboard.writeText(currentPlatformAddress.value)
    message.success('Copied!')
  }
}
</script>

<template>
  <div style="max-width: 1024px; margin: 0 auto; padding: 32px 16px;">
    <h1 style="font-size: 30px; font-weight: 700; margin: 0 0 8px;">{{ t('credits.rechargeTitle') }}</h1>
    <p style="color: #6b7280; margin: 0 0 32px;">{{ t('credits.rechargeSubtitle') || 'Choose a package and pay with cryptocurrency' }}</p>

    <a-row :gutter="[24, 24]" style="margin-bottom: 32px;">
      <a-col
        v-for="pkg in packages.length > 0 ? packages : [
          { packageId: 1, credits: 1000, bonusCredits: 0, price: '9.99' },
          { packageId: 2, credits: 5000, bonusCredits: 500, price: '39.99', popular: true },
          { packageId: 3, credits: 10000, bonusCredits: 2000, price: '69.99' }
        ]"
        :key="pkg.packageId"
        :xs="24"
        :sm="12"
        :lg="8"
      >
        <div
          @click="selectedPkg = pkg"
          class="package-card"
          :style="{
            position: 'relative',
            cursor: 'pointer',
            borderRadius: '12px',
            border: '2px solid',
            borderColor: selectedPkg?.packageId === pkg.packageId ? '#7c3aed' : '#e5e7eb',
            padding: '24px',
            transition: 'all 0.2s',
            background: selectedPkg?.packageId === pkg.packageId ? 'rgba(124, 58, 237, 0.05)' : 'white',
            boxShadow: selectedPkg?.packageId === pkg.packageId ? '0 4px 12px rgba(124, 58, 237, 0.2)' : 'none'
          }"
        >
          <a-tag v-if="pkg.popular" color="purple" style="position: absolute; top: -12px; left: 50%; transform: translateX(-50%);">
            Best Value
          </a-tag>
          <div style="text-align: center; margin-bottom: 16px;">
            <div class="gradient-text" style="font-size: 30px; font-weight: 800;">{{ formatCredits(pkg.credits) }}</div>
            <div style="font-size: 14px; color: #6b7280; margin-top: 4px;">{{ t('generate.credits') }}</div>
          </div>
          <div v-if="pkg.bonusCredits" style="text-align: center; margin-bottom: 16px;">
            <a-tag color="success" style="font-size: 12px;">+ {{ formatCredits(pkg.bonusCredits) }} bonus</a-tag>
          </div>
          <div style="text-align: center;">
            <div style="font-size: 20px; font-weight: 700;">${{ pkg.price }} <span style="font-size: 14px; color: #6b7280; font-weight: 400;">USDT</span></div>
          </div>
          <div v-if="selectedPkg?.packageId === pkg.packageId" style="position: absolute; top: 12px; right: 12px;">
            <div style="height: 24px; width: 24px; border-radius: 50%; background: #7c3aed; display: flex; align-items: center; justify-content: center;">
              <Check style="height: 16px; width: 16px; color: white;" />
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-card>
      <template #title>
        <div>
          <div style="display: flex; align-items: center; gap: 8px; font-weight: 500;">
            <Wallet style="height: 20px; width: 20px;" />
            {{ t('wallet.connect') || 'Connect Wallet' }}
          </div>
          <div style="font-size: 14px; color: #6b7280; font-weight: 400; margin-top: 4px;">Connect your Web3 wallet to complete the payment</div>
        </div>
      </template>
      <a-space direction="vertical" :size="24" style="width: 100%;">
        <div v-if="selectedPkg">
          <label style="font-size: 14px; font-weight: 500; margin-bottom: 8px; display: block;">{{ t('credits.selectToken') || 'Pay with' }}</label>
          <a-space :size="12" wrap>
            <a-button
              v-for="token in tokens"
              :key="token.tokenSymbol"
              :type="payToken === token.tokenSymbol ? 'primary' : 'default'"
              @click="selectToken(token.tokenSymbol)"
              style="flex: 1;"
            >
              {{ token.tokenSymbol }}
            </a-button>
            <!-- Fallback to default tokens if API not loaded yet -->
            <template v-if="tokens.length === 0">
              <a-button
                v-for="token in ['BNB', 'USDT']"
                :key="token"
                :type="payToken === token ? 'primary' : 'default'"
                @click="selectToken(token)"
                style="flex: 1;"
              >
                {{ token }}
              </a-button>
            </template>
          </a-space>
        </div>

        <div v-if="currentPlatformAddress" style="border-radius: 8px; background: #f9fafb; padding: 16px;">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
            <span style="font-size: 12px; color: #6b7280;">{{ t('credits.platformAddress') || 'Platform Address' }}</span>
            <a-button type="text" size="small" @click="copyAddress">
              <template #icon>
                <Copy style="height: 12px; width: 12px;" />
              </template>
            </a-button>
          </div>
          <div style="font-family: monospace; font-size: 12px; word-break: break-all;">{{ currentPlatformAddress }}</div>
        </div>

        <div v-if="!isConnected" style="text-align: center; padding: 16px 0;">
          <p style="font-size: 14px; color: #6b7280; margin: 0 0 16px;">Connect your wallet to proceed</p>
          <w3m-button />
        </div>

        <div v-else>
          <a-space direction="vertical" :size="16" style="width: 100%;">
            <div style="display: flex; align-items: center; justify-content: space-between; border-radius: 12px; background: #f9fafb; padding: 16px;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="height: 40px; width: 40px; border-radius: 50%; background: linear-gradient(135deg, #7c3aed, #ec4899); padding: 2px;">
                  <div style="display: flex; height: 100%; width: 100%; align-items: center; justify-content: center; border-radius: 50%; background: white; font-size: 12px; font-weight: 700;">
                    {{ chainId }}
                  </div>
                </div>
                <div>
                  <div style="font-weight: 500; font-family: monospace;">{{ trimAddress(address || '') }}</div>
                  <div style="font-size: 12px; color: #6b7280;">Connected</div>
                </div>
              </div>
              <a-button type="text" size="small" @click="disconnect()" style="color: #ef4444;">
                Disconnect
              </a-button>
            </div>

            <a-alert v-if="errorMsg" type="error" show-icon closable @close="errorMsg = ''">
              {{ errorMsg }}
            </a-alert>

            <a-alert v-if="successMsg" type="success" show-icon>
              <div style="display: flex; align-items: center; gap: 8px;">
                {{ successMsg }}
                <Loader2 v-if="polling" style="height: 16px; width: 16px; animation: spin 1s linear infinite;" />
              </div>
            </a-alert>

            <div v-if="polling" style="text-align: center; padding: 8px 0;">
              <p style="font-size: 12px; color: #6b7280; margin: 0;">
                {{ t('credits.confirmationHint') }}
              </p>
            </div>

            <a-button
              type="primary"
              size="large"
              block
              @click="handlePay"
              :loading="processing || polling"
              :disabled="!selectedPkg || processing || polling || !currentPlatformAddress"
            >
              <span v-if="!currentPlatformAddress">{{ t('credits.noPlatformAddress') }}</span>
              <span v-else-if="processing">{{ t('credits.step2') }}...</span>
              <span v-else-if="polling">{{ t('credits.step3') }}...</span>
              <span v-else>{{ t('credits.payNow') }} ${{ selectedPkg?.price || '0.00' }} {{ payToken }}</span>
            </a-button>
          </a-space>
        </div>

        <p v-if="!selectedPkg" style="font-size: 12px; color: #6b7280; text-align: center; margin: 0;">
          {{ t('credits.selectPackageHint') || 'Please select a package first' }}
        </p>
      </a-space>
    </a-card>
  </div>
</template>

<style scoped>
.gradient-text {
  background: linear-gradient(135deg, #7c3aed, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.package-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
