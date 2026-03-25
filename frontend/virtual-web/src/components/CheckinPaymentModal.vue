<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { useAccount, useSendTransaction, useSwitchChain } from '@wagmi/vue'
import { parseEther, parseUnits } from 'viem'
import { getCheckinFee } from '@/api/checkin'
import { getNetworks, getTokens, getWalletAddress, createCheckinOrder, getOrder, submitTxHash } from '@/api/payment'
import { Loader2, Check, X, AlertCircle } from 'lucide-vue-next'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const { t } = useI18n()

// ===== Wallet =====
const { address, isConnected, chainId: walletChainId } = useAccount()
const { sendTransactionAsync } = useSendTransaction()
const { switchChainAsync } = useSwitchChain()

async function handleSwitchChain() {
  if (!selectedNetworkChainId.value) return
  try {
    await switchChainAsync({ chainId: selectedNetworkChainId.value })
  } catch (e: any) {
    if (e?.code !== 4001 && e?.name !== 'UserRejectedRequestError') {
      message.error(t('credits.switchNetworkFailed') || 'Failed to switch network')
    }
    throw e
  }
}

// ===== State =====
type Step = 'connect' | 'confirm' | 'processing' | 'polling' | 'success' | 'error' | 'timeout'
const step = ref<Step>('confirm')
const networks = ref<any[]>([])
const allTokens = ref<any[]>([])
const allWalletAddresses = ref<any[]>([])
const fees = ref<{ bnb: string; usdt: string } | null>(null)
const selectedNetworkType = ref('')
const payToken = ref('BNB')
const currentOrderNo = ref('')
const errorMsg = ref('')
const feeLoading = ref(true)

// ===== Computed =====
const filteredTokens = computed(() => {
  if (!selectedNetworkType.value) return []
  return allTokens.value.filter((tk: any) => tk.networkType === selectedNetworkType.value)
})

const platformWalletAddress = computed(() => {
  if (!selectedNetworkType.value) return ''
  const matched = allWalletAddresses.value.find(
    (a: any) => a.networkType === selectedNetworkType.value
  )
  return matched?.walletAddress || ''
})

const selectedNetworkDisplay = computed(() => {
  const net = networks.value.find((n: any) => n.networkType === selectedNetworkType.value)
  return net?.networkName || selectedNetworkType.value
})

const walletNetworkType = computed(() => {
  if (!walletChainId.value) return ''
  const matched = networks.value.find((n: any) => Number(n.chainId) === walletChainId.value)
  return matched?.networkType || ''
})

const selectedNetworkChainId = computed(() => {
  const net = networks.value.find((n: any) => n.networkType === selectedNetworkType.value)
  return net ? Number(net.chainId) : null
})

const isNetworkMismatch = computed(() => {
  if (!isConnected.value || !selectedNetworkType.value || !networks.value.length) return false
  return walletNetworkType.value !== selectedNetworkType.value
})

const currentTokenData = computed(() =>
  allTokens.value.find(
    (tk: any) => tk.tokenSymbol === payToken.value && tk.networkType === selectedNetworkType.value
  )
)

const isNativeToken = computed(() => {
  if (!currentTokenData.value) return false
  return currentTokenData.value.isNative === true || !currentTokenData.value.contractAddress
})

const currentFeeAmount = computed(() => {
  if (!fees.value) return ''
  return payToken.value?.toUpperCase() === 'USDT' ? fees.value.usdt : fees.value.bnb
})

// ===== Load Data =====
onMounted(async () => {
  await Promise.all([
    loadFee(),
    loadNetworks(),
    loadAllTokens(),
    loadAllWalletAddresses()
  ])
  autoSelectNetwork()
  step.value = isConnected.value ? 'confirm' : 'connect'
})

async function loadFee() {
  feeLoading.value = true
  try {
    const res: any = await getCheckinFee()
    fees.value = res.data || res
  } catch (e) {
    console.error('Failed to load checkin fee:', e)
  } finally {
    feeLoading.value = false
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

async function loadAllTokens() {
  try {
    const res: any = await getTokens()
    allTokens.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load tokens:', e)
  }
}

async function loadAllWalletAddresses() {
  try {
    const res: any = await getWalletAddress()
    allWalletAddresses.value = res.data || res.rows || []
  } catch (e) {
    console.error('Failed to load wallet addresses:', e)
  }
}

function autoSelectNetwork() {
  const walletNet = walletNetworkType.value
  if (walletNet && networks.value.some((n: any) => n.networkType === walletNet)) {
    selectNetwork(walletNet)
  } else if (networks.value.length > 0) {
    selectNetwork(networks.value[0].networkType)
  }
}

function selectNetwork(networkType: string) {
  selectedNetworkType.value = networkType
  const networkTokens = allTokens.value.filter((tk: any) => tk.networkType === networkType)
  if (networkTokens.length > 0 && !networkTokens.some((tk: any) => tk.tokenSymbol === payToken.value)) {
    payToken.value = networkTokens[0].tokenSymbol
  }
}

// ===== Watchers =====
watch(() => isConnected.value, (connected) => {
  if (connected && step.value === 'connect') {
    step.value = 'confirm'
  }
})

watch(walletChainId, () => {
  const walletNet = walletNetworkType.value
  if (walletNet && networks.value.some((n: any) => n.networkType === walletNet)) {
    selectNetwork(walletNet)
  }
})

watch(() => props.visible, (visible) => {
  if (!visible) {
    stopPolling()
    setTimeout(() => {
      step.value = isConnected.value ? 'confirm' : 'connect'
      currentOrderNo.value = ''
      errorMsg.value = ''
      autoSelectNetwork()
    }, 300)
  }
})

// ===== Helpers =====
function close() {
  emit('update:visible', false)
}

function retry() {
  step.value = 'confirm'
  currentOrderNo.value = ''
  errorMsg.value = ''
}

// ===== Payment Flow =====
async function handlePay() {
  if (!isConnected.value) return
  if (!platformWalletAddress.value) {
    message.error(t('credits.noPlatformAddress'))
    return
  }
  if (!selectedNetworkChainId.value) {
    message.error(t('credits.selectNetwork') || 'Please select a network')
    return
  }
  if (!walletChainId.value || walletChainId.value !== selectedNetworkChainId.value) {
    message.error(t('credits.networkMismatch', [selectedNetworkDisplay.value]))
    return
  }
  if (!currentFeeAmount.value) {
    message.error(t('checkin.loadingFee'))
    return
  }

  step.value = 'processing'

  try {
    // 1. Create checkin order
    const orderRes: any = await createCheckinOrder({
      chainId: selectedNetworkChainId.value!,
      payToken: payToken.value,
      walletAddress: address.value as string
    })
    const orderData = orderRes.data || orderRes
    currentOrderNo.value = orderData.orderNo

    // 2. Send transaction — native token vs ERC-20
    let txHash: string
    if (isNativeToken.value) {
      txHash = String(await sendTransactionAsync({
        to: platformWalletAddress.value as `0x${string}`,
        value: parseEther(String(currentFeeAmount.value)),
        chainId: selectedNetworkChainId.value!
      }))
    } else {
      // ERC-20: encode transfer(address,uint256) with selector 0xa9059cbb
      const decimals: number = currentTokenData.value?.decimals ?? 18
      const contractAddr = currentTokenData.value?.contractAddress as `0x${string}`
      const amountBig = parseUnits(String(currentFeeAmount.value), decimals)
      const addrHex = (platformWalletAddress.value as string).slice(2).toLowerCase().padStart(64, '0')
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const amountHex: string = (amountBig as any).toString(16).padStart(64, '0')
      const erc20Data = ('0xa9059cbb' + addrHex + amountHex) as `0x${string}`
      txHash = String(await sendTransactionAsync({
        to: contractAddr,
        data: erc20Data,
        value: 0n,
        chainId: selectedNetworkChainId.value!
      }))
    }

    // 3. Submit tx hash (best-effort)
    try {
      await submitTxHash(currentOrderNo.value, String(txHash))
    } catch (err) {
      console.warn('Failed to submit tx hash:', err)
    }

    // 4. Poll order status
    step.value = 'polling'
    startPolling(currentOrderNo.value)

  } catch (e: any) {
    step.value = 'error'
    if (e?.code === 4001 || e?.name === 'UserRejectedRequestError') {
      errorMsg.value = t('credits.userRejected') || 'Transaction cancelled'
    } else {
      const raw = e?.shortMessage || e?.message || ''
      const isTechnical = /Exception|at com\.|mybatis|SQLException|NullPointer/i.test(raw)
      errorMsg.value = isTechnical
        ? (t('checkin.payFailed') || 'Payment failed, please try again')
        : (raw || t('checkin.payFailed') || 'Payment failed')
    }
  }
}

// ===== Polling =====
let pollTimer: ReturnType<typeof setInterval> | null = null
const pollCount = ref(0)
const MAX_POLL_COUNT = 60

function startPolling(orderNo: string) {
  pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++
    if (pollCount.value >= MAX_POLL_COUNT) {
      stopPolling()
      step.value = 'timeout'
      return
    }
    try {
      const res: any = await getOrder(orderNo)
      const order = res.data || res
      if (order.status === 'completed') {
        stopPolling()
        step.value = 'success'
        emit('success')
      } else if (order.status === 'cancelled' || order.status === 'expired') {
        stopPolling()
        step.value = 'error'
        errorMsg.value = 'Order ' + order.status
      }
    } catch (err) {
      // ignore polling errors
    }
  }, 5000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onUnmounted(() => {
  stopPolling()
})
</script>

<template>
  <a-modal
    :open="visible"
    :footer="null"
    :closable="false"
    :width="480"
    centered
    wrapClassName="checkin-pay-modal"
    @cancel="close"
  >
    <div class="ckp-wrap">
      <!-- Close Button -->
      <button @click="close" class="ckp-close-btn">
        <X style="width: 20px; height: 20px;" />
      </button>

      <!-- ===== CONNECT STEP ===== -->
      <div v-if="step === 'connect'" class="ckp-center">
        <div class="ckp-icon-wrap">
          <span style="font-size: 32px;">📅</span>
        </div>
        <h3 class="ckp-title">{{ t('credits.connectWalletTitle') || 'Connect Your Wallet' }}</h3>
        <p class="ckp-subtitle" style="margin-bottom: 24px;">
          {{ t('credits.connectWalletHint') || 'Connect your wallet to proceed with check-in' }}
        </p>
        <div style="display: flex; justify-content: center; margin-bottom: 12px;">
          <w3m-button balance="hide" />
        </div>
      </div>

      <!-- ===== CONFIRM STEP ===== -->
      <div v-if="step === 'confirm'">
        <!-- Header -->
        <div class="ckp-header">
          <div class="ckp-header-icon">
            <span style="font-size: 28px;">📅</span>
          </div>
          <div>
            <h3 class="ckp-title">{{ t('checkin.payTitle') }}</h3>
            <p class="ckp-subtitle">{{ t('checkin.paySubtitle') }}</p>
          </div>
        </div>

        <!-- Network Selection -->
        <div class="ckp-section">
          <div class="ckp-section-label">{{ t('credits.selectNetwork') }}</div>
          <div class="ckp-tab-group">
            <button
              v-for="net in networks"
              :key="net.networkType"
              class="ckp-tab"
              :class="{ 'ckp-tab--active': selectedNetworkType === net.networkType }"
              @click="selectNetwork(net.networkType)"
            >{{ net.networkName }}</button>
          </div>
        </div>

        <!-- Token Selection -->
        <div class="ckp-section">
          <div class="ckp-section-label">{{ t('credits.selectToken') }}</div>
          <div class="ckp-tab-group">
            <button
              v-for="token in filteredTokens"
              :key="token.tokenSymbol + '-' + token.networkType"
              class="ckp-tab"
              :class="{ 'ckp-tab--active': payToken === token.tokenSymbol }"
              @click="payToken = token.tokenSymbol"
            >{{ token.tokenSymbol }}</button>
          </div>
        </div>

        <!-- Fee Display -->
        <div class="ckp-fee-card">
          <span class="ckp-fee-label">{{ t('checkin.minPayAmount') }}</span>
          <div v-if="feeLoading" style="display: flex; align-items: center; gap: 6px;">
            <Loader2 class="spin-icon" style="width: 16px; height: 16px; color: #d4895a;" />
            <span style="font-size: 13px; color: rgba(70,48,20,0.55);">{{ t('checkin.loadingFee') }}</span>
          </div>
          <span v-else class="ckp-fee-value">{{ currentFeeAmount }} <span class="ckp-fee-token">{{ payToken }}</span></span>
        </div>

        <!-- Network mismatch warning -->
        <div v-if="isNetworkMismatch" class="ckp-mismatch">
          <div class="ckp-mismatch-body">
            <AlertCircle style="width: 16px; height: 16px; color: #d97706; flex-shrink: 0; margin-top: 2px;" />
            <div>
              <div style="font-size: 12px; font-weight: 600; color: #92400e; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 2px;">{{ t('wallet.network') }}</div>
              <div style="font-size: 13px; color: #78350f; line-height: 1.4;">{{ t('credits.networkMismatch', [selectedNetworkDisplay]) }}</div>
            </div>
          </div>
          <a-button type="primary" block @click="handleSwitchChain" class="ckp-switch-btn">
            {{ t('wallet.switchNetwork') }} → {{ selectedNetworkDisplay }}
          </a-button>
        </div>

        <!-- Pay Button -->
        <a-button
          @click="handlePay"
          type="primary"
          block
          size="large"
          :disabled="isNetworkMismatch || feeLoading || !currentFeeAmount"
          class="ckp-pay-btn"
        >
          {{ t('checkin.payAndCheckin') }}
        </a-button>
      </div>

      <!-- ===== PROCESSING STEP ===== -->
      <div v-if="step === 'processing'" class="ckp-center" style="padding: 40px 0;">
        <Loader2 class="spin-icon" style="width: 60px; height: 60px; color: #d4895a; margin: 0 auto 20px;" />
        <h3 class="ckp-title">{{ t('credits.paymentSent') || 'Sending Payment...' }}</h3>
        <p class="ckp-subtitle">{{ t('wallet.signing') || 'Please confirm in your wallet' }}</p>
      </div>

      <!-- ===== POLLING STEP ===== -->
      <div v-if="step === 'polling'" class="ckp-center" style="padding: 40px 0;">
        <Loader2 class="spin-icon" style="width: 60px; height: 60px; color: #d4895a; margin: 0 auto 20px;" />
        <h3 class="ckp-title">{{ t('credits.waitingConfirmation') || 'Waiting for Confirmation...' }}</h3>
        <p class="ckp-subtitle" style="margin-bottom: 12px;">{{ t('credits.confirmationHint') || 'This may take 1-5 minutes' }}</p>
        <div style="font-size: 12px; color: rgba(70,48,20,0.45);">{{ pollCount }} / {{ MAX_POLL_COUNT }}</div>
      </div>

      <!-- ===== SUCCESS STEP ===== -->
      <div v-if="step === 'success'" class="ckp-center" style="padding: 40px 0;">
        <div class="ckp-result-icon ckp-result-icon--success">
          <Check style="width: 44px; height: 44px; color: #22c55e;" />
        </div>
        <h3 class="ckp-title">{{ t('checkin.paySuccess') }}</h3>
        <a-button
          @click="close"
          type="primary"
          size="large"
          style="margin-top: 24px; height: 48px; padding: 0 32px; border-radius: 12px; background: #22c55e; border: none; font-weight: 600;"
        >
          {{ t('common.confirm') || 'OK' }}
        </a-button>
      </div>

      <!-- ===== ERROR STEP ===== -->
      <div v-if="step === 'error'" class="ckp-center" style="padding: 40px 0;">
        <div class="ckp-result-icon ckp-result-icon--error">
          <X style="width: 44px; height: 44px; color: #ef4444;" />
        </div>
        <h3 class="ckp-title">{{ t('checkin.payFailed') }}</h3>
        <p class="ckp-subtitle" style="margin-bottom: 24px;">{{ errorMsg }}</p>
        <a-space :size="12">
          <a-button @click="retry" size="large" style="height: 44px; padding: 0 24px; border-radius: 10px;">
            {{ t('credits.tryAgain') || 'Try Again' }}
          </a-button>
          <a-button @click="close" type="primary" size="large" style="height: 44px; padding: 0 24px; border-radius: 10px;">
            {{ t('credits.close') || 'Close' }}
          </a-button>
        </a-space>
      </div>

      <!-- ===== TIMEOUT STEP ===== -->
      <div v-if="step === 'timeout'" class="ckp-center" style="padding: 40px 0;">
        <div class="ckp-result-icon ckp-result-icon--warn">
          <AlertCircle style="width: 44px; height: 44px; color: #f59e0b;" />
        </div>
        <h3 class="ckp-title">{{ t('credits.rechargeTimeout') || 'Taking Longer Than Expected' }}</h3>
        <p class="ckp-subtitle" style="margin-bottom: 24px;">{{ t('credits.timeoutHint') }}</p>
        <a-button @click="close" type="primary" size="large" style="height: 48px; padding: 0 32px; border-radius: 12px;">
          {{ t('credits.checkLater') || 'OK' }}
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<style scoped>
.ckp-wrap {
  padding: 24px 28px 28px;
  position: relative;
}

/* Close button */
.ckp-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: rgba(70, 48, 20, 0.45);
  transition: all 0.2s;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 8px;
}
.ckp-close-btn:hover {
  color: rgba(45, 28, 8, 0.9);
  background: rgba(180, 140, 60, 0.12);
}

/* Header */
.ckp-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.ckp-header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  flex-shrink: 0;
}
.ckp-title {
  font-size: 18px;
  font-weight: 800;
  color: rgba(45, 28, 8, 0.92);
  margin: 0 0 4px;
}
.ckp-subtitle {
  font-size: 13px;
  color: rgba(70, 48, 20, 0.62);
  margin: 0;
  line-height: 1.5;
}

/* Section */
.ckp-section {
  margin-bottom: 14px;
}
.ckp-section-label {
  font-size: 11px;
  font-weight: 600;
  color: rgba(70, 48, 20, 0.55);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 7px;
}

/* Pill tabs */
.ckp-tab-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.ckp-tab {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid rgba(190, 155, 95, 0.3);
  background: rgba(253, 248, 240, 0.8);
  font-size: 13px;
  font-weight: 500;
  color: rgba(70, 48, 20, 0.72);
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1.6;
  white-space: nowrap;
}
.ckp-tab:hover {
  border-color: rgba(212, 137, 90, 0.55);
  color: rgba(45, 28, 8, 0.92);
  background: rgba(212, 137, 90, 0.08);
}
.ckp-tab--active {
  border-color: #d4895a;
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(212, 137, 90, 0.3);
}

/* Fee card */
.ckp-fee-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(212, 137, 90, 0.06);
  border: 1px solid rgba(212, 137, 90, 0.3);
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}
.ckp-fee-label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(70, 48, 20, 0.72);
}
.ckp-fee-value {
  font-weight: 800;
  font-size: 20px;
  color: #c07040;
  line-height: 1;
}
.ckp-fee-token {
  font-size: 13px;
  font-weight: 600;
}

/* Network mismatch */
.ckp-mismatch {
  border: 1.5px solid #fbbf24;
  border-radius: 12px;
  background: linear-gradient(135deg, #fffbeb, #fef9ee);
  padding: 14px 16px;
  margin-bottom: 16px;
}
.ckp-mismatch-body {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}
.ckp-switch-btn {
  height: 38px !important;
  border-radius: 9px !important;
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  border: none !important;
  font-weight: 600 !important;
  font-size: 13px !important;
}

/* Pay button */
.ckp-pay-btn {
  height: 52px !important;
  border-radius: 12px !important;
  background: linear-gradient(90deg, #d4895a 0%, #ebb07a 100%) !important;
  border: none !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  box-shadow: 0 4px 14px rgba(212, 137, 90, 0.3) !important;
}

/* Center layout for status steps */
.ckp-center {
  text-align: center;
}
.ckp-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  margin-bottom: 16px;
}

/* Result icons */
.ckp-result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 20px;
}
.ckp-result-icon--success { background: #dcfce7; }
.ckp-result-icon--error   { background: #fee2e2; }
.ckp-result-icon--warn    { background: #fef3c7; }

/* Spin animation */
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>

<style>
/* 签到支付弹框 - 暖米白背景，与充值弹框风格一致 */
.checkin-pay-modal .ant-modal-content {
  background: #fdf8f0 !important;
  border-radius: 16px !important;
  overflow: hidden !important;
  padding: 0 !important;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18) !important;
}
.checkin-pay-modal .ant-modal-body {
  padding: 0 !important;
  background: #fdf8f0 !important;
}
</style>
